(ns
    com.github.benjaminasdf.idlelib.compclient
    (:require
     [clojure.java.shell :as sh]
     [clojure.java.io :as io]
     [clojure.data.json :as json]
     [com.github.benjaminasdf.idlelib.project-state
      :as
      proj-state]
     [com.github.benjaminasdf.idlelib.component-server
      :as
      comp.server]
     [com.github.benjaminasdf.idlelib.config
      :as
      config]
     [com.github.benjaminasdf.idlelib.notify
      :as
      notify]
     [com.github.benjaminasdf.idlelib.execute
      :as
      execute]))

(defn first-matching-file-in-dir [dir pred]
  (->>
   dir
   io/file
   .listFiles
   (map #(.getPath %))
   (filter pred)
   first))

(defn proj-sln [proj]
  (->
   proj
   .getBasePath
   (first-matching-file-in-dir
    #(re-matches #"^.*\.sln$" %))))

(defn comp-server-status []
  (comp.server/status @comp.server/data))

(defn collect-comps-and-post [sln warn]
  (execute/execute-with-progress-indicator
   (str "ideaidle: Indexing components "
        (.getName (io/file sln)))
   (fn [_]
     (let
         [{:keys [out err exit]}
           (sh/sh
            "dotnet"
            (config/ideaidle-path
             "bin/RoslynBin/CompCollector.dll"
             @config/config)
            sln)]
         (case exit
           0 (comp.server/handle-comps!
              (json/read-str out :key-fn keyword))
           (warn
            (str
             "Err completion dotnet:\n"
             err)))))))

(defn
  user-collect-comps
  [proj]
  (notify/notification
   proj
   "Indexing components")
  (collect-comps-and-post
   (proj-sln proj)
   (partial notify/warn proj)))

(defn
  force-server
  [{proj :project}]
  (collect-comps-and-post
   (proj-sln proj)
   (partial notify/notifiy-warn proj)))

(defn
  auto-collect-components?
  [proj]
  (some->
   proj
   proj-sln
   clojure.java.io/file
   .getName
   #{"RoslynPlayground.sln" "IdleGame.sln"}))

(defn check-comp-server-action [{proj :project}]
  (let
      [{:keys [::comp.server/alive ::comp.server/total]} (comp-server-status)]
      (if alive
        (notify/notification
         {:proj
          proj
          :title "Component completions status:"
          :msg
          (format "alive: %s\ntotal comps: %d"
                  alive total)})
        (user-collect-comps proj))))

(let [did (atom #{})]
  (defn
    maybe-start-collecting-comps
    []
    (when-let
        [proj @proj-state/project]
        (when-let
            [id
             (auto-collect-components? proj)]
          (when-not
              (@did id)
              (swap! did conj id)
              (when
                  (>
                   100
                   (::comp.server/total
                    (comp.server/status
                     @comp.server/data)))
                  (user-collect-comps proj)))))))
(comment
  (maybe-start-collecting-comps))

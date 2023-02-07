;; shouild be called project.clj
;; it's the opposite of core bro
(ns
    com.github.benjaminasdf.idlelib.core
    (:require
     [clojure.java.io]
     [com.github.benjaminasdf.idlelib.project-state :as project.state]
     [com.github.benjaminasdf.idlelib.config :as config]
     [com.github.benjaminasdf.idlelib.notify :as notify]
     [com.github.benjaminasdf.idlelib.compsearch-actions :as search]
     [com.github.benjaminasdf.idlelib.component-server :as comp.server])
    (:import
     [com.intellij.openapi.project
      Project]))
(defn
  initProj
  [^ Project project]
  (reset! project.state/project project)
  (try
    (config/load-config-with-cbs
     (partial
      notify/notifiy-warn
      project)
     (constantly nil))
    ;; create them to register them
    (search/actions)
    (require
     'com.github.benjaminasdf.idlelib.completion)
    (require
     'com.github.benjaminasdf.idlelib.idleactions)
    (comp.server/ensure-server)
    (catch Exception e (println e))))

(comment
  (notify/notifiy-warn @project.state/project "fo")
  (notify/notifiy-warn nil "fo")

  ;; (when (auto-collect-components? project)
  ;;     (completion.client/check-comp-server-action {:project project}))

  )


#_
(comment
  (let [proj @project.state/project]
    (completion.client/collect-comps-and-post
     (completion.client/proj-sln proj)
     println))
  (let [sln
        "/home/benj/idlegame/RoslynAnalyzers/RoslynPlayground/RoslynPlayground.sln"
        ;; "/home/benj/idlegame/IdleGame/IdleGame.sln"
        ]
    (sh/sh
     "dotnet"
     (config/ideaidle-path
      "bin/RoslynBin/CompCollector.dll"
      @config/config))))

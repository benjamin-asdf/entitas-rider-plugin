(ns compserver.main
  (:require
   [clojure.core.async
    :as a
    :refer [>! <! go chan timeout]]
   [clojure.set :as set]
   [clojure.string :as str]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [ring.util.response :as resp]
   [ring.adapter.jetty :as server]
   [ring.middleware.json :as mj])
  (:gen-class))

(def ^:private port 33243)

(def ^:private server (atom nil))

(def ^:private data (atom nil))

(defn map-counts [m]
  (zipmap
   (keys m)
   (map count (vals m))))

(defn limited-chan [size time f]
  (let [in-chan (chan (a/dropping-buffer size))]
    (go (while true
          (apply f (<! in-chan))
          (<! (timeout time))))
    in-chan))

(defn make-limited [size time f]
  (let [c (limited-chan size time f)]
    (fn [& args]
      (go (>! c args)))))

(def print-limited (make-limited 10 300 println))

(defn combine-m [m1 m2]
  (merge-with
   #(set/union (set %1) (set %2)) m1 m2))

(def add-comps
  (make-limited 4 300
                (fn [r]
                  (->>
                   r
                   :body
                   (swap! data combine-m))
                  (print-limited "added: " (map-counts @data)))))

(defn get-comps [types]
  (println (str "get comps " types))
  (->>
   (str/split types #"\|")
   (map keyword)
   (select-keys @data)
   (resp/response)))

(defn say-status []
  (resp/response
   {:alive true
    :total (->>
            (vals @data)
            (map count)
            (apply +))
    :comps (map-counts @data)}))

(defn cut-str [n s]
  (subs s 0 (min (count s) n)))

(defn log-msg [req]
  (->>
   req
   :body
   (#(try
       (slurp %)
       (catch Exception _
         (print-limited "issue: " req)
         "")))
   (cut-str 300)
   (print-limited)))

(defroutes handler
  (GET "/ask-comps/:comp-types"
       [comp-types]
       (get-comps comp-types))
  (GET "/status" _req (say-status))
  (POST "/log" req (and (log-msg req) "success"))
  (POST "/comps" req (and (add-comps req) "success"))
  (route/not-found "Not found"))

(def app
  (-> handler
      (mj/wrap-json-body {:keywords? true :bigdecimals? true})
      (mj/wrap-json-response)))

(defn -main []
  (when @server
    (.stop @server))
  (reset!
   server
   (server/run-jetty app
                {:port port :join? false}))
  (println (str "idea comp server started at " port)))

(defn restart-server []
  (-> handler
      (mj/wrap-json-response)
      (mj/wrap-json-body {:keywords? true :bigdecimals? true}))
  (-main))

(comment
  (restart-server)
  (defn say-when-val-count-changed
    [_key _watched old-data new-data]
    (let [m (map-counts old-data)
          new-m (map-counts new-data)]
      (some->>
       (filter
        #(not=
          ((first %) m) (second %))
        new-m)
       (not-empty)
       (println "changed: "))))


  (add-watch data :val-change say-when-val-count-changed)
  (restart-server)
  (identity @data))

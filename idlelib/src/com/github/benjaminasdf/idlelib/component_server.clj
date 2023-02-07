(ns
    com.github.benjaminasdf.idlelib.component-server
    (:require
     [clojure.spec.alpha :as s]
     [clojure.set :as set]
     [ring.adapter.jetty :as jetty]
     [com.github.benjaminasdf.idlelib.log
      :refer
      [log]]
     [muuntaja.middleware
      :as
      m.middleware]
     [muuntaja.core :as m]
     [reitit.ring :as ring]
     [reitit.coercion.spec]
     [reitit.ring.coercion :as rrc]
     [reitit.ring.middleware.muuntaja
      :as
      muuntaja]
     [clojure.java.io :as io]
     [reitit.ring.middleware.parameters
      :as
      parameters]))

(def port 33243)
(defonce server (atom nil))
(defonce data (atom {}))

(defn map-counts [m]
  (zipmap
   (keys m)
   (map count (vals m))))

(defn combine-m [m1 m2]
  (merge-with
   #(set/union (set %1) (set %2)) m1 m2))

(defn stop-jetty! []
  (.stop @server)
  (reset! server nil))

(defn
  status
  [d]
  {::alive true
   ::total (reduce + (map count (vals d)))})

(defn
  handle-comps!
  [d]
  (log (println-str
        "handle-comps:"
        (println-str)
        (prn-str d)))
  (let [d (swap! data combine-m d)]
    {:status 200 :body (status d)}))

(s/def ::alive boolean?)
(s/def ::total (s/and number? (complement neg?)))
(s/def ::status-res
  (s/keys :req [::alive ::total]))

(def
  ping
  {:get
   {:responses {200 {:body ::status-res}}
    :handler
    (fn
      [_]
      {:status 200
       :body (status @data)})}})

(def app
  (ring/ring-handler
   (ring/router
    [["/ping" ping]
     ["/status" ping]
     ["/log"
      {:post
       {:responses {200 {:body any?}}
        :handler
        (fn [m]
          (let [l (slurp (:body m))]
            (log l)
            (println "[IdeaIdleCompserver] " l))
          {:status 200 :body {:ok :ok}})}}]
     ["/comps"
      {:post
       {:responses
        {200 {:body ::status-res}}
        :handler
        (fn [{:keys [body-params] :as m}]
          (let [d
                ;; didn't figure out the muntaja middleware
                ;; for now
                (or
                 body-params
                 (m/decode
                  m/instance
                  "application/json"
                  (slurp (:body m))))]
            (handle-comps! d)))}}]]
    {:data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware
            [muuntaja/format-middleware
             m.middleware/wrap-format
             rrc/coerce-exceptions-middleware
             rrc/coerce-request-middleware
             rrc/coerce-response-middleware]}})))

(defn run-jetty! []
  (reset!
   server
   (->
    #'app
    (jetty/run-jetty
     {:port port :join? false}))))

(defn
  ensure-server
  []
  (or @server (run-jetty!)))

(comment
  (app
   {:request-method :post
    :uri "/api/comps"
    :headers {"content-type" "application/json"}
    ;; as buff?
    :body (m/encode
           m/instance
           "application/json"
           {:foo ["foobar"]})})
  (.getAttributeNames @server)
  (clojure.reflect/reflect
   org.eclipse.jetty.util.Attributes)
  (run-jetty!)
  (stop-jetty!)
  @data
  (->>
   {:olipa "kerran"}
   (m/encode
    m/instance
    "application/json")
   slurp)
  (reset! data {})
  (handle-comps! {:FlagComponent ["MaralIsAmazing"]})
  (get (@data :FlagComponent) "Foo882") )

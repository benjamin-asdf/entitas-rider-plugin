(ns
    com.github.benjaminasdf.idlelib.log
    (:require
     [clojure.java.io :as io]
     [com.github.benjaminasdf.idlelib.config :refer [config]]))

(defn
  log
  [& xs]
  (when-let
      [level
       (:config/log-level @config)]
      (>= level 1)
      (spit
       (io/file
        (System/getProperty
         "user.home")
        "ideaidle.log")
       (apply println-str xs)
       :append
       true)))

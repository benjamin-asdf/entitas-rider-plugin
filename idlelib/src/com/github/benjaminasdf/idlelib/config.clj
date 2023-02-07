(ns com.github.benjaminasdf.idlelib.config
    (:require
     [com.github.benjaminasdf.idlelib.specs
      :as
      specs]
     [clojure.spec.alpha :as s]
     [clojure.string :as str]
     [clojure.java.io :as io]))

(defn user-file []
  (->
   (System/getProperty
    "user.home")
   (str/replace "\\" "/")
   (io/file
    ".ideaidle.d/config.edn")))

(defn load-config []
  (let [user-f (user-file)]
    (and
        (.exists user-f)
        (read-string (slurp user-f)))))

(def
  config
  (atom
   (try
     (load-config)
     (catch Exception _ nil))))

(defn
  load-config-with-cbs
  "Load config with callbacks."
  [warn success]
  (try
    (let [c (load-config)
          msg (s/explain-str :config/config c)]
      (if
          (s/valid? :config/config c)
          (success (reset! config c))
          (warn
           (str
            "Config is invalid, fix by carefully copying default\nMessage:\n"
            msg))))
    (catch Exception e
      (warn (str "Err reading config: " (.getMessage e))))))

(defn ideaidle-path [path config]
  (let [file
        (io/file
         (:config/ideaidle-home config)
         path)]
    (->
     file
     .getAbsoluteFile
     .getPath)))

(comment
  (load-config-with-cbs
   println
   println)
  (conj {:foo {:f :a}}
        {:foo 10})
  (let [config
        (read-string
         (slurp "/home/benj/.ideaidle.d/config.edn"))]
    ;; (s/explain-str
    ;;  :config/config
    ;;  config)

    (into
     []
     (comp
      (keep :completion/prefix))
     (:completion/table @config))
    )
  )

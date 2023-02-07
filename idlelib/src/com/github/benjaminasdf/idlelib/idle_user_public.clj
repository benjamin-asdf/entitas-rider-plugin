(ns com.github.benjaminasdf.idlelib.idle-user-public
  (:require
   [com.github.benjaminasdf.idlelib.idle-user :as uu]
   [clojure.string :as str]
   [clojure.java.io :as io]))

(defn config-file []
  (->
   (System/getProperty
    "user.home")
   (str/replace "\\" "/")
   (io/file
    ".ideaidle.d/init.clj")))

(defn load-when-exits [file]
  (println "2")
  (when
      (.exists file)
      (load-file
       (.getAbsolutePath file))
      true))

(defn load-config []
  (println "1")
  (load-when-exits
   (config-file)))

(defn ideaidle-path [s]
  (->
   uu/ideaidle-home
   (io/file s)
   .getAbsoluteFile
   .getPath))

(ns com.github.benjaminasdf.idlelib.re-utils)

(defn any-of [& words]
  (format
   "((%s))"
   (clojure.string/join
    ")|("
    words)))

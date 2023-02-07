(ns com.github.benjaminasdf.idlelib.execute
  (:import [com.intellij.openapi.progress
            ProgressIndicator]
           [com.intellij.openapi.progress
            ProgressIndicator]))


(defn execute-with-progress-indicator
  ([title f] (execute-with-progress-indicator title f nil))
  ([title f g]
   (->
    (proxy
        [com.intellij.openapi.progress.Task$Backgroundable]
        [nil title]
      (run ([^ProgressIndicator indicator] (f indicator)))
      (onSuccess [] (when g (g))))
    .queue)))

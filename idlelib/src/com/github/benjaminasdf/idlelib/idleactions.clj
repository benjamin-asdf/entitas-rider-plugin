(ns com.github.benjaminasdf.idlelib.idleactions
  (:require
     [com.github.benjaminasdf.idlelib.actions :as action]
     [com.github.benjaminasdf.idlelib.compclient :as comp-client]
     [com.github.benjaminasdf.idlelib.config :as config]
     [com.github.benjaminasdf.idlelib.compsearch-actions :as compsearch]
     [com.github.benjaminasdf.idlelib.notify :as notify])
  (:import
   [javax.swing JComponent]
   [com.intellij.openapi.actionSystem DefaultActionGroup])
    (:gen-class
     :methods [^{:static true} [createPopupActionGroup
                                [javax.swing.JComponent]
                                com.intellij.openapi.actionSystem.DefaultActionGroup]]
     :name com.github.benjaminasdf.idlelib.idleactions.IdleActions))

(def
  default-idleactions
  [#:action
   {:display-text "Load ideaidle config"
    :function (fn
                [e]
                (config/load-config-with-cbs
                 (partial
                  notify/notifiy-warn
                  (:project e))
                 (fn
                   [config]
                   ;; register actions in action manager aswell
                   ;; for ideavim
                   (action/action-group
                    (compsearch/actions))
                   (notify/notification
                    (:project e)
                    "Loaded config: Success!"))))}
   #:action
   {:display-text "Check component completions status"
    :function comp-client/check-comp-server-action}
   #:action
   {:display-text "Collect all components"
    :function comp-client/force-server}])

(defn -createPopupActionGroup [_ & _]
  (action/action-group default-idleactions))

(comment
  #:action
  {:display-text "foo"
   :function
   (fn
     [e]
     (notify/notifiy-warn nil "hello"))})

(ns
    com.github.benjaminasdf.idlelib.actions
    (:require
     [com.github.benjaminasdf.idlelib.notify
      :as
      notify])
    (:import
     [com.intellij.openapi.actionSystem
      ActionManager
      AnAction
      DefaultActionGroup]))

(defn make-action* [{:keys
                    [:action/function
                     :action/display-text]
                    :as opts}]
  (doto
      (proxy
          [AnAction]
          []
          (actionPerformed
            ([e]
             (try
               (function
                (assoc
                 opts
                 :event e
                 :project (.getProject e)))
               (catch Exception ex
                 (notify/notifiy-warn
                  nil
                  (str
                   "Err in action:\n"
                   (.getMessage ex))))))))
      (->
       .getTemplatePresentation
       (.setText display-text false))))

(let [register (atom {})]
  (defn
    wrap-register!
    [id inner-function]
    (if-not
        id
        inner-function
        (do
          (println "put " id)
          (swap! register assoc id inner-function)
          (fn [e] ((@register id) e)))))

  (defn
    register!
    [action id reg]
    (do
      (when
          (and id (not (reg id)))
          (println "registering " id)
          (.registerAction
           (ActionManager/getInstance)
           id
           action))
      action))

  (defn make-action [{:keys
                      [:action/function
                       :action/id
                       :action/shortcut]
                      :as opts}]
    (let [reg @register]
      (doto
          (make-action*
           (assoc
            opts
            :action/function
            (wrap-register! id function)))
          (register!
           id
           reg)))))


(defn action-group [actions]
  (DefaultActionGroup.
   (doall
    (map make-action actions))))

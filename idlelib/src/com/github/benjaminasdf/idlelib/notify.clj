(ns com.github.benjaminasdf.idlelib.notify
    (:import
     [com.intellij.notification
      NotificationType
      Notification]))

(defn
  notification
  ([proj msg]
   (notification
    {:proj proj
     :msg msg}))
  ([{:keys [proj msg type title id]
     :or {type NotificationType/INFORMATION
          title "ideaidle"
          id "ideaidle"}}]
   (doto
       (Notification.
        id
        title
        msg
        type)
     (.notify proj))))

(defn warn [proj msg]
  (notification
   {:proj proj
    :msg msg
    :type NotificationType/WARNING}))

(def ^{:obsolete true} notifiy-warn warn)

(ns com.github.benjaminasdf.idlelib.compsearch-actions
  (:require
   [clojure.string :as str]
   [com.github.benjaminasdf.idlelib.caretutil :as caret]
   [com.github.benjaminasdf.idlelib.actions :as action]
   [com.github.benjaminasdf.idlelib.config :as config]
   [com.github.benjaminasdf.idlelib.notify :as notify])
  (:import
   [javax.swing JComponent]
   [com.intellij.find FindModel]
   [com.intellij.find.findInProject FindInProjectManager]
   [com.intellij.openapi.actionSystem
    CommonDataKeys
    DefaultActionGroup])
  (:gen-class
   :methods [^{:static true} [createPopupActionGroup
                              [javax.swing.JComponent]
                              com.intellij.openapi.actionSystem.DefaultActionGroup]]
   :name com.github.benjaminasdf.idlelib.compsearchActions.CompsearchActions))

(defn
  re-any-of
  [& words]
  (format
   "((%s))"
   (str/join ")|(" words)))

(defn
  re-generic-inv
  [& names]
  (str
   "\\."
   (apply re-any-of names)
   "<%1$s"))

(defn select [editor file]
  (and
   file
   (let [selection-model
         (.getSelectionModel editor)]
     (if
         (.hasSelection selection-model)
         (.getSelectedText selection-model)
         (do
           (.selectWordAtCaret
            selection-model
            false)
           (caret/word-at-point
            (.getText file)
            (-> editor .getCaretModel .getOffset)))))))

(defn
  find-in-proj
  [{:keys [project
           event
           :action/search-fmt]}]
  (let [editor (.getData
                event
                (CommonDataKeys/EDITOR))
        file (.getData
              event
              (CommonDataKeys/PSI_FILE))
        text (select editor file)
        text (format search-fmt text)
        findModel (doto
                      (FindModel.)
                      (.setStringToFind text)
                      (.setRegularExpressions true)
                      (.setPromptOnReplace false))]
    (.startFindInProject
     (FindInProjectManager/getInstance
      project)
     findModel)))

(defn ensure-fmt
  [{:keys [:action/generic-inv-search] :as opts}]
  (merge
   {:action/search-fmt
    (apply re-generic-inv generic-inv-search)}
   opts))

(defn
  ensure-fn
  [opts]
  (merge
   {:action/function find-in-proj}
   opts))

(defn
  actions
  []
  (into
   []
   (comp
    (map ensure-fmt)
    (map ensure-fn))
   (:action/searches @config/config)))

(defn -createPopupActionGroup [_ & _]
  (action/action-group (actions)))

(comment
  (def
    s-action
    #:action
    {:id "ideaidle.example.customsearch"
     :display-text "My Search AddReactEach"
     :search-fmt "AddReactEach.+Matcher.+%s"})

  (ensure-fmt s-action)

  (def
    s-config
    {:action/searches [s-action]})
  (into
   []
   (comp
    (map ensure-fmt)
    (map ensure-fn))
   [s-action]))

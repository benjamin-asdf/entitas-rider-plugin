(ns
    com.github.benjaminasdf.idlelib.completion
    (:require
     [clojure.core.match
      :refer
      [match]]
     [com.github.benjaminasdf.idlelib.log :refer [log]]
     [com.github.benjaminasdf.idlelib.component-server
      :as
      comp.server]
     [com.github.benjaminasdf.idlelib.config
      :refer
      [config]]
     [com.github.benjaminasdf.idlelib.caretutil
      :as
      caret]
     [com.github.benjaminasdf.idlelib.compclient :as client])
    (:import
     [java.lang Thread]
     [com.intellij.codeInsight.completion
      CompletionProvider
      CompletionType
      CompletionParameters
      InsertHandler
      CompletionInitializationContext
      InsertionContext]
     [com.intellij.codeInsight.completion.impl
      CamelHumpMatcher]
     [com.intellij.codeInsight.lookup
      LookupElement
      LookupElementBuilder
      LookupElementRenderer
      LookupElementPresentation]
     [com.intellij.openapi.progress
      ProgressManager]
     [com.intellij.openapi.util Key]
     [com.intellij.patterns
      PlatformPatterns
      PlatformPatterns]
     [java.awt Color])
    (:gen-class
     :extends
     com.intellij.codeInsight.completion.CompletionContributor
     :main
     false
     :name
     com.github.benjaminasdf.idlelib.completion.MyCompletionContributor
     :post-init
     post-init))

(def dummy-identifier "bestlul")

(def data-key (Key/create (str ::data)))

(def
 all-comps
  [:UniqueComponent
   :UniqueFlagComponent
   :Component
   :FlagComponent
   :PrimaryIndexComponent
   :IndexComponent])


;; TODO you also want something inside Matcher. .. <foo, @bar
;; TODO move cursor inside (), sometimes
;; TODO spec for completion table

(defn- text-before-point [text p]
  (let [it
        (caret/skip-chars-backward
         text
         p
         #{\newline})]
    it))




(defn-
  first-prefix-match
  "Find the first match of `:completion/text` in `:completion/prefix` of `table`.
   Also add `::match` element."
  [text table]
  (some
   (fn
     [elm]
     (when-let
         [str-match
          (re-find
           (:completion/prefix elm)
           text)]
         (assoc elm ::match str-match)))
   table))

;; (defn log [& xs]
;;   (spit
;;    (io/file
;;     (System/getProperty "user.home")
;;     "ideaidle.log")
;;    (apply println-str xs)
;;    :append true))
(comment
  (clojure.reflect/reflect InsertionContext)
  )

(defn-
  handle-insert
  [^InsertionContext context
   ^LookupElement item]

  (let [ctx (.getUserData item data-key)
        point (.getStartOffset context)
        start
        (-
         point
         (match
          (::match ctx)
          [s1 s2]
          (- (count s1) (count s2))
          :else (count (::match ctx))))
        s (format
           (:completion/insert-fmt ctx)
           (.getLookupString item))]
    (log (prn-str "start: " start s (::match ctx))
         (caret/skip-chars-backward
          (.getText (.getDocument context))
          point
          #{\newline}))
    (log "start offset: " (.getStartOffset context)
         "deleting from: " start)
    (.replaceString
     (.getDocument context)
     start
     (.getSelectionEndOffset
      context)
     s)))

(defn get-completions [types]
  (select-keys @comp.server/data types))

;; (select-keys @comp.server/data [:UniqueComponent])

(defn- completion-context
  "Return the first element in completion-table, if in correct context.
  Also adds `::match`."
  [^CompletionParameters parms table]
  (when
      table
      (let [pos (.getPosition parms)
            p
            (+ (.getStartOffset  pos)
               (.getTextLength pos)
               (-  (count dummy-identifier)))
            text
            (->
             pos
             (.getOriginalElement)
             (.getContainingFile)
             (.getText))]
        (when-let
            [text (text-before-point text p)]
            (first-prefix-match text table)))))

(defn-
  completions
  [ctx]
  (try
    (when-let
        [names (:completion/names ctx)]
        (get-completions
         (case
             names
             [:any]
             all-comps
             names)))
    (catch Throwable _ nil)))

(defn- make-elm
  "Create a lookup Element.
  `ctx` : An element of `completion-table`
  `text` : The main text
  `type-name` : Shows up on the right of the elm.
  `color` : The color of the main text foreground,
  can be `nil` in which case you get the default fg."
  [ctx text type-name color]
  (doto
      (->
       (LookupElementBuilder/create text)
       (.withExpensiveRenderer
        (proxy [LookupElementRenderer] []
          (renderElement [elm ^LookupElementPresentation pres]
            (doto
                pres
                (.setItemText text)
                (.setTypeText type-name))
            (when
                color
              (.setItemTextForeground pres color)))))
       (.withTypeText type-name)
       (.withInsertHandler
        (proxy [InsertHandler] []
          (handleInsert
            ([^InsertionContext context ^LookupElement item]
             (handle-insert context item))))))
    (.putUserData data-key ctx)))

(defn- make-elms [ctx m]
  (mapcat
   (fn [[type-key name-arr]]
     (let [type-name (name type-key)
           color
           (when-let
               [color
                (get-in @config [:completion/colors type-key])]
             (Color/decode color))]
       (->>
        name-arr
        (map
         #(make-elm ctx % type-name color)))))
   m))

(defn- check-canceled-till-done [fut]
  (while (not (future-done? fut))
    (Thread/sleep 20)
    (try
      (ProgressManager/checkCanceled)
      (catch Exception e
        (println "cancel it " e)
        (future-cancel fut)
        (throw e)))))

(defn-
  add-completions-1
  [parms _ result]
  (try
    (when-let
        [ctx
         (completion-context
          parms
          (:completion/table @config))]
        (let [result (.withPrefixMatcher
                      result
                      (CamelHumpMatcher.
                       (or (second (::match ctx)) "")
                       ;; not case sensitive
                       false
                       ;; typos allowed
                       true))]
          (check-canceled-till-done
           (future
             (.addAllElements
              result
              (make-elms
               ctx
               (completions ctx)))))))
    (catch Throwable _ nil)))

(defn -post-init [this]
  (.extend
   this
   CompletionType/BASIC
   (PlatformPatterns/psiElement)
   (proxy
       [CompletionProvider]
       []
       (addCompletions [& args]
         (apply add-completions-1 args)))))

(defn -beforeCompletion [_ ^CompletionInitializationContext ctx]
  (.setDummyIdentifier
   ctx dummy-identifier)
  (client/maybe-start-collecting-comps))

(comment
  (let [table [#:completion{:prefix #"fa"}
               #:completion{:prefix #"fo"}]
        text "fo"]
    (first-prefix-match text table))
  (let [table (:completion/table @config)
        text "                c.state.get@"
        ctx (first-prefix-match text table)]
    (completions ctx))
  (get-in @config [:completion/colors])
  (filter
   (complement :completion/prefix)
   (:completion/table @config)))

(comment
  (select-keys @comp.server/data all-comps))

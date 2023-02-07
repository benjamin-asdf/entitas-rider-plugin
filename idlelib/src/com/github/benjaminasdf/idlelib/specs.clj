(ns com.github.benjaminasdf.idlelib.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]))


(def components
  #{:UniqueComponent
    :UniqueFlagComponent
    :Component
    :FlagComponent
    :PrimaryIndexComponent
    :IndexComponent})

(s/def ::completion-prefix #(instance? java.util.regex.Pattern %))
(s/def ::component-name (conj components :any))

(s/def :completion/names (s/+ ::component-name))
(s/def :completion/insert-fmt string?)

(s/def
  ::completion-entry
  (s/keys
   :req
   [:completion/names
    :completion/insert-fmt
    :completion/prefix]))

(s/def :completion/table (s/* ::completion-entry))

(s/def
  :completion/colors
  (s/map-of
   components
   string?))

(s/def :config/valid-file-path
  (s/and string? #(.exists (io/file %))))

(s/def :config/ideaidle-home :config/valid-file-path)


(s/def :action/display-text string?)
(s/def :action/id (s/and string? #(re-matches #"[\w\.]{5,100}" %)))

(s/def :action/search-fmt string?)

(s/def :action/search-action
  (s/keys
   :opt [:action/id]
   :req
   [:action/display-text
    (or
     :action/search-fmt
     :action/generic-inv-search)]))

(s/def :action/searches
  (s/* :action/search-action))

(s/def :config/config
  (s/keys
   :req
   [:config/ideaidle-home]
   :opt
   [:completion/table
    :completion/colors
    :action/searches]))


(comment
  (load-file "/tmp/foo.clj")

  (s/explain

   :action/action
   #:action
   {:id "Foo.MyIddddddddddd"
    :display-text "Display"})
  (s/explain :config/valid-file-path "/home/benj/.aws")
  (s/conform :config/ideaidle-home "/home/benj/.aws"))

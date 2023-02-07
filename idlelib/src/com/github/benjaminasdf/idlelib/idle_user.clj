(ns com.github.benjaminasdf.idlelib.idle-user)

;; the best way to modifiy this file is in emacs clojure mode
;; can try intellij (rider) with plugin cursive
;; start your repl inside ideaidle/idlelib for real hacking

(def
  default-completion-table
  [{:prefix #"(?i)state\.get@(\w+)?$"
    :names [:uniq]
    :insert-fmt "state.Get<%s>().value"}
   {:prefix #"(?i)state\.add@(\w+)?$"
    :names [:uniq]
    :insert-fmt "state.Add<%s>()"}
   {:prefix #"(?i)state\.has@(\w+)?$"
    :names [:uniq]
    :insert-fmt "state.Has<%s>()"}
   {:prefix #"(?i)state\.set@(\w+)?$"
    :names [:uflag]
    :insert-fmt "state.Set<%s>()"}
   {:prefix #"(?i)state\.is@(\w+)?$"
    :names [:uflag]
    :insert-fmt "state.Is<%s>()"}
   {:prefix #"(?i)set@(\w+)?$"
    :names [:uflag :flag]
    :insert-fmt ".Set<%s>()"}
   {:prefix #"(?i)is@(\w+)?$"
    :names [:uflag :flag]
    :insert-fmt ".Is<%s>()"}
   {:prefix #"(?i)add@(\w+)?$"
    :names [:uniq :comp :ind :prim]
    :insert-fmt ".Add<%s>()"}
   {:prefix #"(?i)get@(\w+)?$"
    :names [:uniq :comp :ind :prim]
    :insert-fmt ".Get<%s>()"}
   {:prefix #"(?i)GetEntityWith@(\w+)?$"
    :names [:prim]
    :insert-fmt "GetEntityWith<%s>()"}
   {:prefix #"(?i)gew@(\w+)?$"
    :names [:prim]
    :insert-fmt "c.state.GetEntityWith<%s>()"}
   {:prefix #"(?i)GetEntitiesWith@(\w+)?$"
    :names [:ind]
    :insert-fmt "GetEntitiesWith<%s>()"}
   {:prefix #"(?i)geww@(\w+)?$"
    :names [:ind]
    :insert-fmt "c.state.GetEntitiesWith<%s>()"}
   {:prefix #"rect@(\w+)?$"
    :names [:all]
    :insert-fmt
    "AddReactEach(Matcher.AllOf<%s>(), e => {\n         });"}
   {:prefix #"(?i)getsingleentity<@(\w+)?$"
    :names [:all]
    :insert-fmt"state.GetSingleEntity<%s>()"}
   {:prefix #"<@(\w+)?$"
    :names [:all]
    :insert-fmt"%s"}])


(def default-comp-colors
  {:UniqueComponent "#F689FF"
   :UniqueFlagComponent "#a1fe9a"
   :Component "#8fcefe"
   :FlagComponent "#C5ff00"
   :PrimaryIndexComponent "#b48ffe"
   :IndexComponent "#feb48f"})


(def ideaidle-home
  (or (System/getenv "IDEAIDLE_HOME")
      "fix/the/path/man"))

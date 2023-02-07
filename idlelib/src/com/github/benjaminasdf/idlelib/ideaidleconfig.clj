(ns com.github.benjaminasdf.idlelib.ideaidleconfig
  (:require
   [clojure.java.shell :as sh]
   [com.github.benjaminasdf.idlelib.idle-user-public :as idle-user])
  (:gen-class
   :name
   com.github.benjaminasdf.idlelib.ideaidleconfig.IdeaIdleConfig
   :methods
   [^{:static true} [elispFile [String] String]
    ^{:static true} [inferiorElisp [] String]]))

(let [edir "src/main/elisp/"]
  (defn -elispFile [s]
    (->
     (idle-user/ideaidle-path
      (str edir s)))))

(let [it (atom nil)]
  (defn -inferiorElisp []
    (or
     @it
     (reset!
      it
      (if
          (try
            (sh/sh "emacs" "--version")
            (catch Exception _ false))
          "emacs"
          (idle-user/ideaidle-path
           "bin/Emacs/x86_64/bin/emacs.exe"))))))

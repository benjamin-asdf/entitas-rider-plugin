(ns compserver-check
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]))

(def port 33243)

(curl/get (str "http://localhost:" port "/ping"))
(curl/get (str "http://localhost:" port "/status"))

(json/decode
 (:body
  (curl/post
   (str
    "http://localhost:"
    port
    "/comps")
   {:body
    (json/encode
     {"UniqueComponent" ["fooba", "hurr", "bamba"]
      "FlagComponent" ["MyFlagComplul1"]})})) keyword)

(json/decode
 (:body
  (curl/post
   (str "http://localhost:" port "/log")
   {:body "foo"})))

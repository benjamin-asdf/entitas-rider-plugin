(ns com.github.benjaminasdf.idlelib.caretutil)

(defn skip-chars-backward-1
  "Return the string of characters at the end of S,
  untill the first character for which PRED is true.
  When PRED is never true, return the whole string."
  ([s pred] (skip-chars-backward-1 s pred []))
  ;; we did not figure out these :before or what ever things in defn
  ([s pred acc]
   (if-not
       (seq s)
       (apply str (reverse acc))
       (if
           (pred (last s)) (apply str (reverse acc))
           (recur (butlast s) pred (conj acc (last s)))))))


(defn skip-chars-backward [text p pred]
  "Return string we are looking back to, until pred
returns truthy for a char."
  (if (empty text)
    nil
    (str
     (skip-chars-backward-1
      (subs
       text
       0
       p)
      pred))))

(defn word-behind [text p]
  (skip-chars-backward text p #{\newline \space \< \>}))

(defn word-at-point [text p]
  (str
   (word-behind text p)
   (re-find
    #"\w+"
    (subs
     text
     p
     (min (+ p 20)
          (count text))))))

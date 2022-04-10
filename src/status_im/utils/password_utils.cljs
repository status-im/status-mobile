(ns status-im.utils.password-utils
  (:require [status-im.constants :as const]
            [status-im.utils.security :as security]))

(defn ord
  "Convert a character to a unicode integer"
  [val]
  (.charCodeAt val))

(defn to-numbers
  "Maps a string to a array of integers representing the string"
  [vals]
  (map ord vals))

(defn diff
  "Compares all characters in a string to the character to their right.
   If the character matches the next char, then the value becomes 1, if
   the characters are different, the value becomes 0."
  [vals]
  (map - (next vals) vals))

(defn is-same?
  "Returns true if both values are the same."
  [a b]
  (= a b))

(defn all-same?
  "Returns true if all characters in the give string are the same."
  [word]
  (let [first-letter  (first word)]
    (every? #{first-letter} word)))

(defn is-sequential?
  "Returns true if the unicode value of all characters in the given string are sequential"
  [sequence]
  (all-same? (diff (to-numbers sequence))))

(defn meets-minimum-length?
  "Returns true if the given string's length is greater than the defined minimum password length"
  [password]
  (>= (count password) const/min-password-length))

(defn valid-password
  "Returns true if all password requirements are met."
  [password]
  (and (meets-minimum-length? password)
       (not (all-same? (security/safe-unmask-data password)))
       (not (is-sequential? (security/safe-unmask-data password)))))

(defn confirm-password [password confirm]
  (= password confirm))

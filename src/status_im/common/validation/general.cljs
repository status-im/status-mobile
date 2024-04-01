(ns status-im.common.validation.general
  (:require
    [status-im.constants :as constants]))

(defn valid-public-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches constants/regx-public-key s))))

(defn valid-compressed-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches constants/regx-compressed-key s))))

(defn has-emojis? [s] (boolean (re-find utils.emojilib/emoji-regex s)))

(def no-special-chars-regex #"^[a-zA-Z0-9\-_ ]+$")
(defn has-special-characters?
  [s]
  (and (not (= s ""))
       (not (re-find no-special-chars-regex s))))


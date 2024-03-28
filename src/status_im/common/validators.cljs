(ns status-im.common.validators
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

(defn has-special-characters?
  [s]
  (and (not (= s ""))
       (not (re-find #"^[a-zA-Z0-9\-_ ]+$" s))))

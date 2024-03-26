(ns utils.emojilib
  (:require ["emojilib" :as emojis]
            [goog.object :as object]
            utils.string))

(def emoji-regex
  #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])")

(def lib (.-lib emojis))

(defn get-char
  [emoji-id]
  (when-let [emoji-map (object/get lib emoji-id)]
    (.-char ^js emoji-map)))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (utils.string/safe-replace text
                             #":([a-z_\-+0-9]*):"
                             (fn [[original emoji-id]]
                               (or (get-char emoji-id) original))))

(ns status-im.chat.models.message-content
  (:require [status-im.constants :as constants]))

(def stylings [[:bold   constants/regx-bold]
               [:italic constants/regx-italic]
               [:backquote constants/regx-backquote]])

(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))

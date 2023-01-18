(ns status-im.chat.models.message-content
  (:require [status-im2.setup.constants :as constants]))

(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))

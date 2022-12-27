(ns status-im2.contexts.chat.messages.content.deleted.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]))

(defn deleted-message
  [{:keys [deleted? deleted-undoable-till timestamp-str deleted-for-me-undoable-till]}]
  [quo/system-message
   {:type             :deleted
    :label            (if deleted? :message-deleted :message-deleted-for-you)
    :labels           {:pinned-a-message        (i18n/label :t/pinned-a-message)
                       :message-deleted         (i18n/label :t/message-deleted-for-everyone)
                       :message-deleted-for-you (i18n/label :t/message-deleted-for-you)
                       :added                   (i18n/label :t/added)}
    :timestamp-str    timestamp-str
    :non-pressable?   true
    :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}])

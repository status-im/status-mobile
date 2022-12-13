(ns status-im.ui2.screens.chat.messages.deleted-message
  (:require [i18n.i18n :as i18n]
            [quo2.components.messages.system-message :as system-message]))

(defn message
  [{:keys [timestamp-str deleted? deleted-undoable-till
           deleted-for-me-undoable-till]}]
  [system-message/system-message
   {:type             :deleted
    :label            (if deleted? :message-deleted :message-deleted-for-you)
    :labels           {:pinned-a-message        (i18n/label :pinned-a-message)
                       :message-deleted         (i18n/label :message-deleted-for-everyone)
                       :message-deleted-for-you (i18n/label :message-deleted-for-you)
                       :added                   (i18n/label :added)}
    :timestamp-str    timestamp-str
    :non-pressable?   true
    :animate-landing? (if (or deleted-undoable-till deleted-for-me-undoable-till)
                        true
                        false)}])

(ns status-im2.contexts.chat.messages.content.deleted.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn deleted-message
  [{:keys [deleted? deleted-by deleted-undoable-till timestamp-str deleted-for-me-undoable-till]}]
  (let [msg [quo/system-message
             {:type             :deleted
              :label            (if deleted? :message-deleted :message-deleted-for-you)
              :labels           {:pinned-a-message        (i18n/label :t/pinned-a-message)
                                 :message-deleted         (i18n/label :t/message-deleted-for-everyone)
                                 :message-deleted-for-you (i18n/label :t/message-deleted-for-you)
                                 :added                   (i18n/label :t/added)}
              :timestamp-str    timestamp-str
              :non-pressable?   true
              :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}]]
    (if deleted-by
      [rn/view {:style {:border-width 1 :border-color :blue}} msg]
      msg)))

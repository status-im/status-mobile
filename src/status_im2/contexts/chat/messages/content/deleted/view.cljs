(ns status-im2.contexts.chat.messages.content.deleted.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn user-xxx-deleted-this-message
  [{:keys [display-name profile-picture]}]
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center
            :flex           1
            :flex-wrap      :wrap}}
   [rn/view {:style {:margin-right 4}}
    [quo/user-avatar
     {:full-name         display-name
      :profile-picture   profile-picture
      :status-indicator? false
      :ring?             false
      :size              :xxxs}]]
   [quo/author {:primary-name display-name}]
   [quo/text
    {:style           {:margin-left 4}
     :size            :paragraph-2
     :number-of-lines 1}
    (i18n/label :t/deleted-this-message)]])

(defn- compute-on-long-press-fn
  [{:keys [deleted? pinned] :as message}
   {:keys [message-pin-enabled] :as context}]
  ;; only show drawer for user who has the permission to unpin messages
  (when (and pinned deleted? message-pin-enabled)
    (fn []
      (rf/dispatch [:dismiss-keyboard])
      (rf/dispatch [:bottom-sheet/show-sheet
                    {:content (drawers/reactions-and-actions message
                                                             context)}]))))

(defn deleted-by-message
  [{:keys [deleted-by deleted-undoable-till timestamp-str deleted-for-me-undoable-till from]}
   on-long-press-fn]
  (let [;; deleted message with nil deleted-by is deleted by (:from message)
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity (or deleted-by from)]))
        contact      (rf/sub [:contacts/contact-by-address (or deleted-by from)])
        photo-path   (when-not (empty? (:images contact))
                       (rf/sub [:chats/photo-path (or deleted-by from)]))]
    [quo/system-message
     {:type             :deleted
      :timestamp-str    timestamp-str
      :child            [user-xxx-deleted-this-message
                         {:display-name display-name :profile-picture photo-path}]
      :on-long-press    on-long-press-fn
      :non-pressable?   (if on-long-press-fn false true)
      :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}]))

(defn deleted-message
  [{:keys [deleted? deleted-by deleted-undoable-till timestamp-str
           deleted-for-me-undoable-till from]
    :as   message}
   context]
  (let [pub-key          (rf/sub [:multiaccount/public-key])
        deleted-by-me?   (= (or deleted-by from) pub-key)
        on-long-press-fn (compute-on-long-press-fn message context)]
    (if (and deleted? (not deleted-by-me?))
      [deleted-by-message message on-long-press-fn]
      [quo/system-message
       {:type             :deleted
        :label            (if deleted? :message-deleted :message-deleted-for-you)
        :labels           {:message-deleted         (i18n/label :t/message-deleted-for-everyone)
                           :message-deleted-for-you (i18n/label :t/message-deleted-for-you)}
        :on-long-press    on-long-press-fn
        :timestamp-str    timestamp-str
        :non-pressable?   (if on-long-press-fn false true)
        :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}])))

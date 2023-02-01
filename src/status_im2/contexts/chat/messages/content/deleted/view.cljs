(ns status-im2.contexts.chat.messages.content.deleted.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn user-xxx-deleted-this-message
  [{:keys [display-name profile-picture]}]
  [rn/view {:style {:flex-direction :row :align-items :center}}
   [rn/view {:style {:margin-right 4}}
    [quo/user-avatar
     {:full-name         display-name
      :profile-picture   profile-picture
      :status-indicator? false
      :ring?             false
      :size              :xxxs}]]
   [quo/display-name
    {:profile-name display-name
     :text-style   {}}]
   [quo/text {:style {:margin-left 4} :size :paragraph-2}
    (i18n/label :t/deleted-this-message)]])

(defn deleted-by-message
  [{:keys [deleted-by deleted-undoable-till timestamp-str deleted-for-me-undoable-till from]}]
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
      :non-pressable?   true
      :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}]))

(defn deleted-message
  [{:keys [deleted? deleted-by deleted-undoable-till timestamp-str deleted-for-me-undoable-till from]
    :as   message}]
  (let [pub-key        (rf/sub [:multiaccount/public-key])
        deleted-by-me? (= (or deleted-by from) pub-key)]
    (if (not deleted-by-me?)
      [deleted-by-message message]
      [quo/system-message
       {:type             :deleted
        :label            (if deleted? :message-deleted :message-deleted-for-you)
        :labels           {:message-deleted         (i18n/label :t/message-deleted-for-everyone)
                           :message-deleted-for-you (i18n/label :t/message-deleted-for-you)}
        :timestamp-str    timestamp-str
        :non-pressable?   true
        :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}])))

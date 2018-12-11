(ns status-im.ui.components.popup-menu.views
  (:require [status-im.ui.components.react :as react]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn show-desktop-menu [items]
  (.show rn-dependencies/desktop-menu
         (clj->js (mapv #(hash-map :text (:text %1) :onPress (:on-select %1)) items))))

(defn get-chat-menu-items [group-chat public? chat-id]
  (->> [(when (and (not group-chat) (not public?))
          {:text (i18n/label :t/view-profile)
           :on-select #(re-frame/dispatch [:show-profile-desktop chat-id])})
        (when (and group-chat (not public?))
          {:text (i18n/label :t/group-info)
           :on-select #(re-frame/dispatch [:show-group-chat-profile])})
        {:text (i18n/label :t/clear-history)
         :on-select #(re-frame/dispatch [:chat.ui/clear-history-pressed])}
        {:text (i18n/label :t/delete-chat)
         :on-select #(re-frame/dispatch [(if (and group-chat (not public?))
                                           :group-chats.ui/remove-chat-pressed
                                           :chat.ui/remove-chat-pressed)
                                         chat-id])}]
       (remove nil?)))

(defn get-message-menu-items [chat-id message-id]
  [{:text (i18n/label :t/resend-message)
    :on-select #(re-frame/dispatch [:chat.ui/resend-message chat-id message-id])}
   {:text (i18n/label :t/delete-message)
    :on-select #(re-frame/dispatch [:chat.ui/delete-message chat-id message-id])}])

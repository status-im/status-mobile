(ns status-im.ui.components.popup-menu.views
  (:require [status-im.ui.components.react :as react]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]))

(defn show-desktop-menu [items]
  (.show rn-dependencies/desktop-menu
         (if config/mobile-ui-for-desktop?
           (clj->js (mapv #(hash-map :text (:label %1) :onPress (:action %1)) items))
           (clj->js (mapv #(hash-map :text (:text %1) :onPress (:on-select %1)) items)))))

(defn get-chat-menu-items [group-chat public? chat-id]
  (->> [(when (and (not group-chat) (not public?))
          {:text (i18n/label :t/view-profile)
           :on-select #(re-frame/dispatch [:show-profile-desktop chat-id])})
        (when (and group-chat (not public?))
          {:text (i18n/label :t/group-info)
           :on-select #(re-frame/dispatch [:show-group-chat-profile])})
        {:text (i18n/label :t/clear-history)
         :on-select #(re-frame/dispatch [:chat.ui/clear-history-pressed])}
        {:text (i18n/label :t/fetch-history)
         :on-select #(re-frame/dispatch [:chat.ui/fetch-history-pressed chat-id])}
        #_{:text      "Fetch 48-60h"
           :on-select #(re-frame/dispatch [:chat.ui/fetch-history-pressed48-60 chat-id])}
        #_{:text      "Fetch 84-96h"
           :on-select #(re-frame/dispatch [:chat.ui/fetch-history-pressed84-96 chat-id])}
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

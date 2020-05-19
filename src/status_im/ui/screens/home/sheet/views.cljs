(ns status-im.ui.screens.home.sheet.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.utils.config :as config]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn add-new-view []
  [react/view
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/start-new-chat)
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/one-on-one-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-chat])}]
   (when config/group-chat-enabled?
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/start-group-chat)
       :accessibility-label :start-group-chat-button
       :icon                :main-icons/group-chat
       :on-press            #(hide-sheet-and-dispatch [:contact.ui/start-group-chat-pressed])}])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/new-public-group-chat)
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-public-chat])}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/invite-friends)
     :accessibility-label :chats-menu-invite-friends-button
     :icon                :main-icons/share
     :on-press            (fn []
                            (re-frame/dispatch [:bottom-sheet/hide])
                            ;; https://github.com/facebook/react-native/pull/26839
                            (js/setTimeout
                             #(list-selection/open-share {:message (i18n/label :t/get-status-at)})
                             250))}]])

(def add-new
  {:content add-new-view})

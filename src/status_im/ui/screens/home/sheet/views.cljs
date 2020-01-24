(ns status-im.ui.screens.home.sheet.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.utils.config :as config]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn add-new-view []
  [react/view
   [list-item/list-item
    {:theme               :action
     :title               :t/start-new-chat
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/one-on-one-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-chat])}]
   (when config/group-chat-enabled?
     [list-item/list-item
      {:theme               :action
       :title               :t/start-group-chat
       :accessibility-label :start-group-chat-button
       :icon                :main-icons/group-chat
       :on-press            #(hide-sheet-and-dispatch [:contact.ui/start-group-chat-pressed])}])
   [list-item/list-item
    {:theme               :action
     :title               :t/new-public-group-chat
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-public-chat])}]
   [list-item/list-item
    {:theme               :action
     :title               :t/invite-friends
     :accessibility-label :chats-menu-invite-friends-button
     :icon                :main-icons/share
     :on-press            #(do
                             (re-frame/dispatch [:bottom-sheet/hide-sheet])
                             (list-selection/open-share {:message (i18n/label :t/get-status-at)}))}]])

(def add-new
  {:content add-new-view})

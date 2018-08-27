(ns status-im.ui.components.desktop.tabs
  (:require [re-frame.core :as re-frame]
            status-im.ui.components.desktop.events
            [status-im.ui.components.icons.vector-icons :as icons]
            [taoensso.timbre :as log]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.main-tabs.styles :as tabs.styles])
  (:require-macros [status-im.utils.views :as views]))

;;TODO copy-pate with minimum modifications of status-react tabs

(def tabs-list-data
  [{:view-id :home
    :content {:title         "Home"
              :icon-inactive :icons/home
              :icon-active   :icons/home-active}
    :count-subscription  :get-chats-unread-messages-number}
   #_{:view-id :wallet
      :content {:title         "Wallet"
                :icon-inactive :icons/wallet
                :icon-active   :icons/wallet-active}}
   {:view-id :profile
    :content {:title         "Profile"
              :icon-inactive :icons/profile
              :icon-active   :icons/profile-active}
    :count-subscription  :get-profile-unread-messages-number}])

(defn- counter [cnt]
  (let [[unviewed-messages-label large?] (if (< 9 cnt)
                                           ["9+" true]
                                           [cnt false])]
    [react/view {:style tabs.styles/unread-messages-icon}
     [react/text {:style (tabs.styles/unread-messages-text large?)} unviewed-messages-label]]))

(defn- tab-content [{:keys [title icon-active icon-inactive]}]
  (fn [active? cnt]
    [react/view {:style tabs.styles/tab-container}
     (let [icon (if active? icon-active icon-inactive)]
       [react/view
        [icons/icon icon {:style {:tint-color (if active? colors/blue colors/gray-icon)}}]])
     [react/view
      [react/text {:style (tabs.styles/tab-title active?)}
       title]]
     (when (pos? cnt)
       [counter cnt])]))

(def tabs-list-indexed (map-indexed vector (map #(update % :content tab-content) tabs-list-data)))

(views/defview tab [index content view-id active? count-subscription]
  (views/letsubs [cnt [count-subscription]]
    [react/touchable-highlight {:style    (merge tabs.styles/tab-container {:flex 1})
                                :disabled active?
                                :on-press #(re-frame/dispatch [:show-desktop-tab view-id])}
     [react/view
      [content active? (if (= view-id :home) cnt nil)]]]))

(views/defview main-tabs []
  (views/letsubs [current-tab [:get-in [:desktop/desktop :tab-view-id]]]
    [react/view
     [react/view {:style tabs.styles/tabs-container}
      (for [[index {:keys [content view-id count-subscription]}] tabs-list-indexed]
        ^{:key index} [tab index content view-id (= current-tab view-id) count-subscription])]]))

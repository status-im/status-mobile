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
              :icon :main-icons/home}
    :count-subscription  :chats/unread-messages-number}
   #_{:view-id :wallet
      :content {:title         "Wallet"
                :icon :main-icons/wallet}}
   {:view-id :profile
    :content {:title         "Profile"
              :icon :main-icons/user-profile}
    :count-subscription  :get-profile-unread-messages-number}])

(defn- tab-content [{:keys [title icon]}]
  (fn [active? cnt]
    [react/view {:style tabs.styles/tab-container}
     [react/view
      [icons/icon icon {:style {:tint-color (if active? colors/blue colors/black)}}]]
     [react/view
      [react/text {:style (tabs.styles/tab-title active?)}
       title]]]))

(def tabs-list-indexed (map-indexed vector (map #(update % :content tab-content) tabs-list-data)))

(views/defview tab [index content view-id active? count-subscription]
  (views/letsubs [cnt [count-subscription]]
    [react/touchable-highlight {:style    (merge tabs.styles/tab-container {:flex 1})
                                :disabled active?
                                :on-press #(re-frame/dispatch [:show-desktop-tab view-id])}
     [react/view
      [content active? cnt]]]))

(views/defview main-tabs []
  (views/letsubs [current-tab [:get-in [:desktop/desktop :tab-view-id]]]
    [react/view
     [react/view {:style tabs.styles/tabs-container}
      (for [[index {:keys [content view-id count-subscription]}] tabs-list-indexed]
        ^{:key index}
        [tab index content view-id (= current-tab view-id) count-subscription])]]))

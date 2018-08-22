(ns status-im.ui.screens.main-tabs.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar.view]
            [status-im.ui.components.styles :as common.styles]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.wallet.main.views :as wallet.main]
            [status-im.ui.screens.main-tabs.styles :as styles]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.components.common.common :as components.common]))

(def tabs-list-data
  [{:view-id             :home
    :content             {:title         (i18n/label :t/home)
                          :icon-inactive :icons/home
                          :icon-active   :icons/home-active}
    :count-subscription  :get-chats-unread-messages-number
    :accessibility-label :home-tab-button}
   {:view-id             :wallet
    :content             {:title         (i18n/label :t/wallet)
                          :icon-inactive :icons/wallet
                          :icon-active   :icons/wallet-active}
    :count-subscription  :get-wallet-unread-messages-number
    :accessibility-label :wallet-tab-button}
   {:view-id             :my-profile
    :content             {:title         (i18n/label :t/profile)
                          :icon-inactive :icons/profile
                          :icon-active   :icons/profile-active}
    :count-subscription  :get-profile-unread-messages-number
    :accessibility-label :profile-tab-button}])

(defn- tab-content [{:keys [title icon-active icon-inactive]}]
  (fn [active? count]
    [react/view {:style styles/tab-container}
     (let [icon (if active? icon-active icon-inactive)]
       [react/view
        [vector-icons/icon icon (styles/tab-icon active?)]])
     [react/view
      [react/text {:style (styles/tab-title active?)}
       title]]
     (when (pos? count)
       [react/view styles/counter-container
        [react/view styles/counter
         [components.common/counter count]]])]))

(def tabs-list (map #(update % :content tab-content) tabs-list-data))

(views/defview tab [view-id content active? accessibility-label count-subscription]
  (views/letsubs [count [count-subscription]]
    [react/touchable-highlight
     (cond-> {:style    common.styles/flex
              :disabled active?
              :on-press #(re-frame/dispatch [:navigate-to-tab view-id])}
       accessibility-label
       (assoc :accessibility-label accessibility-label))
     [react/view
      [content active? count]]]))

(defn tabs [current-view-id]
  [react/view {:style styles/tabs-container}
   (for [{:keys [content view-id accessibility-label count-subscription]} tabs-list]
     ^{:key view-id} [tab view-id content (= view-id current-view-id) accessibility-label count-subscription])])

(views/defview main-container [view-id]
  (views/letsubs
    [tab-bar-visible? [:tab-bar-visible?]]
    ;; :should-component-update is called only when props are changed,
    ;; that's why view-id is passed as a prop here. main-tabs component will be
    ;; rendered while next screen from stack navigator is shown, so we have
    ;; to prevent re-rendering to avoid no clause exception in case form
    {:should-component-update
     (fn [_ _ [_ new-view-id]]
       (contains? #{:home :wallet :my-profile} new-view-id))}
    [react/view common.styles/main-container
     (case view-id
       :home [home/home]
       :wallet [wallet.main/wallet]
       :my-profile [profile.user/my-profile]
       nil)

     (when tab-bar-visible?
       [tabs view-id])]))

(defn main-tabs [view-id]
  [react/view common.styles/flex
   [status-bar.view/status-bar
    {:type (if (= view-id :wallet) :wallet-tab :main)}]
   [main-container view-id]])

(defn get-main-tab [view-id]
  (fn []
    [main-tabs view-id]))

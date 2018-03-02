(ns status-im.ui.screens.main-tabs.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.views :as comp.views]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.wallet.views :as wallet]
            [status-im.ui.screens.main-tabs.styles :as styles]
            [status-im.ui.screens.profile.user.views :as profile.user]))

(def tabs-list-data
  [{:view-id :home
    :content {:title         (i18n/label :t/home)
              :icon-inactive :icons/home
              :icon-active   :icons/home-active}}
   {:view-id :wallet
    :content {:title         (i18n/label :t/wallet)
              :icon-inactive :icons/wallet
              :icon-active   :icons/wallet-active}}
   {:view-id :my-profile
    :content {:title         (i18n/label :t/profile)
              :icon-inactive :icons/profile
              :icon-active   :icons/profile-active}}])

(defn- tab-content [{:keys [title icon-active icon-inactive]}]
  (fn [active?]
    [react/view {:style styles/tab-container}
     (let [icon (if active? icon-active icon-inactive)]
       [react/view
        [vector-icons/icon icon (styles/tab-icon active?)]])
     [react/view
      [react/text {:style (styles/tab-title active?)}
       title]]]))

(def tabs-list (map #(update % :content tab-content) tabs-list-data))

(defn- tab [view-id content active?]
  [react/touchable-highlight {:style    common.styles/flex
                              :disabled active?
                              :on-press #(re-frame/dispatch [:navigate-to-tab view-id])}
   [react/view
    [content active?]]])

(defn tabs [current-view-id]
  [react/view {:style styles/tabs-container}
   (for [{:keys [content view-id]} tabs-list]
     ^{:key view-id} [tab view-id content (= view-id current-view-id)])])

(defn main-tabs-content [view-id]
  [react/view components.styles/main-container

   [comp.views/with-activity-indicator
    {:enabled? (= :home view-id)
     :preview  [react/view {}]}
    [react/navigation-wrapper
     {:component    home/home
      :views        :home
      :current-view view-id}]]

   [comp.views/with-activity-indicator
    {:enabled? (= :wallet view-id)
     :preview  [react/view {}]}
    [react/navigation-wrapper
     {:component    wallet/wallet
      :views        :wallet
      :current-view view-id}]]

   [comp.views/with-activity-indicator
    {:enabled? (= :my-profile view-id)
     :preview  [react/view {}]}
    [react/navigation-wrapper
     {:component    profile.user/my-profile
      :views        :my-profile
      :current-view view-id}]]
   [tabs view-id]])

(views/defview ^:theme main-tabs-theme-default [view-id]
  [main-tabs-content view-id])

(views/defview ^{:theme :wallet-home} main-tabs-theme-wallet [view-id]
  [main-tabs-content view-id])

(views/defview main-tabs []
  (views/letsubs [view-id [:get :view-id]]
    (let [theme-wrapper (if (= view-id :wallet)
                          main-tabs-theme-wallet
                          main-tabs-theme-default)]
      [theme-wrapper view-id])))

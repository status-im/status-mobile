(ns status-im.ui.screens.profile.user.list-items
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.styles :as styles]
            [utils.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [quo.components.text :as text]
            [quo2.foundations.colors :as fcolors]))

(def separator
  [quo/separator])

(defn list-item
  [{:keys [icon title title-color accessibility-label chevron accessory on-press title-style]}]
  [quo/list-item
   {:icon                icon
    :icon-bg-color       "transparent"
    :icon-size           32
    :icon-color          (:text-03 @colors/theme)
    :title               title
    :title-color         title-color
    :title-style         (merge {:padding-horizontal 0} title-style)
    :accessibility-label accessibility-label
    :chevron             chevron
    :size                50
    :container-style     styles/list-item-container
    :accessory           accessory
    :on-press            on-press}])

(def personal-info-group
  [react/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :main-icons/edit-profile
     :title               (i18n/label :t/edit-profile)
     :accessibility-label :edit-profile-settings-button
     :chevron             true
     ;;  TODO: No edit profile action
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :edit])
    }]
   separator
   [list-item
    {:icon                :main-icons/key-profile
     :title               (i18n/label :t/password)
     :accessibility-label :password-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :reset-password])}]])

(defn activity-settings-group
  [{:keys [mnemonic]}]

  [react/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :main-icons/message-profile
     :title               (i18n/label :t/messages)
     :accessibility-label :message-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/counter {:size 22} 1])
     ;;  TODO: Chat home doesn't look great
     :on-press            #(re-frame/dispatch [:navigate-to :chat-home])}]
   separator
   [list-item
    {:icon                :main-icons/communities
     :title               (i18n/label :t/communities)
     :accessibility-label :communities-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/counter {:size 22} 1])
     ;;  TODO: Community home doesn't work correctly
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :community-home])
    }]
   separator
   [list-item
    {:icon                :main-icons/wallet-profile
     :title               (i18n/label :t/wallet)
     :accessibility-label :wallet-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/counter {:size 22} 1])
     ;;  TODO: Wallet screen needs back button
     :on-press            #(re-frame/dispatch [:navigate-to :wallet])}]
   separator
   [list-item
    {:icon                :main-icons/dapps
     :title               (i18n/label :t/dapps)
     :accessibility-label :dapps-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/counter {:size 22} 1])
     :on-press            #(re-frame/dispatch [:browser.ui/open-url "https://dap.ps"])}]
   separator
   [list-item
    {:icon                :main-icons/browser
     :title               (i18n/label :t/browser)
     :accessibility-label :browser-settings-button
     :chevron             true
     ;;  TODO: Needs back button?
     :on-press            #(re-frame/dispatch [:navigate-to :browser])}]
   separator
   [list-item
    {:icon                :main-icons/keycard-profile
     :title               (i18n/label :t/keycard)
     :accessibility-label :keycard-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/counter {:size 22} 1])
     :on-press            #(re-frame/dispatch [:navigate-to :keycard-settings])}]])

(defn device-settings-group
  [{:keys [local-pairing-mode-enabled?]}]
  [react/view
   {:style styles/rounded-view}
   (when local-pairing-mode-enabled?
     [react/view
      [list-item
       {:icon                :main-icons/syncing
        :title               (i18n/label :t/syncing)
        :accessibility-label :syncing-settings-button
        :chevron             true
        :on-press            #(re-frame/dispatch [:navigate-to :settings-syncing])}]
      separator])
   [list-item
    {:icon                :main-icons/notification
     :title               (i18n/label :t/notifications)
     :accessibility-label :notifications-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :notifications])}]
   separator
   [list-item
    {:icon                :main-icons/appearance
     :title               (i18n/label :t/appearance)
     :accessibility-label :appearance-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :appearance])}]
   separator
   [list-item
    {:icon                :main-icons/globe
     :title               (i18n/label :t/language-and-currency)
     :accessibility-label :language-settings-button
     :chevron             true
     ;;  TODO: No language screen
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :appearance])
    }]])

(def advanced-settings-group
  [react/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :main-icons/mobile-profile
     :title               (i18n/label :t/data-usage)
     :accessibility-label :data-usage-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :mobile-network-settings])}]
   separator
   [list-item
    {:icon                :main-icons/settings
     :title               (i18n/label :t/advanced)
     :accessibility-label :advanced-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :advanced-settings])}]])

(def about-help-group
  [react/view
   {:style styles/rounded-view}
   [list-item
    {:title               (i18n/label :t/about-app)
     :accessibility-label :about-button
     :chevron             true
     :title-style         {:margin-horizontal 16}
     :on-press            #(re-frame/dispatch [:navigate-to :about-app])}]
   separator
   [list-item
    {:title               (i18n/label :t/status-help)
     :accessibility-label :help-button
     :chevron             false
     :accessory           [icons/icon :main-icons/external {:color colors/gray}]
     :title-style         {:margin-horizontal 16}
     :on-press            #(re-frame/dispatch [:navigate-to :help-center])}]])

(def logout-item
  [react/view
   {:style styles/logout-container
    :on-press
    #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])
    :accessibility-label :log-out-button}
   [icons/icon :main-icons/log-out {:color fcolors/danger-60}]
   [text/text
    {:style {:color       fcolors/danger-60
             :margin-left 8}}
    (i18n/label :t/logout)]])

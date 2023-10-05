(ns status-im2.contexts.profile.profiles.list-items
  (:require [quo2.core :as quo2]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.ui.components.badge :as components.common]
            [react-native.core :as rn]
            [status-im2.contexts.profile.profiles.style :as styles]
            [utils.i18n :as i18n]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as fcolors]))

(def separator
  [quo2/separator])

(defn list-item
  [{:keys [icon title accessibility-label chevron accessory on-press title-style]}]
  [quo/list-item
   {:icon                icon
    :icon-bg-color       "transparent"
    :icon-size           32
    :icon-color          (:text-03 @colors/theme)
    :title               title
    :title-color         (:text-01 @colors/theme)
    :title-style         (merge {:padding-horizontal 0} title-style)
    :accessibility-label accessibility-label
    :chevron             chevron
    :size                :small
    :container-style     styles/list-item-container
    :accessory           accessory
    :on-press            on-press}])

(def personal-info-group
  [rn/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :i/edit-profile
     :title               (i18n/label :t/edit-profile)
     :accessibility-label :edit-profile-settings-button
     :chevron             true
     ;;  TODO: No edit profile action
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :edit])
     }]
   separator
   [list-item
    {:icon                :i/key-profile
     :title               (i18n/label :t/password)
     :accessibility-label :password-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :reset-password])}]])

(defn activity-settings-group
  [{:keys [mnemonic]}]

  [rn/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :i/message-profile
     :title               (i18n/label :t/messages)
     :accessibility-label :message-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/badge {:size 22} 1])
      ;; TODO: Chat home doesn't look great
      ;; :on-press            #(re-frame/dispatch [:navigate-to :chat])
     }]
   separator
   [list-item
    {:icon                :i/communities
     :title               (i18n/label :t/communities)
     :accessibility-label :communities-settings-button
     :chevron             true
     :accessory           (when mnemonic
                            [components.common/badge {:size 22} 1])
     ;;  TODO: Community home doesn't work correctly
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :community-home])
     }]
   separator
   [list-item
    {:icon                :i/wallet-profile
     :title               (i18n/label :t/wallet)
     :accessibility-label :wallet-settings-button
     :chevron             true
     ;;  TODO: Wallet screen needs back button
     :on-press            #(re-frame/dispatch [:navigate-to :wallet])}]
   separator
   [list-item
    {:icon                :i/dapps
     :title               (i18n/label :t/dapps)
     :accessibility-label :dapps-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:browser.ui/open-url "https://dap.ps"])}]
   separator
   [list-item
    {:icon                :i/browser
     :title               (i18n/label :t/browser)
     :accessibility-label :browser-settings-button
     :chevron             true
     ;;  TODO: Needs back button?
     :on-press            #(re-frame/dispatch [:navigate-to :browser])}]
   separator
   [list-item
    {:icon                :i/keycard-profile
     :title               (i18n/label :t/keycard)
     :accessibility-label :keycard-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :keycard-settings])}]])

(defn device-settings-group
  [{:keys [local-pairing-mode-enabled?]}]
  [rn/view
   {:style styles/rounded-view}
   (when local-pairing-mode-enabled?
     [rn/view
      [list-item
       {:icon                :i/syncing
        :title               (i18n/label :t/syncing)
        :accessibility-label :syncing-settings-button
        :chevron             true
        :on-press            #(re-frame/dispatch [:navigate-to :settings-syncing])}]
      separator])
   [list-item
    {:icon                :i/notification
     :title               (i18n/label :t/notifications)
     :accessibility-label :notifications-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :notifications])}]
   separator
   [list-item
    {:icon                :i/appearance
     :title               (i18n/label :t/appearance)
     :accessibility-label :appearance-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :appearance])}]
   separator
   [list-item
    {:icon                :i/globe
     :title               (i18n/label :t/language-and-currency)
     :accessibility-label :language-settings-button
     :chevron             true
     ;;  TODO: No language screen
     ;;  :on-press            #(re-frame/dispatch [:navigate-to :appearance])
     }]])

(def advanced-settings-group
  [rn/view
   {:style styles/rounded-view}
   [list-item
    {:icon                :i/mobile-profile
     :title               (i18n/label :t/data-usage)
     :accessibility-label :data-usage-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :mobile-network-settings])}]
   separator
   [list-item
    {:icon                :i/settings
     :title               (i18n/label :t/advanced)
     :accessibility-label :advanced-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:navigate-to :advanced-settings])}]])

(def about-help-group
  [rn/view
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
     :accessory           [icons/icon :i/external {:color colors/gray}]
     :title-style         {:margin-horizontal 16}
     :on-press            #(re-frame/dispatch [:navigate-to :help-center])}]])

(def logout-item
  [rn/view
   {:style styles/logout-container
    :on-press
    #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])
    :accessibility-label :log-out-button}
   [icons/icon :i/log-out {:color fcolors/danger-60}]
   [quo2/text
    {:style {:color       fcolors/danger-60
             :margin-left 8}}
    (i18n/label :t/logout)]])

(ns status-im.ui.screens.profile.user.views-v2
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.components :as components]
            [status-im.ui.screens.profile.user.list-items :as profile-list-item]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im2.common.qr-code-viewer.view :as qr-code-viewer]
            [status-im2.config :as config]
            [utils.i18n :as i18n])
  (:require-macros [status-im.utils.views :as views]))


(views/defview share-chat-key
  []
  (views/letsubs [{:keys [address ens-name]} [:popover/popover]
                  width                      (reagent/atom nil)]
                 (let [link (universal-links/generate-link :user :external (or ens-name address))]
                   [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
                    [react/view {:style {:padding-top 16 :padding-horizontal 16}}
                     (when @width
                       [qr-code-viewer/qr-code-view (- @width 32) address])
                     (when ens-name
                       [react/view
                        [copyable-text/copyable-text-view
                         {:label           :t/ens-username
                          :container-style {:margin-top 12 :margin-bottom 4}
                          :copied-text     ens-name}
                         [quo/text
                          {:monospace           true
                           :accessibility-label :ens-username}
                          ens-name]]
                        [react/view
                         {:height            1
                          :margin-top        12
                          :margin-horizontal -16
                          :background-color  colors/gray-lighter}]])
                     [copyable-text/copyable-text-view
                      {:label           :t/chat-key
                       :container-style {:margin-top 12 :margin-bottom 4}
                       :copied-text     address}
                      [quo/text
                       {:number-of-lines     1
                        :ellipsize-mode      :middle
                        :accessibility-label :chat-key
                        :monospace           true}
                       address]]]
                    [react/view styles/share-link-button
                     [quo/button
                      {:on-press            (fn []
                                              (re-frame/dispatch [:hide-popover])
                                              (js/setTimeout
                                               #(list-selection/open-share {:message link})
                                               250))
                       :accessibility-label :share-my-contact-code-button}
                      (i18n/label :t/share-link)]]])))

(defn items
  [{:keys [mnemonic]}]
  [react/view
   {:style styles/items-container}

   profile-list-item/personal-info-group

   [profile-list-item/activity-settings-group
    {:mnemonic mnemonic}]

   [profile-list-item/device-settings-group
    {:local-pairing-mode-enabled? config/local-pairing-mode-enabled?}]

   profile-list-item/advanced-settings-group

   profile-list-item/about-help-group

   profile-list-item/logout-item])

(defn my-profile
  []
  (fn []
    (let [{:keys [public-key
                  compressed-key
                  ens-verified
                  emoji-hash
                  mnemonic
                  ens-name address
                  key-uid]
           :as   account}
          @(re-frame/subscribe [:profile/multiaccount])
          customization-color (or (:color @(re-frame/subscribe [:onboarding-2/profile]))
                                  @(re-frame/subscribe [:profile/customization-color key-uid]))
          on-share #(re-frame/dispatch [:navigate-to :share-view])
          has-picture @(re-frame/subscribe [:profile/has-picture])
          link (universal-links/generate-link :user :external (or ens-name address))]

      [react/view {:flex 1 :style styles/container-style}

       components/top-background-view

       [components/fixed-toolbar
        {:on-share #(list-selection/open-share {:message link})
         :on-close #(re-frame/dispatch [:navigate-back])
        ;;  TODO: No action for switch account
         :on-switch-profile nil
         :on-show-qr on-share}]

       [react/scroll-view
        [components/user-info
         {:on-share on-share
          :has-picture has-picture
          :customization-color customization-color
          :account account
          :emoji-hash (string/join emoji-hash)
          :ens-verified ens-verified
          :public-key public-key
          :compressed-key compressed-key}]
        [items
         {:mnemonic mnemonic}]]])))

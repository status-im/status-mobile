(ns status-im.ui.screens.profile.user.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            [quo.core :as quo]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ui.components.profile-header.view :as profile-header])
  (:require-macros [status-im.utils.views :as views]))

(views/defview share-chat-key []
  (views/letsubs [{:keys [address ens-name]}     [:popover/popover]
                  width                          (reagent/atom nil)]
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
            [react/nested-text
             {:style               {:line-height 22 :font-size 15
                                    :font-family "monospace"}
              :accessibility-label :ens-username}
             ens-name]]
           [react/view {:height           1 :margin-top 12 :margin-horizontal -16
                        :background-color colors/gray-lighter}]])
        [copyable-text/copyable-text-view
         {:label           :t/chat-key
          :container-style {:margin-top 12 :margin-bottom 4}
          :copied-text     address}
         [react/text {:number-of-lines     1
                      :ellipsize-mode      :middle
                      :accessibility-label :chat-key
                      :style               {:line-height 22 :font-size 15
                                            :font-family "monospace"}}
          address]]]
       [react/view styles/share-link-button
        ;;TODO implement icon support
        [button/button
         {:on-press            #(list-selection/open-share {:message link})
          :label               :t/share-link
                                        ;:icon                :main-icons/link
          :accessibility-label :share-my-contact-code-button}]]])))

(defn tribute-to-talk-item
  [opts]
  [list.views/big-list-item
   (merge {:text                (i18n/label :t/tribute-to-talk)
           :accessibility-label :notifications-button
           :action-fn           #(re-frame/dispatch
                                  [:tribute-to-talk.ui/menu-item-pressed])}
          opts)])

(defn- flat-list-content
  [preferred-name registrar tribute-to-talk
   active-contacts-count mnemonic
   keycard-account? notifications-enabled?]
  [(cond-> {:title                (or (when registrar preferred-name)
                                      :t/ens-usernames)
            :subtitle             (if registrar
                                    (if preferred-name
                                      :t/ens-your-your-name
                                      :t/ens-usernames-details)
                                    :t/ens-network-restriction)
            :subtitle-max-lines   (if registrar
                                    (if preferred-name 1 2)
                                    1)
            :accessibility-label  :ens-button
            :container-margin-top 8
            :disabled?            (not registrar)
            :accessories          [:chevron]
            :icon                 :main-icons/username}
     registrar
     (assoc :on-press #(re-frame/dispatch [:navigate-to :ens-main registrar])))
   ;; TODO replace this with list-item config map
   ;; left it as it is because not sure how to enable it for testing
   (when tribute-to-talk [tribute-to-talk-item tribute-to-talk])
   {:title               :t/contacts
    :icon                :main-icons/in-contacts
    :accessibility-label :contacts-button
    :accessories         [(if (pos? active-contacts-count)
                            (str active-contacts-count)
                            :t/none)
                          :chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :contacts-list])}
   {:type                 :section-header
    :accessibility-label  :settings-section
    :container-margin-top 16
    :title                :t/settings}
   {:icon                :main-icons/security
    :title               :t/privacy-and-security
    :accessibility-label :privacy-and-security-settings-button
    :accessories
    [(when mnemonic
       [components.common/counter {:size 22} 1]) :chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :privacy-and-security])}
   {:icon                :main-icons/appearance
    :title               :t/appearance
    :accessibility-label :appearance-settings-button
    :accessories         [:chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :appearance])}
   (when (and platform/android?
              config/local-notifications?)
     {:icon                :main-icons/notification
      :title               :t/notifications
      :accessibility-label :notifications-button
      :on-press
      #(re-frame/dispatch
        [:multiaccounts.ui/notifications-switched (not notifications-enabled?)])
      :accessories
      [[react/switch
        {:track-color #js {:true colors/blue :false nil}
         :value       notifications-enabled?
         :on-value-change
         #(re-frame/dispatch
           [:multiaccounts.ui/notifications-switched
            (not notifications-enabled?)])
         :disabled    false}]]})
   {:icon                :main-icons/mobile
    :title               :t/sync-settings
    :accessibility-label :sync-settings-button
    :accessories         [:chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :sync-settings])}
   (when (and (or platform/android?
                  config/keycard-test-menu-enabled?)
              config/hardwallet-enabled?
              keycard-account?)
     {:icon                :main-icons/keycard
      :title               :t/keycard
      :accessibility-label :keycard-button
      :accessories         [:chevron]
      :on-press            #(re-frame/dispatch [:navigate-to :keycard-settings])})
   {:icon                :main-icons/settings-advanced
    :title               :t/advanced
    :accessibility-label :advanced-button
    :accessories         [:chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :advanced-settings])}
   {:icon                :main-icons/help
    :title               :t/need-help
    :accessibility-label :help-button
    :accessories         [:chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :help-center])}
   {:icon                :main-icons/info
    :title               :t/about-app
    :accessibility-label :about-button
    :accessories         [:chevron]
    :on-press            #(re-frame/dispatch [:navigate-to :about-app])}
   {:icon                    :main-icons/log-out
    :title                   :t/sign-out
    :accessibility-label     :log-out-button
    :container-margin-top    24
    :container-margin-bottom 24
    :theme                   :action-destructive
    :on-press
    #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])}])

(defn content []
  (let [{:keys [preferred-name
                mnemonic
                keycard-pairing
                notifications-enabled?]}
        @(re-frame/subscribe [:multiaccount])

        active-contacts-count @(re-frame/subscribe [:contacts/active-count])
        tribute-to-talk       @(re-frame/subscribe [:tribute-to-talk/profile])
        registrar             @(re-frame/subscribe [:ens.stateofus/registrar])]
    [react/view
     (for [item (flat-list-content
                 preferred-name registrar tribute-to-talk
                 active-contacts-count mnemonic
                 keycard-pairing notifications-enabled?)]
       ^{:key (str "item" (:title item))}
       [list.views/flat-list-generic-render-fn item])]))

(defn my-profile []
  (let [{:keys [public-key ens-verified preferred-name]
         :as   account} @(re-frame/subscribe [:multiaccount])
        on-share        #(re-frame/dispatch [:show-popover
                                             {:view     :share-chat-key
                                              :address  public-key
                                              :ens-name preferred-name}])]
    [react/view {:style {:flex 1}}
     [quo/animated-header
      {:right-accessories [{:icon     :main-icons/share
                            :on-press on-share}]
       :use-insets        true
       :extended-header   (profile-header/extended-header
                           {:on-press on-share
                            :title    (multiaccounts/displayed-name account)
                            :photo    (multiaccounts/displayed-photo account)
                            :subtitle (if (and ens-verified public-key)
                                        (gfy/generate-gfy public-key)
                                        public-key)})}
      [content]]]))

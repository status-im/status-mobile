(ns status-im.ui.screens.profile.user.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.large-toolbar.view :as large-toolbar]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ui.components.animation :as animation])
  (:require-macros [status-im.utils.views :as views]))

(views/defview share-chat-key []
  (views/letsubs [{:keys [address ens-name]}     [:popover/popover]
                  width                          (reagent/atom nil)]
    (let [link (universal-links/generate-link :user :external (or ens-name address))]
      [react/view {:on-layout #(reset! width (-> % .-nativeEvent .-layout .-width))}
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

(defn- header [{:keys [photo-path] :as account} photo-added?]
  [profile.components/profile-header
   {:contact                account
    ;;set to true if we want to re-enable custom icon
    :allow-icon-change?     false
    :include-remove-action?  photo-added?}])

(defn- header-in-toolbar [account]
  (let [displayed-name (multiaccounts/displayed-name account)]
    [react/view {:flex           1
                 :flex-direction :row
                 :align-items    :center
                 :align-self     :stretch}
     ;;TODO this should be done in a subscription
     [photos/photo (multiaccounts/displayed-photo account) {:size 40}]
     [react/text {:style {:typography   :title-bold
                          :line-height  21
                          :margin-right 40
                          :margin-left  16
                          :text-align   :left}}
      displayed-name]]))

(defn- toolbar-action-items [public-key ens-name]
  [toolbar/actions
   [{:icon      :main-icons/share
     :icon-opts {:width  24
                 :height 24}
     :handler   #(re-frame/dispatch [:show-popover
                                     {:view :share-chat-key
                                      :address public-key
                                      :ens-name ens-name}])}]])

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
   (when (and platform/android?
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
   {:icon                    :main-icons/log_out
    :title                   :t/sign-out
    :accessibility-label     :log-out-button
    :container-margin-top    24
    :container-margin-bottom 24
    :theme                   :action-destructive
    :on-press
    #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])}])

(defn minimized-toolbar-handler [anim-opacity]
  (let [{:keys [public-key preferred-name]
         :as   multiaccount}         @(re-frame/subscribe [:multiaccount])]
    [large-toolbar/minimized-toolbar-handler
     (header-in-toolbar multiaccount)
     nil
     (toolbar-action-items public-key preferred-name)
     anim-opacity]))

(defn content-with-header [list-ref scroll-y]
  (let [{:keys [preferred-name
                mnemonic
                keycard-pairing
                notifications-enabled?]
         :as   multiaccount}  @(re-frame/subscribe [:multiaccount])
        active-contacts-count @(re-frame/subscribe [:contacts/active-count])
        tribute-to-talk       @(re-frame/subscribe [:tribute-to-talk/profile])
        registrar             @(re-frame/subscribe [:ens.stateofus/registrar])
        photo-added?          @(re-frame/subscribe [:profile/photo-added?])]
    [large-toolbar/flat-list-with-header-handler
     (header multiaccount photo-added?)
     (flat-list-content
      preferred-name registrar tribute-to-talk
      active-contacts-count mnemonic
      keycard-pairing notifications-enabled?)
     list-ref
     scroll-y]))

(defn my-profile []
  (let [list-ref     (reagent/atom nil)
        anim-opacity (animation/create-value 0)
        scroll-y     (animation/create-value 0)]
    (large-toolbar/add-listener anim-opacity scroll-y)
    (fn []
      [react/view {:style {:flex 1}}
       [minimized-toolbar-handler anim-opacity]
       [content-with-header list-ref scroll-y]])))

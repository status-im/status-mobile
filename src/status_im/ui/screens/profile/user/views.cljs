(ns status-im.ui.screens.profile.user.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.tabs :as tabs]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.contacts-list.views :as contacts-list]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.profile.status :as my-status]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.chat.views :as chat.views])
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
            [quo/text
             {:monospace           true
              :accessibility-label :ens-username}
             ens-name]]
           [react/view {:height           1 :margin-top 12 :margin-horizontal -16
                        :background-color colors/gray-lighter}]])
        [copyable-text/copyable-text-view
         {:label           :t/chat-key
          :container-style {:margin-top 12 :margin-bottom 4}
          :copied-text     address}
         [quo/text {:number-of-lines     1
                    :ellipsize-mode      :middle
                    :accessibility-label :chat-key
                    :monospace           true}
          address]]]
       [react/view styles/share-link-button
        [quo/button
         {:on-press            #(list-selection/open-share {:message link})
          :accessibility-label :share-my-contact-code-button}
         (i18n/label :t/share-link)]]])))

(def state (reagent/atom {:tab :status}))

(defn tabs []
  (let [{:keys [tab]} @state]
    [react/view {:flex-direction :row :padding-horizontal 4 :margin-top 16}
     [tabs/tab-title state :status (i18n/label :t/my-status) (= tab :status)]
     [tabs/tab-title state :contacts (i18n/label :t/contacts) (= tab :contacts)]
     [tabs/tab-title state :settings (i18n/label :t/settings) (= tab :settings)]]))

(defn my-status []
  (let [messages @(re-frame/subscribe [:chats/current-chat-messages-stream])
        no-messages? @(re-frame/subscribe [:chats/current-chat-no-messages?])]
    (if no-messages?
      [react/view {:padding-horizontal 32 :margin-top 32}
       [react/view (styles/descr-container)
        [react/text {:style {:color colors/gray :line-height 22}}
         (i18n/label :t/statuses-my-profile-descr)]]
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :my-status])}
        [react/view {:flex-direction :row :align-items :center :align-self :center
                     :padding        12}
         [react/text {:style {:color colors/blue :margin-right 8}}
          (i18n/label :t/new-status)]
         [icons/icon :main-icons/add-circle {:color colors/blue}]]]]
      [list/flat-list
       {:key-fn                    #(or (:message-id %) (:value %))
        :render-fn                 my-status/render-message
        :data                      messages
        :on-viewable-items-changed chat.views/on-viewable-items-changed
        :on-end-reached            #(re-frame/dispatch [:chat.ui/load-more-messages])
        :on-scroll-to-index-failed #()                      ;;don't remove this
        :footer                    [react/view {:height 68}]}])))

(defn content []
  (let [{:keys [preferred-name
                mnemonic
                keycard-pairing]}
        @(re-frame/subscribe [:multiaccount])
        chain                 @(re-frame/subscribe [:chain-keyword])
        registrar             (stateofus/get-cached-registrar chain)
        {:keys [tab]} @state]
    [:<>
     [tabs]
     [react/view {:height 1 :background-color colors/gray-lighter}]
     (cond
       (= tab :status)
       [my-status]
       (= tab :contacts)
       [contacts-list/contacts-list]
       (= tab :settings)
       [:<>
        [quo/list-item
         (cond-> {:title                (or (when registrar preferred-name)
                                            (i18n/label :t/ens-usernames))
                  :subtitle             (if registrar
                                          (if preferred-name
                                            (i18n/label :t/ens-your-your-name)
                                            (i18n/label :t/ens-usernames-details))
                                          (i18n/label :t/ens-network-restriction))
                  :subtitle-max-lines   (if registrar
                                          (if preferred-name 1 2)
                                          1)
                  :accessibility-label  :ens-button
                  :container-margin-top 8
                  :disabled             (not registrar)
                  :chevron              true
                  :icon                 :main-icons/username}
           registrar
           (assoc :on-press #(re-frame/dispatch [:navigate-to :ens-main registrar])))]
        [quo/list-item
         {:icon                :main-icons/security
          :title               (i18n/label :t/privacy-and-security)
          :accessibility-label :privacy-and-security-settings-button
          :chevron             true
          :accessory           (when mnemonic
                                 [components.common/counter {:size 22} 1])
          :on-press            #(re-frame/dispatch [:navigate-to :privacy-and-security])}]
        (when config/quo-preview-enabled?
          [quo/list-item
           {:icon                :main-icons/appearance
            :title               "Quo Preview"
            :accessibility-label :appearance-settings-button
            :chevron             true
            :on-press            #(re-frame/dispatch [:navigate-to :quo-preview])}])
        [quo/list-item
         {:icon                :main-icons/appearance
          :title               (i18n/label :t/appearance)
          :accessibility-label :appearance-settings-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :appearance])}]
        [quo/list-item
         {:icon                :main-icons/notification
          :title               (i18n/label :t/notifications)
          :accessibility-label :notifications-settings-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :notifications])}]
        [quo/list-item
         {:icon                :main-icons/mobile
          :title               (i18n/label :t/sync-settings)
          :accessibility-label :sync-settings-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :sync-settings])}]
        (when (and (or platform/android?
                       config/keycard-test-menu-enabled?)
                   keycard-pairing)
          [quo/list-item
           {:icon                :main-icons/keycard
            :title               (i18n/label :t/keycard)
            :accessibility-label :keycard-button
            :chevron             true
            :on-press            #(re-frame/dispatch [:navigate-to :keycard-settings])}])
        [quo/list-item
         {:icon                :main-icons/settings-advanced
          :title               (i18n/label :t/advanced)
          :accessibility-label :advanced-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :advanced-settings])}]
        [quo/list-item
         {:icon                :main-icons/help
          :title               (i18n/label :t/need-help)
          :accessibility-label :help-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :help-center])}]
        [quo/list-item
         {:icon                :main-icons/info
          :title               (i18n/label :t/about-app)
          :accessibility-label :about-button
          :chevron             true
          :on-press            #(re-frame/dispatch [:navigate-to :about-app])}]
        [react/view {:padding-vertical 24}
         [quo/list-item
          {:icon                :main-icons/log-out
           :title               (i18n/label :t/sign-out)
           :accessibility-label :log-out-button
           :theme               :negative
           :on-press
           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])}]]])]))

(defn my-profile []
  (fn []
    (let [{:keys [public-key ens-verified preferred-name]
           :as   account} @(re-frame/subscribe [:multiaccount])
          on-share        #(re-frame/dispatch [:show-popover
                                               {:view     :share-chat-key
                                                :address  public-key
                                                :ens-name preferred-name}])
          {:keys [tab]} @state]
      [react/view {:flex 1}
       [quo/animated-header
        {:right-accessories [{:accessibility-label :share-header-button
                              :icon     :main-icons/share
                              :on-press on-share}]
         :use-insets        true
         :extended-header   (profile-header/extended-header
                             {:on-press         on-share
                              :title            (multiaccounts/displayed-name account)
                              :photo            (multiaccounts/displayed-photo account)
                              :monospace        (not ens-verified)
                              :subtitle         (if (and ens-verified public-key)
                                                  (gfy/generate-gfy public-key)
                                                  (utils/get-shortened-address public-key))
                              :bottom-separator false})}
        [content]]
       (when-not (= :settings tab)
         [components.plus-button/plus-button
          {:on-press #(if (= :contacts tab)
                        (re-frame/dispatch [:navigate-to :new-contact])
                        (re-frame/dispatch [:navigate-to :my-status]))}])])))

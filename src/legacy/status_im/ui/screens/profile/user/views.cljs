(ns legacy.status-im.ui.screens.profile.user.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.common.common :as components.common]
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list-selection :as list-selection]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.profile-header.view :as profile-header]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.screens.profile.user.edit-picture :as edit]
    [legacy.status-im.ui.screens.profile.user.styles :as styles]
    [legacy.status-im.ui.screens.profile.visibility-status.views :as visibility-status]
    [legacy.status-im.utils.utils :as utils]
    [quo.components.avatars.user-avatar.style :as user-avatar.style]
    [quo.theme :as theme]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im2.common.qr-codes.view :as qr-codes]
    [status-im2.config :as config]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.ens.stateofus :as stateofus]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview share-chat-key
  []
  (views/letsubs [{:keys [address ens-name]}      [:popover/popover]
                  {:keys [universal-profile-url]} [:profile/profile]
                  width                           (reagent/atom nil)]
    [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      (when @width
        [qr-codes/qr-code
         {:url  address
          :size (- @width 32)}])
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
                                #(list-selection/open-share {:message universal-profile-url})
                                250))
        :accessibility-label :share-my-contact-code-button}
       (i18n/label :t/share-link)]]]))

(defn content
  []
  (let [{:keys [preferred-name
                mnemonic
                keycard-pairing]}
        @(re-frame/subscribe [:profile/profile])
        active-contacts-count @(re-frame/subscribe [:contacts/active-count])
        chain @(re-frame/subscribe [:chain-keyword])
        registrar (when (or (not= chain :goerli) config/test-stateofus?)
                    (stateofus/get-cached-registrar chain))
        local-pairing-mode-enabled? config/local-pairing-mode-enabled?]
    [:<>
     [visibility-status/visibility-status-button
      visibility-status/calculate-button-height-and-dispatch-popover]
     [quo/separator {:style {:margin-top (:tiny spacing/spacing)}}]
     [list.item/list-item
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
     [list.item/list-item
      {:title               (i18n/label :t/contacts)
       :icon                :main-icons/in-contacts
       :accessibility-label :contacts-button
       :accessory           :text
       :accessory-text      (if (pos? active-contacts-count)
                              (str active-contacts-count)
                              (i18n/label :t/none))
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :contacts-list])}]
     [react/view {:padding-top 16}
      [quo/list-header (i18n/label :t/settings)]]
     [list.item/list-item
      {:icon                :main-icons/security
       :title               (i18n/label :t/privacy-and-security)
       :accessibility-label :privacy-and-security-settings-button
       :chevron             true
       :accessory           (when mnemonic
                              [components.common/counter {:size 22} 1])
       :on-press            #(re-frame/dispatch [:navigate-to :privacy-and-security])}]
     (when config/quo-preview-enabled?
       [list.item/list-item
        {:icon                :main-icons/appearance
         :title               "Quo Preview"
         :accessibility-label :appearance-settings-button
         :chevron             true
         :on-press            #(re-frame/dispatch [:navigate-to :quo-preview])}])
     (when config/quo-preview-enabled?
       [list.item/list-item
        {:icon                :main-icons/appearance
         :title               "Status IM Components"
         :accessibility-label :status-im-common-components
         :chevron             true
         :on-press            #(re-frame/dispatch [:navigate-to :status-im-preview])}])
     [list.item/list-item
      {:icon                :main-icons/appearance
       :title               (i18n/label :t/appearance)
       :accessibility-label :appearance-settings-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :appearance])}]
     [list.item/list-item
      {:icon                :main-icons/notification
       :title               (i18n/label :t/notifications)
       :accessibility-label :notifications-settings-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :notifications])}]
     [list.item/list-item
      {:icon                :main-icons/mobile
       :title               (i18n/label :t/sync-settings)
       :accessibility-label :sync-settings-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :sync-settings])}]
     (when keycard-pairing
       [list.item/list-item
        {:icon                :main-icons/keycard
         :title               (i18n/label :t/keycard)
         :accessibility-label :keycard-button
         :chevron             true
         :on-press            #(re-frame/dispatch [:navigate-to :keycard-settings])}])
     [list.item/list-item
      {:icon                :main-icons/settings-advanced
       :title               (i18n/label :t/advanced)
       :accessibility-label :advanced-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :advanced-settings])}]
     [list.item/list-item
      {:icon                :main-icons/help
       :title               (i18n/label :t/need-help)
       :accessibility-label :help-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :help-center])}]
     [list.item/list-item
      {:icon                :main-icons/info
       :title               (i18n/label :t/about-app)
       :accessibility-label :about-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :about-app])}]
     (when local-pairing-mode-enabled?
       [list.item/list-item
        {:icon                :i/mobile
         :title               (i18n/label :t/syncing)
         :accessibility-label :syncing
         :chevron             true
         :on-press            #(re-frame/dispatch [:navigate-to :settings-syncing])}])
     [react/view {:padding-vertical 24}
      [list.item/list-item
       {:icon :main-icons/log-out
        :title (i18n/label :t/sign-out)
        :accessibility-label :log-out-button
        :theme :negative
        :on-press
        #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])}]]]))

(defn my-profile
  []
  (fn []
    (let [{:keys [public-key
                  compressed-key
                  ens-verified
                  preferred-name
                  key-uid]
           :as   profile}
          @(re-frame/subscribe [:profile/profile-with-image])
          customization-color (or (:color @(re-frame/subscribe [:onboarding/profile]))
                                  @(re-frame/subscribe [:profile/customization-color key-uid]))
          on-share #(re-frame/dispatch [:show-popover
                                        {:view     :share-chat-key
                                         :address  (or compressed-key
                                                       public-key)
                                         :ens-name preferred-name}])
          has-picture @(re-frame/subscribe [:profile/has-picture])]
      [react/view {:flex 1}
       [quo/animated-header
        {:right-accessories [{:accessibility-label :share-header-button
                              :icon                :main-icons/share
                              :on-press            on-share}]
         :left-accessories  [{:accessibility-label :close-header-button
                              :icon                :main-icons/close
                              :on-press            #(re-frame/dispatch [:navigate-back])}]
         :use-insets        true
         :extended-header   (profile-header/extended-header
                             {:on-press  on-share
                              :on-edit   #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                                              {:content (edit/bottom-sheet
                                                                         has-picture)}])
                              :color     (user-avatar.style/customization-color customization-color
                                                                                (theme/get-theme))
                              :title     (profile.utils/displayed-name profile)
                              :photo     (profile.utils/photo profile)
                              :monospace (not ens-verified)
                              :subtitle  (utils/get-shortened-address (or compressed-key public-key))})}
        [content]]])))

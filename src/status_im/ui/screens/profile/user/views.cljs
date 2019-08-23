(ns status-im.ui.screens.profile.user.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.large-toolbar :as large-toolbar]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.config :as config]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ethereum.stateofus :as stateofus]))

(defview share-chat-key []
  (letsubs [{:keys [address]}              [:popover/popover]
            window-width                   [:dimensions/window-width]
            {:keys [names preferred-name]} [:ens.main/screen]]
    (let [username (stateofus/username preferred-name)
          qr-width (- window-width 128)
          name     (or username preferred-name)
          link     (universal-links/generate-link :user :external address)]
      [react/view
       [react/view {:style {:padding-top 16 :padding-left 16 :padding-right 16}}
        [qr-code-viewer/qr-code-view qr-width address]
        (when (seq names)
          [react/view
           [copyable-text/copyable-text-view
            {:label       :t/ens-usernames
             :copied-text preferred-name}
            [react/nested-text
             {:style               {:line-height 22 :font-size 15
                                    :font-family "monospace"}
              :accessibility-label :ens-username}
             name
             (when username [{:style {:color colors/gray}} (str "." stateofus/domain)])]]
           [react/view {:height 1 :margin-top 12 :margin-horizontal -16
                        :background-color colors/gray-lighter}]])
        [copyable-text/copyable-text-view
         {:label       :t/chat-key
          :copied-text address}
         [react/text {:number-of-lines     1
                      :ellipsize-mode      :middle
                      :accessibility-label :chat-key
                      :style               {:line-height 22 :font-size 15
                                            :font-family "monospace"}}
          address]]
        [react/view styles/share-link-button
         [button/button-with-icon
          {:on-press            #(list-selection/open-share {:message link})
           :label               (i18n/label :t/share-link)
           :icon                :main-icons/link
           :style               {:height 44 :margin-horizontal 0}
           :accessibility-label :share-my-contact-code-button}]]]])))

(defn- my-profile-settings [{:keys [seed-backed-up? mnemonic]}
                            {:keys [settings]}
                            logged-in?]
  (let [show-backup-seed? (and (not seed-backed-up?) (not (string/blank? mnemonic)))]
    [react/view
     [profile.components/settings-title (i18n/label :t/settings)]
     (when (and config/hardwallet-enabled?
                platform/android?)
       [profile.components/settings-item {:label-kw            :t/status-keycard
                                          :accessibility-label :keycard-button
                                          :action-fn           #(re-frame/dispatch [:profile.ui/keycard-settings-button-pressed])}])
     [profile.components/settings-item {:label-kw            :t/notifications
                                        :accessibility-label :notifications-button
                                        :action-fn           #(.openURL (react/linking) "app-settings://notification/status-im")}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/mobile-network-settings
                                        :accessibility-label :notifications-button
                                        :action-fn            #(re-frame/dispatch [:navigate-to :mobile-network-settings])}]
     (when show-backup-seed?
       [profile.components/settings-item-separator])
     (when show-backup-seed?
       [profile.components/settings-item
        {:label-kw     :t/backup-your-recovery-phrase
         :accessibility-label :back-up-recovery-phrase-button
         :action-fn    #(re-frame/dispatch [:navigate-to :backup-seed])
         :icon-content [components.common/counter {:size 22} 1]}])
     [profile.components/settings-item-separator]
     [profile.components/settings-item
      {:label-kw            :t/devices
       :action-fn           #(re-frame/dispatch [:navigate-to :installations])
       :accessibility-label :pairing-settings-button}]
     [profile.components/settings-item-separator]
     [profile.components/settings-switch-item
      {:label-kw  :t/preview-privacy
       :value     (boolean (:preview-privacy? settings))
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/preview-privacy-mode-switched %])}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item
      {:label-kw            :t/dapps-permissions
       :accessibility-label :dapps-permissions-button
       :action-fn           #(re-frame/dispatch [:navigate-to :dapps-permissions])}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item
      {:label-kw            :t/need-help
       :accessibility-label :help-button
       :action-fn           #(re-frame/dispatch [:navigate-to :help-center])}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item
      {:label-kw            :t/about-app
       :accessibility-label :about-button
       :action-fn           #(re-frame/dispatch [:navigate-to :about-app])}]
     [profile.components/settings-item-separator]
     [react/view styles/my-profile-settings-logout-wrapper
      [react/view styles/my-profile-settings-logout
       [profile.components/settings-item {:label-kw            :t/logout
                                          :accessibility-label :log-out-button
                                          :destructive?        true
                                          :hide-arrow?         true
                                          :active?             logged-in?
                                          :action-fn           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-pressed])}]]]]))

(defview advanced-settings
  [{:keys [chaos-mode? dev-mode? settings]} network-name on-show supported-biometric-auth]
  {:component-did-mount on-show}
  [react/view
   (when dev-mode?
     [profile.components/settings-item
      {:label-kw            :t/network
       :value               network-name
       :action-fn           #(re-frame/dispatch [:navigate-to :network-settings])
       :accessibility-label :network-button}])
   [profile.components/settings-item-separator]
   [profile.components/settings-item
    {:label-kw            :t/offline-messaging
     :action-fn           #(re-frame/dispatch [:navigate-to :offline-messaging-settings])
     :accessibility-label :offline-messages-settings-button}]
   [profile.components/settings-item-separator]
   [profile.components/settings-item
    {:label-kw            :t/log-level
     :action-fn           #(re-frame/dispatch [:navigate-to :log-level-settings])
     :accessibility-label :log-level-settings-button}]
   (when dev-mode?
     [react/view styles/my-profile-settings-send-logs-wrapper
      [react/view styles/my-profile-settings-send-logs
       [profile.components/settings-item {:label-kw            :t/send-logs
                                          :destructive?        true
                                          :hide-arrow?         true
                                          :action-fn           #(re-frame/dispatch [:logging.ui/send-logs-pressed])}]]])
   [profile.components/settings-item-separator]
   [profile.components/settings-item
    {:label-kw            :t/fleet
     :action-fn           #(re-frame/dispatch [:navigate-to :fleet-settings])
     :accessibility-label :fleet-settings-button}]
   (when config/bootnodes-settings-enabled?
     [profile.components/settings-item-separator])
   (when config/bootnodes-settings-enabled?
     [profile.components/settings-item
      {:label-kw            :t/bootnodes
       :action-fn           #(re-frame/dispatch [:navigate-to :bootnodes-settings])
       :accessibility-label :bootnodes-settings-button}])
   (when dev-mode?
     [profile.components/settings-item-separator])
   (when dev-mode?
     [profile.components/settings-switch-item
      {:label-kw  :t/datasync
       :value     (:datasync? settings)
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/toggle-datasync %])}])
   (when dev-mode?
     [profile.components/settings-item-separator])
   (when dev-mode?
     [profile.components/settings-switch-item
      {:label-kw  :t/v1-messages
       :value     (:v1-messages? settings)
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/toggle-v1-messages %])}])
   (when dev-mode?
     [profile.components/settings-item-separator])
   (when dev-mode?
     [profile.components/settings-switch-item
      {:label-kw  :t/disable-discovery-topic
       :value     (:disable-discovery-topic? settings)
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/toggle-disable-discovery-topic %])}])
   [profile.components/settings-item-separator]
   [profile.components/settings-switch-item
    {:label-kw  :t/dev-mode
     :value     dev-mode?
     :action-fn #(re-frame/dispatch [:multiaccounts.ui/dev-mode-switched %])}]
   [profile.components/settings-item-separator]
   [profile.components/settings-switch-item
    {:label-kw  :t/chaos-mode
     :value     chaos-mode?
     :action-fn #(re-frame/dispatch [:multiaccounts.ui/chaos-mode-switched %])}]
   (when dev-mode?
     [profile.components/settings-item-separator]
     [profile.components/settings-switch-item
      {:label-kw  :t/biometric-auth-setting-label
       :value     (:biometric-auth? settings)
       :active?   (some? supported-biometric-auth)
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched %])}])])

(defview advanced [multiaccount network-name on-show]
  (letsubs [advanced? [:my-profile/advanced?]
            supported-biometric-auth [:supported-biometric-auth]]
    {:component-will-unmount #(re-frame/dispatch [:set :my-profile/advanced? false])}
    [react/view {:padding-bottom 16}
     [react/touchable-highlight {:on-press #(re-frame/dispatch [:set :my-profile/advanced? (not advanced?)])
                                 :style    styles/advanced-button}
      [react/view {:style styles/advanced-button-container}
       [react/view {:style styles/advanced-button-container-background}
        [react/view {:style styles/advanced-button-row}
         [react/text {:style styles/advanced-button-label}
          (i18n/label :t/advanced)]
         [icons/icon (if advanced? :main-icons/dropdown-up :main-icons/dropdown) {:color colors/blue}]]]]]
     (when advanced?
       [advanced-settings multiaccount network-name on-show supported-biometric-auth])]))

(defn contacts-list-item [active-contacts-count]
  [list.views/big-list-item
   {:text            (i18n/label :t/contacts)
    :icon                :main-icons/in-contacts
    :accessibility-label :notifications-button
    :accessory-value     active-contacts-count
    :action-fn           #(re-frame/dispatch [:navigate-to :contacts-list])}])

(defn- ens-item [name {:keys [registrar] :as props}]
  [react/view {:style {:margin-top 8}}
   [list.views/big-list-item
    (let [enabled? (not (nil? registrar))]
      (merge
       {:text                (or name (i18n/label :t/ens-usernames))
        :subtext             (if enabled?
                               (if name (i18n/label :t/ens-your-your-name) (i18n/label :t/ens-usernames-details))
                               (i18n/label :t/ens-network-restriction))
        :icon                :main-icons/username
        :accessibility-label :ens-button}
       (if enabled?
         {:action-fn #(re-frame/dispatch [:navigate-to :ens-main props])}
         {:icon-color    colors/gray
          :active?       false
          :hide-chevron? (not enabled?)})))]])

(defn tribute-to-talk-item
  [opts]
  [list.views/big-list-item
   (merge {:text                (i18n/label :t/tribute-to-talk)
           :accessibility-label :notifications-button
           :action-fn           #(re-frame/dispatch
                                  [:tribute-to-talk.ui/menu-item-pressed])}
          opts)])

(defn- header [{:keys [public-key photo-path] :as account}]
  [profile.components/profile-header
   {:contact                account
    :allow-icon-change?     true
    :include-remove-action? (not= (identicon/identicon public-key) photo-path)}])

(defn- header-in-toolbar [{:keys [photo-path] :as account}]
  (let [displayed-name (multiaccounts/displayed-name account)]
    [react/view {:flex           1
                 :flex-direction :row
                 :align-items    :center
                 :align-self     :stretch}
     [photos/photo photo-path {:size 40}]
     [react/text {:style {:typography   :title-bold
                          :line-height  21
                          :margin-right 40
                          :margin-left  16
                          :text-align   :left}}
      displayed-name]]))

(defn- toolbar-action-items [public-key]
  [toolbar/actions
   [{:icon      :main-icons/share
     :icon-opts {:width  24
                 :height 24}
     :handler   #(re-frame/dispatch [:show-popover {:view :share-chat-key :address public-key}])}]])

(defview my-profile []
  (letsubs [list-ref                     (reagent/atom nil)
            {:keys [public-key photo-path preferred-name]
             :as   current-multiaccount} [:multiaccount]
            network-name                 [:network-name]
            changed-multiaccount         [:my-profile/profile]
            currency                     [:wallet/currency]
            login-data                   [:multiaccounts/login]
            active-contacts-count        [:contacts/active-count]
            tribute-to-talk              [:tribute-to-talk/profile]
            stateofus-registrar          [:ens.stateofus/registrar]]
    (let [shown-multiaccount   (merge current-multiaccount changed-multiaccount)
          ;; We scroll on the component once rendered. setTimeout is necessary,
          ;; likely to allow the animation to finish.
          on-show-advanced
          (fn [] (js/setTimeout #(.scrollToEnd @list-ref {:animated false}) 300))

          ;; toolbar-contents
          header-in-toolbar    (header-in-toolbar shown-multiaccount)
          toolbar-action-items (toolbar-action-items public-key)

          ;; flatlist contents
          header               (header shown-multiaccount)
          content
          [[ens-item preferred-name {:registrar stateofus-registrar}]
           [contacts-list-item active-contacts-count]
           (when tribute-to-talk [tribute-to-talk-item tribute-to-talk])
           [my-profile-settings current-multiaccount shown-multiaccount
            currency (nil? login-data)]
           (when (nil? login-data) [advanced shown-multiaccount network-name on-show-advanced])]]
      [(react/safe-area-view) {:style {:flex 1}}
       [status-bar/status-bar {:type :main}]
       [large-toolbar/minimized-toolbar header-in-toolbar nil toolbar-action-items]
       [large-toolbar/flat-list-with-large-header header content list-ref]])))

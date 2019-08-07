(ns status-im.ui.screens.profile.user.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.components.styles
             :as
             profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.config :as config]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.biometric-auth.core :as biometric-auth]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn my-profile-toolbar []
  [toolbar/toolbar
   {}
   nil
   nil
   [toolbar/text-action
    {:handler            #(re-frame/dispatch [:my-profile/start-editing-profile])
     :accessibility-label :edit-button}
    (i18n/label :t/edit)]])

(defn my-profile-edit-toolbar [on-show]
  (reagent/create-class
   {:component-did-mount on-show
    :reagent-render (fn []
                      [toolbar/toolbar
                       {}
                       nil
                       nil
                       [toolbar/text-action
                        {:handler             #(re-frame/dispatch [:my-profile/save-profile])
                         :accessibility-label :done-button}
                        (i18n/label :t/done)]])}))

(def profile-icon-options
  [{:label  (i18n/label :t/image-source-gallery)
    :action #(re-frame/dispatch [:my-profile/update-picture])}
   {:label  (i18n/label :t/image-source-make-photo)
    :action (fn []
              (re-frame/dispatch [:request-permissions {:permissions [:camera :write-external-storage]
                                                        :on-allowed  #(re-frame/dispatch [:navigate-to :profile-photo-capture])
                                                        :on-denied   (fn []
                                                                       (utils/set-timeout
                                                                        #(utils/show-popup (i18n/label :t/error)
                                                                                           (i18n/label :t/camera-access-error))
                                                                        50))}]))}])

(defn- profile-icon-options-ext []
  (conj profile-icon-options {:label  (i18n/label :t/image-remove-current)
                              :action #(re-frame/dispatch [:my-profile/remove-current-photo])}))

(defn qr-viewer-toolbar [label value]
  [toolbar/toolbar nil
   (toolbar/nav-button
    (toolbar.actions/close toolbar.actions/default-handler))
   [toolbar/content-title label]])

(defn qr-code-share-button [value]
  (let [link (universal-links/generate-link :user :external value)]
    [button/button-with-icon
     {:on-press            #(list-selection/open-share {:message link})
      :label               (i18n/label :t/share-link)
      :icon                :main-icons/share
      :accessibility-label :share-my-contact-code-button
      :style               styles/share-link-button}]))

(defview qr-viewer []
  (letsubs [{:keys [value contact]} [:qr-modal]
            ttt-enabled? [:tribute-to-talk/enabled?]]
    [react/view styles/qr-code-viewer
     [status-bar/status-bar {:type :modal-white}]
     [qr-viewer-toolbar (multiaccounts/displayed-name contact) value]
     [qr-code-viewer/qr-code-viewer
      (merge
       {:style         styles/qr-code
        :value         value
        :hint          (i18n/label :t/qr-code-public-key-hint)
        :legend        (str value)
        :show-tribute-to-talk-warning? ttt-enabled?}
       (when-not platform/desktop?
         {:footer-button qr-code-share-button}))]]))

(defn- show-qr [contact source value]
  #(re-frame/dispatch [:navigate-to :profile-qr-viewer {:contact contact
                                                        :source  source
                                                        :value   value}]))

(defn- my-profile-settings [{:keys [seed-backed-up? mnemonic]}
                            {:keys [settings]}
                            logged-in?
                            extensions]
  (let [show-backup-seed? (and (not seed-backed-up?) (not (string/blank? mnemonic)))
        extensions-settings (vals (get extensions :settings))]
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
     (when extensions-settings
       (for [{:keys [label] :as st} extensions-settings]
         [react/view
          [profile.components/settings-item-separator]
          [profile.components/settings-item
           {:item-text           label
            :action-fn           #(re-frame/dispatch [:navigate-to :my-profile-ext-settings st])}]]))
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

(defview advanced-settings [{:keys [network networks dev-mode? settings]} on-show supported-biometric-auth]
  {:component-did-mount on-show}
  [react/view
   (when (and config/extensions-enabled? dev-mode?)
     [profile.components/settings-item
      {:label-kw            :t/extensions
       :action-fn           #(re-frame/dispatch [:navigate-to :extensions-settings])
       :accessibility-label :extensions-button}])
   (when dev-mode?
     [profile.components/settings-item
      {:label-kw            :t/network
       :value               (get-in networks [network :name])
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
     :value     (:chaos-mode? settings)
     :action-fn #(re-frame/dispatch [:multiaccounts.ui/chaos-mode-switched %])}]
   (when dev-mode?
     [profile.components/settings-item-separator]
     [profile.components/settings-switch-item
      {:label-kw  :t/biometric-auth-setting-label
       :value     (:biometric-auth? settings)
       :active?   (some? supported-biometric-auth)
       :action-fn #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched %])}])])

(defview advanced [params on-show]
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
       [advanced-settings params on-show supported-biometric-auth])]))

(defn share-profile-item
  [{:keys [public-key photo-path] :as current-multiaccount}]
  [list.views/big-list-item
   {:text                (i18n/label :t/share-my-profile)
    :icon                :main-icons/share
    :accessibility-label :share-my-profile-button
    :action-fn           #(re-frame/dispatch [:navigate-to :profile-qr-viewer
                                              {:contact current-multiaccount
                                               :source  :public-key
                                               :value   public-key}])}])

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

(defview extensions-settings []
  (letsubs [{:keys [label view on-close]} [:get-screen-params :my-profile-ext-settings]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [status-bar/status-bar {:type :main}]
     [toolbar/simple-toolbar label]
     [react/scroll-view
      [view]]]))

(defview my-profile []
  (letsubs [{:keys [public-key photo-path preferred-name] :as current-multiaccount} [:multiaccount]
            editing?        [:my-profile/editing?]
            extensions      [:extensions/profile]
            changed-multiaccount [:my-profile/profile]
            currency        [:wallet/currency]
            login-data      [:multiaccounts/login]
            scroll          (reagent/atom nil)
            active-contacts-count [:contacts/active-count]
            tribute-to-talk [:tribute-to-talk/profile]
            stateofus-registrar [:ens.stateofus/registrar]]
    (let [shown-multiaccount    (merge current-multiaccount changed-multiaccount)
          ;; We scroll on the component once rendered. setTimeout is necessary,
          ;; likely to allow the animation to finish.
          on-show-edit     (fn []
                             (js/setTimeout
                              #(.scrollTo @scroll {:x 0 :y 0 :animated false})
                              300))
          on-show-advanced (fn []
                             (js/setTimeout
                              #(.scrollToEnd @scroll {:animated false})
                              300))]
      [react/keyboard-avoiding-view {:style {:flex 1}}
       [status-bar/status-bar {:type :main}]
       [react/view profile.components.styles/profile
        (if editing?
          [my-profile-edit-toolbar on-show-edit]
          [my-profile-toolbar])
        [react/scroll-view {:ref                          #(reset! scroll %)
                            :keyboard-should-persist-taps :handled}
         [react/view profile.components.styles/profile-form
          [profile.components/profile-header
           {:contact              current-multiaccount
            :edited-contact       changed-multiaccount
            :editing?             editing?
            :allow-icon-change?   true
            :options              (if (not= (identicon/identicon public-key)
                                            photo-path)
                                    (profile-icon-options-ext)
                                    profile-icon-options)
            :on-change-text-event :my-profile/update-name}]]
         [share-profile-item (dissoc current-multiaccount :mnemonic)]
         [ens-item preferred-name {:registrar stateofus-registrar}]
         [contacts-list-item active-contacts-count]
         (when tribute-to-talk
           [tribute-to-talk-item tribute-to-talk])
         [my-profile-settings current-multiaccount shown-multiaccount currency (nil? login-data) extensions]
         (when (nil? login-data)
           [advanced shown-multiaccount on-show-advanced])]]])))

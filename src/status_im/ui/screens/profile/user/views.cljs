(ns status-im.ui.screens.profile.user.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.identicon :as identicon]
            [clojure.string :as string]
            [status-im.utils.universal-links.core :as universal-links]))

(defn my-profile-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press            #(re-frame/dispatch [:my-profile/start-editing-profile])
     :accessibility-label :edit-button}
    [react/view
     [react/text {:style      common.styles/label-action-text
                  :uppercase? true}
      (i18n/label :t/edit)]]]])

(defn my-profile-edit-toolbar [on-show]
  (reagent/create-class
   {:component-did-mount on-show
    :reagent-render (fn [] [toolbar/toolbar {}
                            nil
                            [toolbar/content-title ""]
                            [toolbar/default-done {:handler             #(re-frame/dispatch [:my-profile/save-profile])
                                                   :icon                :icons/ok
                                                   :icon-opts           {:color colors/blue}
                                                   :accessibility-label :done-button}]])}))

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
  [toolbar/toolbar {:style styles/qr-toolbar}
   [toolbar/default-done {:icon-opts           {:color colors/black}
                          :accessibility-label :done-button}]
   [toolbar/content-title label]])

(defn qr-code-share-button [value]
  (let [link (universal-links/generate-link :user :external value)]
    [button/button-with-icon
     {:on-press            #(list-selection/open-share {:message link})
      :label               (i18n/label :t/share-link)
      :icon                :icons/share
      :accessibility-label :share-my-contact-code-button
      :style               styles/share-link-button}]))

(defview qr-viewer []
  (letsubs [{:keys [value contact]} [:get :qr-modal]]
    [react/view styles/qr-code-viewer
     [status-bar/status-bar {:type :modal-white}]
     [qr-viewer-toolbar (:name contact) value]
     [qr-code-viewer/qr-code-viewer
      {:style         styles/qr-code
       :footer-button qr-code-share-button
       :value         value
       :hint          (i18n/label :t/qr-code-public-key-hint)
       :legend        (str value)}]]))

(defn- show-qr [contact source value]
  #(re-frame/dispatch [:navigate-to :profile-qr-viewer {:contact contact
                                                        :source  source
                                                        :value   value}]))

(defn- my-profile-settings [{:keys [seed-backed-up? mnemonic]} {:keys [settings]} currency logged-in?]
  (let [show-backup-seed? (and (not seed-backed-up?) (not (string/blank? mnemonic)))]
    [react/view
     [profile.components/settings-title (i18n/label :t/settings)]
     [profile.components/settings-item {:label-kw            :t/ens-names
                                        :action-fn           #(re-frame/dispatch [:profile.ui/ens-names-button-pressed])
                                        :accessibility-label :ens-names-button}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/main-currency
                                        :value               (:code currency)
                                        :action-fn           #(re-frame/dispatch [:navigate-to :currency-settings])
                                        :accessibility-label :currency-button}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/notifications
                                        :accessibility-label :notifications-button
                                        :action-fn           #(.openURL react/linking "app-settings://notification/status-im")}]
     (when show-backup-seed?
       [profile.components/settings-item-separator])
     (when show-backup-seed?
       [profile.components/settings-item
        {:label-kw     :t/backup-your-recovery-phrase
         :action-fn    #(re-frame/dispatch [:navigate-to :backup-seed])
         :icon-content [components.common/counter {:size 22} 1]}])
     [profile.components/settings-item-separator]
     [profile.components/settings-switch-item
      {:label-kw  :t/web3-opt-in
       :value     (or (nil? (:web3-opt-in? settings)) (:web3-opt-in? settings))
       :action-fn #(re-frame/dispatch [:accounts.ui/web3-opt-in-mode-switched %])}]
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
                                          :action-fn           #(re-frame/dispatch [:accounts.logout.ui/logout-pressed])}]]]]))

(defview advanced-settings [{:keys [network networks dev-mode? settings]} on-show]
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
   (when (and dev-mode? (not platform/ios?))
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
   (when (config/pairing-enabled? dev-mode?)
     [profile.components/settings-item-separator])
   (when (config/pairing-enabled? dev-mode?)
     [profile.components/settings-item
      {:label-kw            :t/devices
       :action-fn           #(re-frame/dispatch [:navigate-to :installations])
       :accessibility-label :pairing-settings-button}])
   (when dev-mode?
     [profile.components/settings-item-separator])
   (when dev-mode?
     [profile.components/settings-switch-item
      {:label-kw  :t/pfs
       :value     (:pfs? settings)
       :action-fn #(re-frame/dispatch [:accounts.ui/toggle-pfs %])}])
   [profile.components/settings-item-separator]
   [profile.components/settings-switch-item
    {:label-kw  :t/dev-mode
     :value     dev-mode?
     :action-fn #(re-frame/dispatch [:accounts.ui/dev-mode-switched %])}]])

(defview advanced [params on-show]
  (letsubs [advanced? [:get :my-profile/advanced?]]
    {:component-will-unmount #(re-frame/dispatch [:set :my-profile/advanced? false])}
    [react/view
     [react/touchable-highlight {:on-press #(re-frame/dispatch [:set :my-profile/advanced? (not advanced?)])
                                 :style    styles/advanced-button}
      [react/view {:style styles/advanced-button-container}
       [react/view {:style styles/advanced-button-container-background}
        [react/view {:style styles/advanced-button-row}
         [react/text {:style styles/advanced-button-label}
          (i18n/label :t/wallet-advanced)]
         [icons/icon (if advanced? :icons/up :icons/down) {:color colors/blue}]]]]]
     (when advanced?
       [advanced-settings params on-show])]))

(defn share-profile-item []
  (let [link (universal-links/generate-link :user :external {})]
    [profile.components/settings-item
     {:label-kw            :t/share-my-profile
      :icon                :icons/share
      :accessibility-label :share-button
      :action-fn           #(list-selection/open-share {:message link})}]))

(defn contacts-list-item []
  [profile.components/settings-item
   {:label-kw            :t/contacts
    :icon                :icons/contacts
    :accessibility-label :notifications-button
    :action-fn           #(.openURL react/linking "app-settings://notification/status-im")}])

(defview my-profile []
  (letsubs [{:keys [public-key photo-path] :as current-account} [:account/account]
            editing?        [:get :my-profile/editing?]
            changed-account [:get :my-profile/profile]
            currency        [:wallet/currency]
            login-data      [:get :accounts/login]
            scroll          (reagent/atom nil)]
    (let [shown-account    (merge current-account changed-account)
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
      [react/view profile.components.styles/profile
       (if editing?
         [my-profile-edit-toolbar on-show-edit]
         [my-profile-toolbar])
       [react/scroll-view {:ref                          #(reset! scroll %)
                           :keyboard-should-persist-taps :handled}
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header
          {:contact              current-account
           :edited-contact       changed-account
           :editing?             editing?
           :allow-icon-change?   true
           :options              (if (not= (identicon/identicon public-key) photo-path)
                                   (profile-icon-options-ext)
                                   profile-icon-options)
           :on-change-text-event :my-profile/update-name}]]
        #_[react/view (merge action-button.styles/actions-list
                             styles/share-contact-code-container)
           [button/secondary-button {:on-press            #(re-frame/dispatch [:navigate-to :profile-qr-viewer
                                                                               {:contact current-account
                                                                                :source  :public-key
                                                                                :value   public-key}])
                                     :style               styles/share-contact-code-button
                                     :accessibility-label :share-my-profile-button}
            (i18n/label :t/share-my-profile)]]
        [share-profile-item]
        [contacts-list-item]
        [react/view styles/my-profile-info-container
         [my-profile-settings current-account shown-account currency (nil? login-data)]]
        (when (nil? login-data)
          [advanced shown-account on-show-advanced])]])))

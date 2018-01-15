(ns status-im.ui.screens.profile.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.context-menu :as context-menu]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.utils :as utils]
            [status-im.utils.datetime :as time]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.protocol.core :as protocol]
            [re-frame.core :as re-frame])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn my-profile-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:my-profile/edit-profile])}
    [react/view
     [react/text {:style      styles/toolbar-edit-text
                  :uppercase? component.styles/uppercase?} (i18n/label :t/edit)]]]])

(defn profile-toolbar [contact]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [toolbar/actions
    (when (and (not (:pending? contact))
               (not (:unremovable? contact)))
      [(actions/opts [{:value #(re-frame/dispatch [:hide-contact contact])
                       :text  (i18n/label :t/remove-from-contacts)}])])]])

(defn online-text [last-online]
  (let [last-online-date (time/to-date last-online)
        now-date         (time/now)]
    (if (and (pos? last-online)
             (<= last-online-date now-date))
      (time/time-ago last-online-date)
      (i18n/label :t/active-unknown))))

(defn profile-badge [{:keys [name last-online] :as contact}]
  [react/view styles/profile-badge
   [chat-icon.screen/my-profile-icon {:account contact
                                      :edit?   false}]
   [react/view styles/profile-badge-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 1}
     name]
    (when-not (nil? last-online)
      [react/view styles/profile-activity-status-container
       [react/text {:style styles/profile-activity-status-text}
        (online-text last-online)]])]])

(defn profile-actions [{:keys [pending? whisper-identity dapp?]} chat-id]
  [react/view action-button.styles/actions-list
   (if pending?
     [action-button/action-button {:label     (i18n/label :t/add-to-contacts)
                                   :icon      :icons/add
                                   :icon-opts {:color :blue}
                                   :on-press  #(re-frame/dispatch [:add-pending-contact chat-id])}]
     [action-button/action-button-disabled {:label (i18n/label :t/in-contacts) :icon :icons/ok}])
   [action-button/action-separator]
   [action-button/action-button {:label     (i18n/label :t/start-conversation)
                                 :icon      :icons/chats
                                 :icon-opts {:color :blue}
                                 :on-press  #(re-frame/dispatch [:profile/send-message whisper-identity])}]
   (when-not dapp?
     [react/view
      [action-button/action-separator]
      [action-button/action-button {:label     (i18n/label :t/send-transaction)
                                    :icon      :icons/arrow-right
                                    :icon-opts {:color :blue}
                                    :on-press  #(re-frame/dispatch [:profile/send-transaction chat-id whisper-identity])}]])])

(defn profile-info-item [{:keys [label value options text-mode empty-value? accessibility-label]}]
  [react/view styles/profile-setting-item
   [react/view (styles/profile-info-text-container options)
    [react/text {:style styles/profile-settings-title}
     label]
    [react/view styles/profile-setting-spacing]
    [react/text {:style               (if empty-value?
                                        styles/profile-setting-text-empty
                                        styles/profile-setting-text)
                 :number-of-lines     1
                 :ellipsizeMode       text-mode
                 :accessibility-label accessibility-label}
     value]]
   (when options
     [context-menu/context-menu
      [vector-icons/icon :icons/options]
      options
      nil
      styles/profile-info-item-button])])

(defn show-qr [contact qr-source qr-value]
  #(re-frame/dispatch [:navigate-to-modal :qr-code-view {:contact   contact
                                                         :qr-source qr-source
                                                         :qr-value  qr-value}]))

(defn profile-options [contact k text]
  (into []
        (concat [{:value (show-qr contact k text)
                  :text  (i18n/label :t/show-qr)}]
                (when text
                  (list-selection/share-options text)))))

(defn profile-info-address-item [{:keys [address] :as contact}]
  [profile-info-item
   {:label               (i18n/label :t/address)
    :value               address
    :options             (profile-options contact :address address)
    :text-mode           :middle
    :accessibility-label :profile-address}])

(defn profile-info-public-key-item [public-key contact]
  [profile-info-item
   {:label               (i18n/label :t/public-key)
    :value               public-key
    :options             (profile-options contact :public-key public-key)
    :text-mode           :middle
    :accessibility-label :profile-public-key}])

(defn settings-item-separator []
  [common/separator styles/settings-item-separator])

(defn tag-view [tag]
  [react/text {:style {:color colors/blue}
               :font  :medium}
   (str tag " ")])

(defn colorize-status-hashtags [status]
  (for [[i status] (map-indexed vector (string/split status #" "))]
    (if (utils/hash-tag? status)
      ^{:key (str "item-" i)}
      [tag-view status]
      ^{:key (str "item-" i)}
      (str status " "))))

(defn profile-info-phone-item [phone & [options]]
  (let [phone-empty? (or (nil? phone) (string/blank? phone))
        phone-text   (if phone-empty?
                       (i18n/label :t/not-specified)
                       phone)]
    [profile-info-item {:label               (i18n/label :t/phone-number)
                        :value               phone-text
                        :options             options
                        :empty-value?        phone-empty?
                        :accessibility-label :profile-phone-number}]))

(defn settings-title [title]
  [react/text {:style styles/profile-settings-title}
   title])

(defn settings-item [label-kw value action-fn active?]
  [react/touchable-highlight
   {:on-press action-fn
    :disabled (not active?)}
   [react/view styles/settings-item
    [react/text {:style styles/settings-item-text}
     (i18n/label label-kw)]
    [react/text {:style      styles/settings-item-value
                 :uppercase? component.styles/uppercase?} value]
    (when active?
      [vector-icons/icon :icons/forward {:color colors/gray}])]])

(defn profile-info [{:keys [whisper-identity phone] :as contact}]
  [react/view
   [profile-info-address-item contact]
   [settings-item-separator]
   [profile-info-public-key-item whisper-identity contact]
   [settings-item-separator]
   [profile-info-phone-item phone]])

(defn navigate-to-accounts []
  ;; TODO(rasom): probably not the best place for this call
  (protocol/stop-whisper!)
  (re-frame/dispatch [:navigate-to :accounts]))

(defn handle-logout []
  (utils/show-confirmation (i18n/label :t/logout-title)
                           (i18n/label :t/logout-are-you-sure)
                           (i18n/label :t/logout) navigate-to-accounts))

(defn logout []
  [react/view {}
   [react/touchable-highlight
    {:on-press handle-logout}
    [react/view styles/settings-item
     [react/text {:style styles/logout-text
                  :font  (if platform/android? :medium :default)}
      (i18n/label :t/logout)]]]])

(defn my-profile-settings [{:keys [network networks]}]
  [react/view
   [settings-title (i18n/label :t/settings)]
   [settings-item :t/main-currency "USD" #() false]
   [settings-item-separator]
   [settings-item :t/notifications "" #() true]
   [settings-item-separator]
   [settings-item :t/network (get-in networks [network :name])
    #(re-frame/dispatch [:navigate-to :network-settings]) true]
   (when config/offline-inbox-enabled?
     [settings-item-separator])
   (when config/offline-inbox-enabled?
     [settings-item :t/offline-messaging-settings ""
      #(re-frame/dispatch [:navigate-to :offline-messaging-settings]) true])])

(defn profile-status [status & [edit?]]
  [react/view styles/profile-status-container
   (if (or (nil? status) (string/blank? status))
     [react/touchable-highlight {:on-press #(re-frame/dispatch [:my-profile/edit-profile :edit-status])}
      [react/view
       [react/text {:style styles/add-a-status}
        (i18n/label :t/add-a-status)]]]
     [react/scroll-view
      [react/touchable-highlight {:on-press (when edit? #(re-frame/dispatch [:my-profile/edit-profile :edit-status]))}
       [react/view
        [react/text {:style styles/profile-status-text}
         (colorize-status-hashtags status)]]]])])

(defn network-info []
  [react/view styles/network-info
   [common/network-info]
   [common/separator]])

(defn share-contact-code [current-account public-key]
  [react/touchable-highlight {:on-press (show-qr current-account :public-key public-key)}
   [react/view styles/share-contact-code
    [react/view styles/share-contact-code-text-container
     [react/text {:style      styles/share-contact-code-text
                  :uppercase? component.styles/uppercase?}
      (i18n/label :t/share-contact-code)]]
    [react/view styles/share-contact-icon-container
     [vector-icons/icon :icons/qr {:color colors/blue}]]]])

(defview my-profile []
  (letsubs [{:keys [public-key] :as current-account} [:get-current-account]]
    [react/view styles/profile
     [my-profile-toolbar]
     [react/scroll-view
      [react/view styles/profile-form
       [profile-badge current-account]]
      [react/view action-button.styles/actions-list
       [share-contact-code current-account public-key]]
      [react/view styles/profile-info-container
       [my-profile-settings current-account]]
      [logout]]]))

(defview profile []
  (letsubs [{:keys [status]
             :as   contact} [:contact]
            chat-id [:get :current-chat-id]]
    [react/view styles/profile
     [status-bar/status-bar]
     [profile-toolbar contact]
     [network-info]
     [react/scroll-view
      [react/view styles/profile-form
       [profile-badge contact]
       (when (and (not (nil? status)) (not (string/blank? status)))
         [profile-status status])]
      [common/form-spacer]
      [profile-actions contact chat-id]
      [common/form-spacer]
      [react/view styles/profile-info-container
       [profile-info contact]
       [common/bottom-shadow]]]]))

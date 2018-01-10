(ns status-im.ui.screens.profile.views
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [status-im.ui.components.action-button.action-button
             :refer
             [action-button action-button-disabled action-separator]]
            [status-im.ui.components.action-button.styles :refer [actions-list]]
            [status-im.ui.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.context-menu :refer [context-menu]]
            [status-im.ui.components.list-selection :refer [share-options]]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.styles :refer [color-blue]]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.utils.datetime :as time]
            [status-im.utils.utils :refer [hash-tag?]]
            [status-im.utils.config :as config])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn my-profile-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/actions
    [(actions/opts [{:value #(dispatch [:my-profile/edit-profile])
                     :text  (label :t/edit)}])]]])

(defn profile-toolbar [contact]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [toolbar/actions
    (when (and (not (:pending? contact))
               (not (:unremovable? contact)))
      [(actions/opts [{:value #(dispatch [:hide-contact contact])
                       :text  (label :t/remove-from-contacts)}])])]])

(defn online-text [last-online]
  (let [last-online-date (time/to-date last-online)
        now-date         (time/now)]
    (if (and (pos? last-online)
             (<= last-online-date now-date))
      (time/time-ago last-online-date)
      (label :t/active-unknown))))

(defn profile-badge [{:keys [name last-online] :as contact}]
  [react/view styles/profile-badge
   [my-profile-icon {:account contact
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
  [react/view actions-list
   (if pending?
     [action-button {:label     (label :t/add-to-contacts)
                     :icon      :icons/add
                     :icon-opts {:color :blue}
                     :on-press  #(dispatch [:add-pending-contact chat-id])}]
     [action-button-disabled {:label (label :t/in-contacts) :icon :icons/ok}])
   [action-separator]
   [action-button {:label     (label :t/start-conversation)
                   :icon      :icons/chats
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:profile/send-message whisper-identity])}]
   (when-not dapp?
     [react/view
      [action-separator]
      [action-button {:label     (label :t/send-transaction)
                      :icon      :icons/arrow-right
                      :icon-opts {:color :blue}
                      :on-press  #(dispatch [:profile/send-transaction chat-id whisper-identity])}]])])

(defn profile-info-item [{:keys [label value options text-mode empty-value? accessibility-label]}]
  [react/view styles/profile-setting-item
   [react/view (styles/profile-info-text-container options)
    [react/text {:style styles/profile-setting-title}
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
     [context-menu
      [vector-icons/icon :icons/options]
      options
      nil
      styles/profile-info-item-button])])

(defn show-qr [contact qr-source qr-value]
  #(dispatch [:navigate-to-modal :qr-code-view {:contact   contact
                                                :qr-source qr-source
                                                :qr-value  qr-value}]))

(defn profile-options [contact k text]
  (into []
        (concat [{:value (show-qr contact k text)
                  :text  (label :t/show-qr)}]
                (when text
                  (share-options text)))))

(defn profile-info-address-item [{:keys [address] :as contact}]
  [profile-info-item
   {:label               (label :t/address)
    :value               address
    :options             (profile-options contact :address address)
    :text-mode           :middle
    :accessibility-label :profile-address}])

(defn profile-info-public-key-item [public-key contact]
  [profile-info-item
   {:label               (label :t/public-key)
    :value               public-key
    :options             (profile-options contact :public-key public-key)
    :text-mode           :middle
    :accessibility-label :profile-public-key}])

(defn info-item-separator []
  [common/separator styles/info-item-separator])

(defn tag-view [tag]
  [react/text {:style {:color color-blue}
               :font  :medium}
   (str tag " ")])

(defn colorize-status-hashtags [status]
  (for [[i status] (map-indexed vector (string/split status #" "))]
    (if (hash-tag? status)
      ^{:key (str "item-" i)}
      [tag-view status]
      ^{:key (str "item-" i)}
      (str status " "))))

(defn profile-info-phone-item [phone & [options]]
  (let [phone-empty? (or (nil? phone) (string/blank? phone))
        phone-text  (if phone-empty?
                      (label :t/not-specified)
                      phone)]
    [profile-info-item {:label               (label :t/phone-number)
                        :value               phone-text
                        :options             options
                        :empty-value?        phone-empty?
                        :accessibility-label :profile-phone-number}]))

(defn network-settings []
  [react/touchable-highlight
   {:on-press #(dispatch [:navigate-to :network-settings])}
   [react/view styles/network-settings
    [react/text {:style styles/network-settings-text}
     (label :t/network-settings)]
    [vector-icons/icon :icons/forward {:color :gray}]]])

(defn offline-messaging-settings []
  [react/touchable-highlight
   {:on-press #(dispatch [:navigate-to :offline-messaging-settings])}
   [react/view styles/offline-messaging-settings
    [react/text {:style styles/offline-messaging-settings-text}
     (label :t/offline-messaging-settings)]
    [vector-icons/icon :icons/forward {:color :gray}]]])

(defn profile-info [{:keys [whisper-identity status phone] :as contact}]
  [react/view
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item whisper-identity contact]
   [info-item-separator]
   [profile-info-phone-item phone]])

(defn my-profile-info [{:keys [public-key status phone] :as contact}]
  [react/view
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item public-key contact]
   [info-item-separator]
   [profile-info-phone-item
    phone
    [{:value #(dispatch [:my-profile/update-phone-number])
      :text  (label :t/edit)}]]
   [info-item-separator]
   [network-settings]
   (when config/offline-inbox-enabled?
     [info-item-separator])
   (when config/offline-inbox-enabled?
     [offline-messaging-settings])])

(defn profile-status [status & [edit?]]
  [react/view styles/profile-status-container
   (if (or (nil? status) (string/blank? status))
     [react/touchable-highlight {:on-press #(dispatch [:my-profile/edit-profile :edit-status])}
      [react/view
       [react/text {:style styles/add-a-status}
        (label :t/add-a-status)]]]
     [react/scroll-view
      [react/touchable-highlight {:on-press (when edit? #(dispatch [:my-profile/edit-profile :edit-status]))}
       [react/view
        [react/text {:style styles/profile-status-text}
         (colorize-status-hashtags status)]]]])])

(defn network-info []
  [react/view styles/network-info
   [common/network-info]
   [common/separator]])

(defview my-profile []
  (letsubs [{:keys [status public-key] :as current-account} [:get-current-account]]
    [react/view styles/profile
     [my-profile-toolbar]
     [network-info]
     [react/scroll-view
      [react/view styles/profile-form
       [profile-badge current-account]
       [profile-status status true]]
      [common/form-spacer]
      [react/view actions-list
       [action-button {:label     (label :t/show-qr)
                       :icon      :icons/qr
                       :icon-opts {:color :blue}
                       :on-press  (show-qr current-account :public-key public-key)}]]
      [common/form-spacer]
      [react/view styles/profile-info-container
       [my-profile-info current-account]
       [common/bottom-shadow]]]]))

(defview profile []
  (letsubs [{:keys [pending?
                    status
                    whisper-identity]
             :as   contact} [:contact]
            chat-id  [:get :current-chat-id]]
    [react/view styles/profile
     [status-bar]
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

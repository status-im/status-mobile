(ns status-im.ui.screens.profile.views
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [status-im.components.action-button.action-button
             :refer
             [action-button action-button-disabled action-separator]]
            [status-im.components.action-button.styles :refer [actions-list]]
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.common.common
             :refer
             [bottom-shadow form-spacer separator]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.list-selection :refer [share-options]]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.styles :refer [color-blue]]
            [status-im.components.toolbar-new.actions :as actions]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.utils.datetime :as time]
            [status-im.utils.utils :refer [hash-tag?]]
            [status-im.utils.config :as config])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))
  
(defn my-profile-toolbar []
  [toolbar/toolbar {:actions [(actions/opts [{:value #(dispatch [:my-profile/edit-profile])
                                              :text  "Edit"}])]}])

(defn profile-toolbar [contact]
  [toolbar/toolbar
   (when (and (not (:pending? contact))
              (not (:unremovable? contact)))
     {:actions [(actions/opts [{:value #(dispatch [:hide-contact contact])
                                :text  "Remove"}])]})])

(defn online-text [last-online]
  (let [last-online-date (time/to-date last-online)
        now-date         (time/now)]
    (if (and (pos? last-online)
             (<= last-online-date now-date))
      (time/time-ago last-online-date)
      (label "Unknown"))))

(defn profile-badge [{:keys [name last-online] :as contact}]
  [react/view styles/profile-badge
   [my-profile-icon {:account {:photo-path (js/require "./200px-Anonymous.png") :name name}
                     :edit?   false}]
                     
   [react/view styles/profile-badge-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 1}
     "Satoshi N."]
    (when (nil? last-online)
      [react/view styles/profile-activity-status-container
       [react/text {:style styles/profile-activity-status-text}
        "Online"]])]])

(defn profile-actions [{:keys [pending? whisper-identity dapp?]} chat-id]
  [react/view actions-list
   (if pending?
     [action-button {:label     (label "Add")
                     :icon      :icons/add
                     :icon-opts {:color :blue}
                     :on-press  #(dispatch [:add-pending-contact chat-id])}]
     [action-button-disabled {:label (label "Contacts") :icon :icons/ok}])
   [action-separator]
   [action-button {:label     (label "Start conversation")
                   :icon      :icons/chats
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:profile/send-message whisper-identity])}]
   (when-not dapp?
     [react/view
      [action-separator]
      [action-button {:label     (label "Send transaction")
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
     value]]])
   

(defn show-qr [contact qr-source]
  #(dispatch [:navigate-to-modal :qr-code-view {:contact   contact
                                                :qr-source qr-source}]))

(defn profile-options [contact k text]
  (into []
        (concat [{:value (show-qr contact k)
                  :text  "QR"}]
                (when text
                  (share-options text)))))

(defn profile-info-address-item [{:keys [address] :as contact}]
  [profile-info-item
   {:label               "Address"
    :value               address
    :options             (profile-options contact :address address)
    :text-mode           :middle
    :accessibility-label :profile-address}])

(defn profile-info-public-key-item [public-key contact]
  [profile-info-item
   {:label               "Public key"
    :value               public-key
    :options             (profile-options contact :public-key public-key)
    :text-mode           :middle
    :accessibility-label :profile-public-key}])

(defn info-item-separator []
  [react/view
    [react/view {:style {:height 25}}]
    [react/view {:style {:background-color "#d9dae1"
                         :height 5
                         :margin-left 16
                         :opacity 0.5}}]
   [react/view {:style {:height 25}}]])

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
                      (label "Not specified")
                      phone)]
    [profile-info-item {:label               "Phone number"
                        :value               phone-text
                        :options             options
                        :empty-value?        phone-empty?
                        :accessibility-label :profile-phone-number}]))

(defn network-settings []
  [react/touchable-highlight
   {:on-press #(dispatch [:navigate-to :network-settings])}
   [react/view styles/network-settings
    [react/text {:style styles/network-settings-text}
     (label "Network settings")]
    [vi/icon :icons/forward {:color :gray}]]])

(defn profile-info [{:keys [whisper-identity status phone] :as contact}]
  [react/view
   ;;[profile-info-address-item contact]
   ;;[info-item-separator]
   ;;[profile-info-public-key-item whisper-identity contact]
   ;;[info-item-separator]
   [profile-info-phone-item phone]])

(defn my-profile-info [{:keys [public-key status phone] :as contact}]
  [react/view
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item public-key contact]
   [info-item-separator]
   [profile-info-phone-item
    phone
    [{
      :text  "Edit"}]]])
   ;;[info-item-separator]])


(defn profile-status [status & [edit?]]
  [react/view styles/profile-status-container
   (if (or (nil? status) (string/blank? status))
      [react/view
       [react/text {:style styles/add-a-status}
        "Add status"]]
     [react/scroll-view
       [react/view
        [react/text {:style styles/profile-status-text}
         (colorize-status-hashtags status)]]])])


(defn testnet-only []
  [react/view styles/testnet-only-container
   [react/view styles/testnet-icon
    [react/text {:style styles/testnet-icon-text}
     (label "testnet")]]
   [react/text {:style styles/testnet-only-text}
    (label "testnet")]])

(defview profile []
  [react/view styles/profile-form
   [react/scroll-view
    [react/view styles/profile-form
     [profile-badge]
     [profile-status]]
    [form-spacer]
    [react/view styles/profile-badge
     [my-profile-icon {:account {:photo-path (js/require "./QR.svg") :name "QR icon"}
                       :edit?   false}]
     [action-button {:label "Show QR"}]]
    [form-spacer]
    [react/view styles/profile-info-container
       [my-profile-info {:phone "+44 7911 123456"
                         :public-key "0x04223458893...303a35c18c29caf"
                         :address "e6e248c8caac...d48395284bd23a"}]]]])
    
    ;;[react/image {:source {:uri "https://origami.design/public/images/bird-logo.png" :width 64 :height 64}
    ;;    :style {:width 64 :height 64}]]]])     ;;  [bottom-shadow]]]])
     
   
   


 ;; [react/view styles/profile-form 
 ;;     [react/view
 ;;      [react/text {:style styles/add-a-status}
 ;;       "Add status")


  ;;[react/view [react/text "Some text here!!"]])

    

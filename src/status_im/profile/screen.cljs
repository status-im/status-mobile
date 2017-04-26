(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [clojure.string :as str]
            [reagent.core :as r]
            [status-im.contacts.styles :as cst]
            [status-im.components.common.common :refer [separator
                                                        form-spacer
                                                        top-shadow
                                                        bottom-shadow]]
            [status-im.components.styles :refer [color-blue color-gray5]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.action-button.action-button :refer [action-button
                                                                      action-button-disabled
                                                                      action-separator]]
            [status-im.components.action-button.styles :refer [actions-list]]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                icon
                                                scroll-view
                                                touchable-highlight]]
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.list-selection :refer [share-options]]
            [status-im.utils.platform :refer [platform-specific android?]]
            [status-im.profile.handlers :refer [message-user]]
            [status-im.profile.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.utils.datetime :as time]))


(defn my-profile-toolbar []
  [toolbar {:actions [(act/opts [{:value #(dispatch [:open-edit-my-profile])
                                  :text (label :t/edit)}])]}])

(defn profile-toolbar [contact]
  [toolbar
   (when-not (:pending? contact)
     {:actions [(act/opts [{:value #(dispatch [:hide-contact contact])
                            :text  (label :t/remove-from-contacts)}])]})])

(defn online-text [last-online]
  (let [last-online-date (time/to-date last-online)
        now-date         (time/now)]
    (if (and (pos? last-online)
             (<= last-online-date now-date))
      (time/time-ago last-online-date)
      (label :t/active-unknown))))

(defn profile-badge [{:keys [name last-online] :as contact}]
  [view st/profile-bage
   [my-profile-icon {:account contact
                     :edit?   false}]
   [view st/profile-badge-name-container
    [text {:style st/profile-name-text
           :number-of-lines 1}
      name]
    (when-not (nil? last-online)
      [view st/profile-activity-status-container
       [text {:style st/profile-activity-status-text}
        (online-text last-online)]])]])

(defn profile-actions [{:keys [pending? whisper-identity dapp?]} chat-id]
  [view actions-list
   (if pending?
     [action-button (label :t/add-to-contacts)
                    :add_blue
                    #(dispatch [:add-pending-contact chat-id])]
     [action-button-disabled (label :t/in-contacts)
                             :ok_dark])
   [action-separator]
   [action-button (label :t/start-conversation)
                  :chats_blue
                  #(message-user whisper-identity)]
   (when-not dapp?
     [view
      [action-separator]
      [action-button (label :t/send-transaction)
                     :arrow_right_blue
                     #(dispatch [:open-chat-with-the-send-transaction chat-id])]])])

(defn profile-info-item [{:keys [label value options text-mode empty-value?]}]
  [view st/profile-setting-item
   [view (st/profile-info-text-container options)
    [text {:style st/profile-setting-title}
     label]
    [view st/profile-setting-spacing]
    [text {:style           (if empty-value?
                              st/profile-setting-text-empty
                              st/profile-setting-text)
           :number-of-lines 1
           :ellipsizeMode   text-mode}
     value]]
   (when options
     [context-menu
      [icon :options_gray]
      options
      nil
      st/profile-info-item-button])])

(defn show-qr [contact qr-source]
  #(dispatch [:navigate-to-modal :qr-code-view {:contact   contact
                                                :qr-source qr-source}]))

(defn profile-info-address-item [{:keys [address] :as contact}]
  [profile-info-item
   {:label     (label :t/address)
    :value     address
    :options   (into []
                 (concat [{:value (show-qr contact :address)
                           :text (label :t/show-qr)}]
                         (share-options address)))
    :text-mode :middle}])

(defn profile-info-public-key-item [public-key contact]
  [profile-info-item
   {:label     (label :t/public-key)
    :value     public-key
    :options   (into []
                 (concat [{:value (show-qr contact :public-key)
                           :text (label :t/show-qr)}]
                         (share-options public-key)))
    :text-mode :middle}])

(defn info-item-separator []
  [separator st/info-item-separator])

(defn tag-view [tag]
  [text {:style {:color color-blue}
         :font :medium}
   (str tag " ")])

(defn colorize-status-hashtags [status]
  (for [[i status] (map-indexed vector (str/split status #" "))]
    (if (.startsWith status "#")
      ^{:key (str "item-" i)}
      [tag-view status]
      ^{:key (str "item-" i)}
      (str status " "))))

(defn profile-info-phone-item [phone & [options]]
  (let [phone-empty? (or (nil? phone) (str/blank? phone))
        phone-text  (if phone-empty?
                       (label :t/not-specified)
                       phone)]
    [profile-info-item {:label        (label :t/phone-number)
                        :value        phone-text
                        :options      options
                        :empty-value? phone-empty?}]))

(defn profile-info [{:keys [whisper-identity status phone] :as contact}]
  [view
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item whisper-identity contact]
   [info-item-separator]
   [profile-info-phone-item phone]])

(defn my-profile-info [{:keys [public-key status phone] :as contact}]
  [view
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item public-key contact]
   [info-item-separator]
   [profile-info-phone-item
    phone
    [{:value #(dispatch [:phone-number-change-requested])
      :text (label :t/edit)}]]])

(defn- profile-status-on-press []
  (dispatch [:set-in [:profile-edit :edit-status?] true])
  (dispatch [:open-edit-my-profile]))

(defn profile-status [status & [edit?]]
  [view st/profile-status-container
   (if (or (nil? status) (str/blank? status))
     [touchable-highlight {:on-press profile-status-on-press}
      [view
       [text {:style st/add-a-status}
        (label :t/add-a-status)]]]
     [scroll-view
      [touchable-highlight {:on-press (when edit? profile-status-on-press)}
       [view
        [text {:style st/profile-status-text}
         (colorize-status-hashtags status)]]]])])

(defview my-profile []
  [{:keys [status] :as current-account} [:get-current-account]]
  [view st/profile
   [status-bar]
   [my-profile-toolbar]
   [view st/profile-form
    [profile-badge current-account]
    [profile-status status true]]
   [form-spacer]
   [view actions-list
    [action-button (label :t/show-qr)
     :q_r_blue
     (show-qr current-account :public-key)]]
   [form-spacer]
   [view st/profile-info-container
    [my-profile-info current-account]
    [bottom-shadow]]])

(defview profile []
  [{:keys [pending?
           status
           whisper-identity]
    :as contact} [:contact]
   chat-id [:get :current-chat-id]]
  [view st/profile
   [status-bar]
   [profile-toolbar contact]
   [scroll-view
    [view st/profile-form
     [profile-badge contact]
     (when (and (not (nil? status)) (not (str/blank? status)))
       [profile-status status])]
    [form-spacer]
    [profile-actions contact chat-id]
    [form-spacer]
    [view st/profile-info-container
     [profile-info contact]
     [bottom-shadow]]]])

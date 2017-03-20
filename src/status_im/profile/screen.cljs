(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [clojure.string :as str]
            [reagent.core :as r]
            [status-im.contacts.styles :as cst]
            [status-im.components.common.common :refer [separator]]
            [status-im.components.styles :refer [color-blue color-gray5]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.action-button.action-button :refer [action-button
                                                                      action-separator]]
            [status-im.components.common.common :refer [top-shaddow bottom-shaddow]]
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

(defn online-text [last-online]
  (let [last-online-date (time/to-date last-online)
        now-date         (time/now)]
    (if (and (pos? last-online)
             (<= last-online-date now-date))
      (time/time-ago last-online-date)
      (label :t/active-unknown))))

(defn profile-bage [{:keys [name last-online] :as contact}]
  [view st/profile-bage
   [my-profile-icon {:account contact
                     :edit?   false}]
   [view st/profile-name-container
    [text {:style st/profile-name-text}
     name]]
   (when-not (nil? last-online)
     [view st/profile-status-container
      [text {:style st/profile-status-text}
       (online-text last-online)]])])

(defn add-to-contacts [pending? chat-id]
  [view
   (if pending?
     [touchable-highlight {:on-press #(dispatch [:add-pending-contact chat-id])}
      [view st/add-to-contacts
       [text {:style st/add-to-contacts-text
              :font (when android? :medium)
              :uppercase? (get-in platform-specific [:uppercase?])}
        (label :t/add-to-contacts)]]]
     [view st/in-contacts
      [icon :ok_blue]
      [view st/in-contacts-inner
       [text {:style st/in-contacts-text
              :font (when android? :medium)
              :uppercase? (get-in platform-specific [:uppercase?])}
        (label :t/in-contacts)]]])])

(defn profile-actions [whisper-identity chat-id]
  [view st/profile-actions-container
   [action-button (label :t/start-conversation)
                  :chats_blue
                  #(message-user whisper-identity)]
   [action-separator]
   [action-button (label :t/send-transaction)
                  :arrow_right_blue
                  #(dispatch [:open-chat-with-the-send-transaction chat-id])]])

(defn profile-info-item [{:keys [label value options text-mode empty-value?]}]
  [view st/profile-setting-item
   [view (st/profile-setting-text-container options)
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
      options])])

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

(defn profile-info-status-item [status & [options]]
  (let [status-empty? (= "" status)
        status-text  (if status-empty?
                       (label :t/profile-no-status)
                       (colorize-status-hashtags status))]
    [profile-info-item {:label        (label :t/status)
                        :value        status-text
                        :options      options
                        :empty-value? status-empty?}]))

(defn profile-info-phone-item [phone & [options]]
  (let [phone-empty? (or (nil? phone) (= "" phone))
        phone-text  (if phone-empty?
                       (label :t/not-specified)
                       phone)]
    [profile-info-item {:label        (label :t/phone-number)
                        :value        phone-text
                        :options      options
                        :empty-value? phone-empty?}]))

(defn profile-info [{:keys [whisper-identity :whisper-identity
                            status           :status
                            phone            :phone] :as contact}]
    [view
     [profile-info-status-item status]
     [info-item-separator]
     [profile-info-address-item contact]
     [info-item-separator]
     [profile-info-public-key-item whisper-identity contact]
     [info-item-separator]
     [profile-info-phone-item phone]])

(defn my-profile-info [{:keys [public-key :public-key
                               status     :status
                               phone      :phone] :as contact}]
  [view st/my-profile-info-container
   [profile-info-status-item
    status
    [{:value #(dispatch [:open-edit-my-profile])
      :text (label :t/edit)}]]
   [info-item-separator]
   [profile-info-address-item contact]
   [info-item-separator]
   [profile-info-public-key-item public-key contact]
   [info-item-separator]
   [profile-info-phone-item
    phone
    [{:value #(dispatch [:phone-number-change-requested])
      :text (label :t/edit)}]]])

(defview my-profile []
  [current-account [:get-current-account]]
  [view st/profile
   [status-bar]
   [my-profile-toolbar]
   [view st/my-profile-form
    [profile-bage current-account]]
   [bottom-shaddow]
   [view st/profile-info-container
    [top-shaddow]
    [view st/profile-actions-container
     [action-button (label :t/share-qr)
                    :q_r_blue
                    (show-qr current-account :public-key)]]
    [view st/form-separator]
    [my-profile-info current-account]
    [bottom-shaddow]]])

(defview profile []
  [{:keys [pending?
           whisper-identity]
    :as contact} [:contact]
   chat-id [:get :current-chat-id]]
  [view st/profile
   [status-bar]
   [toolbar]
   [scroll-view
    [view st/profile-form
     [profile-bage contact]
     [add-to-contacts pending? chat-id]]
    [bottom-shaddow]
    [view st/profile-info-container
     [top-shaddow]
     [profile-actions whisper-identity chat-id]
     [view st/form-separator]
     [profile-info contact]
     [bottom-shaddow]]]])
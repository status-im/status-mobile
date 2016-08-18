(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                icon
                                                scroll-view
                                                touchable-highlight
                                                touchable-opacity
                                                show-image-picker]]
            [status-im.components.icons.custom-icons :refer [oct-icon]]
            [status-im.components.chat-icon.screen :refer [profile-icon
                                                           my-profile-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.profile.styles :as st]
            [status-im.profile.handlers :refer [get-hashtags
                                                message-user
                                                update-profile]]
            [status-im.components.qr-code :refer [qr-code]]
            [status-im.utils.phone-number :refer [format-phone-number
                                                  valid-mobile-number?]]
            [status-im.utils.fs :refer [read-file]]
            [status-im.utils.types :refer [clj->json]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.i18n :refer [label]]
            [clojure.string :as str]))

(defview toolbar [{:keys [account profile-edit-data edit?]}]
  [view
   [touchable-highlight {:style    st/back-btn-touchable
                         :on-press (fn []
                                     (dispatch [:set :profile-edit {:edit?      false
                                                                    :name       nil
                                                                    :email      nil
                                                                    :status     nil
                                                                    :photo-path nil}])
                                     (dispatch [:navigate-back]))}
    [view st/back-btn-container
     [icon :back st/back-btn-icon]]]
   [touchable-highlight {:style    st/actions-btn-touchable
                         :on-press (fn []
                                     (when edit?
                                       (update-profile account profile-edit-data))
                                     (dispatch [:set-in [:profile-edit :edit?] (not edit?)]))}
    [view st/actions-btn-container
     (if edit?
       [oct-icon {:name  :check
                  :style st/ok-btn-icon}]
       [icon :dots st/edit-btn-icon])]]])

(defview status-image-view [{{:keys [list-selection-fn]
                              :as   platform-specific} :platform-specific
                             {address  :address
                              username :name}            :account
                             photo-path                  :photo-path
                             status                      :status
                             edit?                       :edit?}]
  [view st/status-block
   [view st/user-photo-container
    (if edit?
      [touchable-highlight {:on-press (fn []
                                        (dispatch [:open-image-source-selector list-selection-fn]))}
       [view
        [my-profile-icon {:account {:photo-path photo-path
                                    :name       username}
                          :edit?   edit?}]]]
      [my-profile-icon {:account {:photo-path photo-path
                                  :name       username}
                        :edit?   edit?}])]
   [text {:style             st/username
          :platform-specific platform-specific
          :font              :default}
    (if (= username address)
      (label :t/user-anonymous)
      username)]
   [text-input {:style          st/status-input
                :editable       edit?
                :placeholder    (label :t/profile-no-status)
                :on-change-text #(dispatch [:set-in [:profile-edit :status] %])}
    status]])


(defview profile-property-view [{name                :name
                                 value               :value
                                 empty-value         :empty-value
                                 on-change-text      :on-change-text
                                 {edit-mode? :edit?} :profile-data
                                 platform-specific   :platform-specific}]
  [view st/profile-property-view-container
   [view st/profile-property-view-sub-container
    [text {:style             st/profile-property-view-label
           :platform-specific platform-specific
           :font              :medium}
     name]
    [text-input {:style          st/profile-property-view-value
                 :editable       (and on-change-text edit-mode?)
                 :on-change-text on-change-text}
     (or value (when-not edit-mode? empty-value))]]])

(defview profile [{platform-specific :platform-specific}]
  [{whisper-identity :whisper-identity
    address          :address
    username         :name
    email            :email
    photo-path       :photo-path
    phone            :phone
    status           :status
    :as              contact} [:contact]]
  [scroll-view {:style st/profile}
   [status-bar {:platform-specific platform-specific}]
   [view
    [touchable-highlight {:style    st/back-btn-touchable
                          :on-press (fn []
                                      (dispatch [:navigate-back]))}
     [view st/back-btn-container
      [icon :back st/back-btn-icon]]]
    [touchable-highlight {:style    st/actions-btn-touchable
                          :on-press (fn []
                                      (.log js/console "Dots pressed!"))}
     [view st/actions-btn-container
      [icon :dots st/edit-btn-icon]]]]

   [status-image-view {:platform-specific platform-specific
                       :account           contact
                       :photo-path        photo-path
                       :edit?             false}]

   [view st/status-block
    [view st/btns-container
     [touchable-highlight {:onPress #(message-user whisper-identity)}
      [view st/message-btn
       [text {:style st/message-btn-text} (label :t/message)]]]
     [touchable-highlight {:onPress (fn []
                                      ;; TODO not implemented
                                      )}
      [view st/more-btn
       [icon :more_vertical_blue st/more-btn-image]]]]]

   [scroll-view st/profile-properties-container
    [profile-property-view {:name              (label :t/username)
                            :value             (if (not= username address)
                                                 username)
                            :empty-value       (label :t/not-specified)
                            :platform-specific platform-specific}]
    [profile-property-view {:name              (label :t/phone-number)
                            :value             (if-not (or (not phone) (str/blank? phone))
                                                 (format-phone-number phone))
                            :empty-value       (label :t/not-specified)
                            :platform-specific platform-specific}]
    [profile-property-view {:name              (label :t/email)
                            :value             (if-not (or (not email) (str/blank? email))
                                                 email)
                            :empty-value       (label :t/not-specified)
                            :platform-specific platform-specific}]
    [view st/report-user-container
     [touchable-highlight {:on-press (fn []
                                       ;; TODO not implemented
                                       )}
      [view [text {:style st/report-user-text} (label :t/report-user)]]]]]])

(defview my-profile [{platform-specific :platform-specific}]
  [{public-key :public-key
    address    :address
    username   :name
    email      :email
    photo-path :photo-path
    phone      :phone
    status     :status
    :as        account} [:get-current-account]
   {edit?          :edit?
    new-status     :status
    new-photo-path :photo-path
    :as            profile-edit-data} [:get :profile-edit]]
  [scroll-view {:style st/profile}
   [status-bar {:platform-specific platform-specific}]
   [toolbar {:account           account
             :profile-edit-data profile-edit-data
             :edit?             edit?}]

   [status-image-view {:platform-specific platform-specific
                       :account           account
                       :photo-path        (or new-photo-path photo-path)
                       :status            (or new-status status)
                       :edit?             edit?}]

   [scroll-view st/profile-properties-container
    [profile-property-view {:name              (label :t/username)
                            :value             (if (not= username address)
                                                 username)
                            :empty-value       (label :t/not-specified)
                            :on-change-text    #(dispatch [:set-in [:profile-edit :name] %])
                            :profile-data      profile-edit-data
                            :platform-specific platform-specific}]
    [profile-property-view {:name              (label :t/phone-number)
                            :value             (if-not (or (not phone) (str/blank? phone))
                                                 (format-phone-number phone))
                            :empty-value       (label :t/not-specified)
                            :profile-data      profile-edit-data
                            :platform-specific platform-specific}]
    [profile-property-view {:name              (label :t/email)
                            :value             (if-not (or (not email) (str/blank? email))
                                                 email)
                            :empty-value       (label :t/not-specified)
                            :on-change-text    #(dispatch [:set-in [:profile-edit :email] %])
                            :profile-data      profile-edit-data
                            :platform-specific platform-specific}]
    [view st/qr-code-container
     ;; TODO: this public key should be replaced by address
     [qr-code {:value (str "ethereum:" public-key)
               :size  220}]]]])

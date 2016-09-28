(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [cljs.spec :as s]
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
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.qr-code :refer [qr-code]]
            [status-im.utils.handlers :refer [get-hashtags]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.profile.handlers :refer [message-user
                                                update-profile]]
            [status-im.profile.validations :as v]
            [status-im.profile.styles :as st]
            [status-im.i18n :refer [label]]))

(defn toolbar [{:keys [account profile-edit-data edit?]}]
  (let [profile-edit-data-valid? (s/valid? ::v/profile profile-edit-data)]
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
                                       (if edit?
                                         (when profile-edit-data-valid?
                                           (update-profile account profile-edit-data)
                                           (dispatch [:set-in [:profile-edit :edit?] false]))
                                         (dispatch [:set-in [:profile-edit :edit?] true])))}
      [view st/actions-btn-container
       (if edit?
         [oct-icon {:name  :check
                    :style (st/ok-btn-icon profile-edit-data-valid?)}]
         [icon :dots st/edit-btn-icon])]]]))

(defn status-image-view [{{address  :address
                           username :name} :account
                          photo-path       :photo-path
                          status           :status
                          edit?            :edit?}]
  [view st/status-block
   [view st/user-photo-container
    (if edit?
      [touchable-highlight {:on-press (fn []
                                        (let [list-selection-fn (get platform-specific :list-selection-fn)]
                                          (dispatch [:open-image-source-selector list-selection-fn])))}
       [view
        [my-profile-icon {:account {:photo-path photo-path
                                    :name       username}
                          :edit?   edit?}]]]
      [my-profile-icon {:account {:photo-path photo-path
                                  :name       username}
                        :edit?   edit?}])]
   [text {:style st/username
          :font  :default}
    (if (= username address)
      (label :t/user-anonymous)
      username)]
   [text-input {:style          st/status-input
                :editable       edit?
                :placeholder    (label :t/profile-no-status)
                :on-change-text #(dispatch [:set-in [:profile-edit :status] %])
                :value          status}]])

(defview profile []
  [{whisper-identity :whisper-identity
    address          :address
    username         :name
    email            :email
    photo-path       :photo-path
    phone            :phone
    status           :status
    :as              contact} [:contact]]
  [scroll-view {:style st/profile}
   [status-bar]
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

   [status-image-view {:account    contact
                       :photo-path photo-path
                       :edit?      false}]

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
    [text-field
     {:editable      false
      :input-style   st/profile-input-text
      :wrapper-style st/profile-input-wrapper
      :value         (if (and (not= username address)
                              username
                              (not (str/blank? username)))
                       username
                       (label :t/not-specified))
      :label         (label :t/username)}]

    [text-field
     {:editable      false
      :input-style   st/profile-input-text
      :wrapper-style st/profile-input-wrapper
      :value         (if (and phone (not (str/blank? phone)))
                       (format-phone-number phone)
                       (label :t/not-specified))
      :label         (label :t/phone-number)}]

    [text-field
     {:editable      false
      :input-style   st/profile-input-text
      :wrapper-style st/profile-input-wrapper
      :value         (if (and email (not (str/blank? email)))
                       email
                       (label :t/not-specified))
      :label         (label :t/email)}]

    [view st/report-user-container
     [touchable-highlight {:on-press (fn []
                                       ;; TODO not implemented
                                       )}
      [view [text {:style st/report-user-text} (label :t/report-user)]]]]]])

(defview my-profile []
  [{public-key :public-key
    address    :address
    username   :name
    email      :email
    photo-path :photo-path
    phone      :phone
    status     :status
    :as        account} [:get-current-account]
   {edit?          :edit?
    new-name       :name
    new-email      :email
    new-status     :status
    new-photo-path :photo-path
    :as            profile-edit-data} [:get :profile-edit]]
  [scroll-view {:style st/profile}
   [status-bar]
   [toolbar {:account           account
             :profile-edit-data profile-edit-data
             :edit?             edit?}]

   [status-image-view {:account    account
                       :photo-path (or new-photo-path photo-path)
                       :status     (or new-status status)
                       :edit?      edit?}]

   [scroll-view st/profile-properties-container
    [text-field
     {:error          (if-not (s/valid? ::v/name new-name)
                        (label :t/error-incorrect-name))
      :error-color    "#7099e6"
      :editable       edit?
      :input-style    (if edit?
                        st/profile-input-text
                        st/profile-input-text-non-editable)
      :wrapper-style  st/profile-input-wrapper
      :value          (if (not= username address)
                        username)
      :label          (label :t/username)
      :on-change-text #(dispatch [:set-in [:profile-edit :name] %])}]

    [text-field
     {:editable      false
      :input-style   st/profile-input-text-non-editable
      :wrapper-style st/profile-input-wrapper
      :value         (if (and phone (not (str/blank? phone)))
                       (format-phone-number phone))
      :label         (label :t/phone-number)}]

    [text-field
     {:error          (if-not (s/valid? ::v/email new-email)
                        (label :t/error-incorrect-email))
      :error-color    "#7099e6"
      :editable       edit?
      :input-style    (if edit?
                        st/profile-input-text
                        st/profile-input-text-non-editable)
      :wrapper-style  st/profile-input-wrapper
      :value          (if (and email (not (str/blank? email)))
                        email)
      :label          (label :t/email)
      :on-change-text #(dispatch [:set-in [:profile-edit :email] %])}]

    [view st/qr-code-container
     ;; TODO: this public key should be replaced by address
     [qr-code {:value (str "ethereum:" public-key)
               :size  220}]]]])

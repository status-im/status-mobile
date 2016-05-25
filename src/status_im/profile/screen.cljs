(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                              text
                                              image
                                              icon
                                              scroll-view
                                              touchable-highlight
                                              touchable-opacity]]
            [status-im.components.chat-icon.screen :refer [profile-icon
                                                         my-profile-icon]]
            [status-im.profile.styles :as st]
            [status-im.i18n :refer [t]]))

(defn profile-property-view [{:keys [name value]}]
  [view st/profile-property-view-container
   [view st/profile-property-view-sub-container
    [text {:style st/profile-property-view-label} name]
    [text {:style st/profile-property-view-value} value]]])

(defn message-user [identity]
  (when identity
    (dispatch [:navigate-to :chat identity])))

(defview profile []
  [{:keys [name whisper-identity phone-number]} [:contact]]
  [scroll-view {:style st/profile}
   [touchable-highlight {:style    st/back-btn-touchable
                         :on-press #(dispatch [:navigate-back])}
    [view st/back-btn-container
     [icon :back st/back-btn-icon]]]
   [view st/status-block
    [view st/user-photo-container
     [profile-icon]]
    [text {:style st/user-name} name]
    ;; TODO stub data
    [text {:style st/status} (t :not-implemented)]
    [view st/btns-container
     [touchable-highlight {:onPress #(message-user whisper-identity)}
      [view st/message-btn
       [text {:style st/message-btn-text} (t :profile.message)]]]
     [touchable-highlight {:onPress (fn []
                                      ;; TODO not implemented
                                      )}
      [view st/more-btn
       [icon :more_vertical_blue st/more-btn-image]]]]]
   [view st/profile-properties-container
    [profile-property-view {:name  (t :profile.username)
                            :value name}]
    [profile-property-view {:name  (t :profile.phone-number)
                            :value phone-number}]
    ;; TODO stub data
    [profile-property-view {:name  (t :profile.email)
                            :value (t :not-implemented)}]
    [view st/report-user-container
     [touchable-highlight {:on-press (fn []
                                       ;; TODO not implemented
                                       )}
      [text {:style st/report-user-text} (t :profile.report-user)]]]]])

(defview my-profile []
  [username     [:get :username]
   photo-path   [:get :photo-path]
   phone-number [:get :phone-number]
   email        [:get :email]
   status       [:get :status]]
  [scroll-view {:style st/profile}
   [touchable-highlight {:style    st/back-btn-touchable
                         :on-press #(dispatch [:navigate-back])}
    [view st/back-btn-container
     [icon :back st/back-btn-icon]]]
   [touchable-highlight {:style    st/actions-btn-touchable
                         :on-press (fn []
                                     ;; TODO not implemented
                                     )}
    [view st/actions-btn-container
     [icon :dots st/actions-btn-icon]]]
   [view st/status-block
    [view st/user-photo-container
     [my-profile-icon]]
    [text {:style st/user-name} username]
    [text {:style st/status} status]]
   [view st/profile-properties-container
    [profile-property-view {:name  (t :profile.username)
                            :value username}]
    [profile-property-view {:name  (t :profile.phone-number)
                            :value phone-number}]
    [profile-property-view {:name  (t :profile.email)
                            :value email}]]])

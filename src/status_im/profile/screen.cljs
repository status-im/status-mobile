(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                              text
                                              image
                                              icon
                                              scroll-view
                                              touchable-highlight
                                              touchable-opacity]]
            [status-im.resources :as res]
            [status-im.profile.styles :as st]))

(defn user-photo [{:keys [photo-path]}]
  [image {:source (if (s/blank? photo-path)
                    res/user-no-photo
                    {:uri photo-path})
          :style  st/user-photo}])

(defn user-online [{:keys [online]}]
  (when online
    [view st/user-online-container
     [view st/user-online-dot-left]
     [view st/user-online-dot-right]]))

(defn profile-property-view [{:keys [name value]}]
  [view st/profile-property-view-container
   [view st/profile-property-view-sub-container
    [text {:style st/profile-property-view-label} name]
    [text {:style st/profile-property-view-value} value]]])

(defn message-user [identity]
  (when identity
    (dispatch [:show-chat identity :push])))

(defview profile []
  [{:keys [name whisper-identity phone-number]} [:contact]]
  [scroll-view {:style st/profile}
   [touchable-highlight {:style    st/back-btn-touchable
                         :on-press #(dispatch [:navigate-back])}
    [view st/back-btn-container
     [icon :back st/back-btn-icon]]]
   [view st/status-block
    [view st/user-photo-container
     [user-photo {}]
     [user-online {:online true}]]
    [text {:style st/user-name} name]
    [text {:style st/status} "!not implemented"]
    [view st/btns-container
     [touchable-highlight {:onPress #(message-user whisper-identity)}
      [view st/message-btn
       [text {:style st/message-btn-text} "Message"]]]
     [touchable-highlight {:onPress (fn []
                                      ;; TODO not implemented
                                      )}
      [view st/more-btn
       [icon :more_vertical_blue st/more-btn-image]]]]]
   [view st/profile-properties-container
    [profile-property-view {:name  "Username"
                            :value name}]
    [profile-property-view {:name  "Phone number"
                            :value phone-number}]
    [profile-property-view {:name  "Email"
                            :value "!not implemented"}]
    [view st/report-user-container
     [touchable-opacity {}
      [text {:style st/report-user-text} "REPORT USER"]]]]])

(defview my-profile []
  [username [:get :username]
   phone-number [:get :phone-number]
   email [:get :email]
   status [:get :status]]
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
     [user-photo {}]
     [user-online {:online true}]]
    [text {:style st/user-name} username]
    [text {:style st/status} status]]
   [view st/profile-properties-container
    [profile-property-view {:name  "Username"
                            :value username}]
    [profile-property-view {:name  "Phone number"
                            :value phone-number}]
    [profile-property-view {:name  "Email"
                            :value email}]]])

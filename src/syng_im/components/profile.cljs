(ns syng-im.components.profile
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              text-input
                                              image
                                              icon
                                              scroll-view
                                              touchable-highlight
                                              touchable-opacity]]
            [syng-im.resources :as res]
            [syng-im.components.profile-styles :as st]))

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
    [text {:style st/profile-property-view-label}
     name]
    [text {:style st/profile-property-view-value}
     value]]])

(defn message-user [identity]
  (when identity
    (dispatch [:show-chat identity nil :push])))

(defn profile []
  (let [contact (subscribe [:contact])]
    (fn []
      [scroll-view {:style st/profile}
       [touchable-highlight {:style          st/profile-back-button-touchable
                             :on-press       #(dispatch [:navigate-back])
                             :underlay-color :transparent}
        [view st/profile-back-button-container
         [icon :back st/profile-back-button-icon]]]
       [view st/status-block
        [view st/user-photo-container
         [user-photo  {}]
         [user-online {:online true}]]
        [text {:style st/user-name}
         (:name @contact)]
        [text {:style st/status}
         "!not implemented"]
        [view st/btns-container
         [touchable-highlight {:onPress #(message-user (:whisper-identity @contact))
                               :underlay-color :transparent}
          [view st/message-btn
           [text {:style st/message-btn-text}
            "Message"]]]
         [touchable-highlight {:onPress (fn []
                                          ;; TODO not implemented
                                          )
                               :underlay-color :transparent}
          [view st/more-btn
           [icon :more_vertical_blue st/more-btn-image]]]]]
       [view st/profile-properties-container
        [profile-property-view {:name "Username"
                                :value (:name @contact)}]
        [profile-property-view {:name "Phone number"
                                :value (:phone-number @contact)}]
        [profile-property-view {:name "Email"
                                :value "!not implemented"}]
        [view st/report-user-container
         [touchable-opacity {}
          [text {:style st/report-user-text}
           "REPORT USER"]]]]])))

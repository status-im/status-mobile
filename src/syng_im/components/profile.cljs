(ns syng-im.components.profile
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              text-input
                                              image
                                              scroll-view
                                              navigator
                                              touchable-highlight
                                              touchable-opacity]]
            [syng-im.resources :as res]
            [syng-im.components.profile-styles :as st]
            [syng-im.navigation :refer [nav-pop]]))

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
  [view {:style st/profile-property-view-container}
   [view {:style st/profile-property-view-sub-container}
    [text {:style st/profile-property-view-label}
     name]
    [text {:style st/profile-property-view-value}
     value]]])

(defn profile [{:keys [navigator]}]
  (let [contact (subscribe [:contact])]
    (fn [{:keys [navigator]}]
      [scroll-view {:style st/profile}
       [touchable-highlight {:style          st/profile-back-button-touchable
                             :on-press       (fn []
                                               (nav-pop navigator))
                             :underlay-color :transparent}
        [view st/profile-back-button-container
         [image {:source {:uri "icon_back"}
                 :style  st/profile-back-button-icon}]]]
       [view {:style st/status-block}
        [view {:style st/user-photo-container}
         [user-photo  {}]
         [user-online {:online true}]]
        [text {:style st/user-name}
         (:name @contact)]
        [text {:style st/status}
         "!not implemented"]
        [view {:style st/btns-container}
         [touchable-highlight {:onPress (fn []
                                          ;; TODO not implemented
                                          )
                               :underlay-color :transparent}
          [view {:style st/message-btn}
           [text {:style st/message-btn-text}
            "Message"]]]
         [touchable-highlight {:onPress (fn []
                                          ;; TODO not implemented
                                          )
                               :underlay-color :transparent}
          [view {:style st/more-btn}
           [image {:source {:uri "icon_more_vertical_blue"}
                   :style  st/more-btn-image}]]]]]
       [view {:style st/profile-properties-container}
        [profile-property-view {:name "Username"
                                :value (:name @contact)}]
        [profile-property-view {:name "Phone number"
                                :value (:phone-number @contact)}]
        [profile-property-view {:name "Email"
                                :value "!not implemented"}]
        [view {:style st/report-user-container}
         [touchable-opacity {}
          [text {:style st/report-user-text}
           "REPORT USER"]]]]])))

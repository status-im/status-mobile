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
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-blue
                                               color-blue-transparent
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               text3-color]]
            [syng-im.navigation :refer [nav-pop]]))

(defn user-photo [{:keys [photo-path]}]
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        64
                    :height       64}}]])

(defn user-online [{:keys [online]}]
  (when online
    [view {:position        :absolute
           :top             44
           :left            44
           :width           24
           :height          24
           :borderRadius    50
           :backgroundColor online-color
           :borderWidth     2
           :borderColor     color-white}
     [view {:position        :absolute
            :top             8
            :left            5
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]
     [view {:position        :absolute
            :top             8
            :left            11
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]]))

(defn profile-property-view [{:keys [name value]}]
  [view {:style {:height            85
                 :paddingHorizontal 16}}
   [view {:borderBottomWidth 1
          :borderBottomColor separator-color}
    [text {:style {:marginTop  16
                   :fontSize   14
                   :fontFamily font
                   :color      text2-color}}
     name]
    [text {:style {:marginTop  11
                   :height     40
                   :fontSize   16
                   :fontFamily font
                   :color      text1-color}}
     value]]])

(defn profile [{:keys [navigator]} {:keys [name status phone-number email]}]
  [scroll-view {:style {:flex            1
                        :backgroundColor color-white
                        :flexDirection   :column}}
   [touchable-highlight {:style          {:position :absolute}
                         :on-press       (fn []
                                           (nav-pop navigator))
                         :underlay-color :transparent}
    [view {:width  56
           :height 56}
     [image {:source {:uri "icon_back"}
             :style  {:marginTop  21
                      :marginLeft 23
                      :width      8
                      :height     14}}]]]
   
   [view {:style {:alignSelf  :center
                  :alignItems :center
                  :width      249}}
    [view {:marginTop 26}
     [user-photo  {}]
     [user-online {:online true}]]
    [text {:style {:marginTop  20
                   :fontSize   18
                   :fontFamily font
                   :color      text1-color}}
     name]
    [text {:style {:marginTop  10
                   :fontFamily font
                   :fontSize   14
                   :lineHeight 20
                   :textAlign  :center
                   :color      text2-color}}
     status]
    [view {:style {:marginTop      18
                   :flexDirection  :row}}
     [touchable-highlight {:onPress (fn []
                                      ;; TODO not implemented
                                      )
                           :underlay-color :transparent}
      [view {:style {:height          40
                     :justifyContent  :center
                     :backgroundColor color-blue
                     :paddingLeft     25
                     :paddingRight    25
                     :borderRadius    50}}
       [text {:style {:marginTop  -2.5
                      :fontSize   14
                      :fontFamily font
                      :color      color-white}}
        "Message"]]]
     [touchable-highlight {:onPress (fn []
                                      ;; TODO not implemented
                                      )
                           :underlay-color :transparent}
      [view {:style {:marginLeft      10
                     :width           40
                     :height          40
                     :alignItems      :center
                     :justifyContent  :center
                     :backgroundColor color-blue-transparent
                     :padding         8
                     :borderRadius    50}}
       [image {:source {:uri "icon_more_vertical_blue"}
               :style  {:width  4
                        :height 16}}]]]]]
   [view {:style {:marginTop     20
                  :alignItems    :stretch
                  :flexDirection :column}}
    [profile-property-view {:name "Username"
                            :value name}]
    [profile-property-view {:name "Phone number"
                            :value phone-number}]
    [profile-property-view {:name "Email"
                            :value email}]
    [view {:style {:marginTop       50
                   :marginBottom    43
                   :alignItems :center}}
     [touchable-opacity {}
      [text {:style {:fontSize      14
                     :fontFamily    font
                     :lineHeight    21
                     :color         text2-color
                     ;; IOS:
                     :letterSpacing 0.5}}
       "REPORT USER"]]]]])

(ns syng-im.components.chats.chat-list-item-inner
  (:require [clojure.string :as s]
            [syng-im.components.react :refer [view image text]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-blue
                                               online-color
                                               text1-color
                                               text2-color
                                               new-messages-count-color]]
            [syng-im.resources :as res]))

(defn contact-photo [{:keys [photo-path]}]
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        40
                    :height       40}}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view {:position        "absolute"
           :top             24
           :left            24
           :width           20
           :height          20
           :borderRadius    50
           :backgroundColor online-color
           :borderWidth     2
           :borderColor     color-white}
     [view {:position        "absolute"
            :top             6
            :left            3
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]
     [view {:position        "absolute"
            :top             6
            :left            9
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]]))

(defn chat-list-item-inner-view
  [{:keys [name photo-path delivery-status timestamp new-messages-count online
           group-chat contacts]}]
  [view {:style {:flexDirection     "row"
                 :paddingVertical   15
                 :paddingHorizontal 16
                 :height            90}}
   [view {:marginTop 2
          :width     44
          :height    44}
;;; photo
    [contact-photo {:photo-path photo-path}]
;;; online
    [contact-online {:online online}]]
   [view {:style {:flexDirection "column"
                  :marginLeft    12
                  :flex          1}}
;;; name
    [view {:style {:flexDirection "row"}}
     [text {:style {:marginTop  -2.5
                    :color      text1-color
                    :fontSize   14
                    :fontFamily title-font}} name]
;;; group size
     (when group-chat
       [image {:source {:uri "icon_group"}
               :style  {:marginTop  4
                        :marginLeft 8
                        :width      14
                        :height     9}}])
     (when group-chat
       [text {:style {:marginTop  -0.5
                      :marginLeft 4
                      :fontFamily font
                      :fontSize   12
                      :color      text2-color}}
        (if (< 1 (count contacts))
          (str (count contacts) " members")
          "1 member")])]
;;; last message
    [text {:style {:marginTop    7
                   :marginRight  40
                   :color        text1-color
                   :fontFamily   font
                   :fontSize     14
                   :lineHeight   20}
           :numberOfLines 2}
     (repeatedly 5 #(str "Hi, I'm " name "! "))]]
   [view {}
;;; delivery status
    [view {:style {:flexDirection "row"
                   :position      "absolute"
                   :top           0
                   :right         0}}
     (when delivery-status
       [image {:source (if (= (keyword delivery-status) :seen)
                         {:uri "icon_ok_small"}
                         {:uri "icon_ok_small"})
               :style  {:marginTop 6
                        :width     9
                        :height    7}}])
;;; datetime
     [text {:style {:fontFamily font
                    :fontSize   12
                    :color      text2-color
                    :marginLeft 5}}
      timestamp]]
;;; new messages count
    (when (pos? new-messages-count)
      [view {:style {:position        "absolute"
                     :top             36
                     :right           0
                     :width           24
                     :height          24
                     :backgroundColor new-messages-count-color
                     :borderRadius    50}}
       [text {:style {:top        4
                      :left       0
                      :fontFamily title-font
                      :fontSize   10
                      :color      color-blue
                      :textAlign  "center"}}
        new-messages-count]])]])

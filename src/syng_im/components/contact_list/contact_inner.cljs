(ns syng-im.components.contact-list.contact-inner
  (:require [clojure.string :as s]
            [syng-im.components.react :refer [view image text]]
            [syng-im.components.styles :refer [font]]
            [syng-im.resources :as res]))

(defn contact-photo [{:keys [photo-path]}]
  [view {:width           54
         :height          54
         :borderRadius    50
         :backgroundColor "#FFFFFF"
         :elevation       6}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderWidth  2
                    :borderColor  "#FFFFFF"
                    :borderRadius 50
                    :width        54
                    :height       54
                    :position     "absolute"}}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view {:position        "absolute"
           :top             41
           :left            36
           :width           12
           :height          12
           :borderRadius    50
           :backgroundColor "#FFFFFF"
           :elevation       6}
     [image {:source res/online-icon
             :style  {:width  12
                      :height 12}}]]))

(defn contact-inner-view [{:keys [name photo-path delivery-status datetime new-messages-count
                                  online whisper-identity]}]
  [view {:style {:flexDirection "row"
                 :marginTop     5
                 :marginBottom  5
                 :paddingLeft   15
                 :paddingRight  15
                 :height        75}}
   [view {:width  54
          :height 54}
    ;;; photo
    [contact-photo {:photo-path photo-path}]
    ;;; online
    [contact-online {:online online}]]
   [view {:style {:flexDirection "column"
                  :marginLeft    7
                  :marginRight   10
                  :flex          1
                  :position      "relative"}}
    ;;; name
    [text {:style {:fontSize   15
                   :fontFamily font}} name]
    ;;; last message
    [text {:style {:color        "#AAB2B2"
                   :fontFamily   font
                   :fontSize     14
                   :marginTop    2
                   :paddingRight 10}}
     (str "Hi, I'm " name)]]
   [view {:style {:flexDirection "column"}}
    ;;; delivery status
    [view {:style {:flexDirection "row"
                   :position      "absolute"
                   :top           0
                   :right         0}}
     (when delivery-status
       [image {:source (if (= (keyword delivery-status) :seen)
                         res/seen-icon
                         res/delivered-icon)
               :style  {:marginTop 5}}])
     ;;; datetime
     [text {:style {:fontFamily    font
                    :fontSize      11
                    :color         "#AAB2B2"
                    :letterSpacing 1
                    :lineHeight    15
                    :marginLeft    5}}
      datetime]]
    ;;; new messages count
    (when (< 0 new-messages-count)
      [view {:style {:position        "absolute"
                     :right           0
                     :bottom          24
                     :width           18
                     :height          18
                     :backgroundColor "#6BC6C8"
                     :borderColor     "#FFFFFF"
                     :borderRadius    50
                     :alignSelf       "flex-end"}}
       [text {:style {:width      18
                      :height     17
                      :fontFamily font
                      :fontSize   10
                      :color      "#FFFFFF"
                      :lineHeight 19
                      :textAlign  "center"
                      :top        1}}
        new-messages-count]])]])


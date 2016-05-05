(ns syng-im.components.contact-list.contact-inner
  (:require [clojure.string :as s]
            [syng-im.components.react :refer [view image text]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               text1-color
                                               color-white
                                               online-color]]
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

(defn contact-inner-view [{:keys [name photo-path delivery-status datetime new-messages-count
                                  online whisper-identity]}]
  [view {:style {:flexDirection "row"
                 :height        56}}
   [view {:style {:marginTop 8
                  :marginLeft 16
                  :width     44
                  :height    44}}
;;; photo
    [contact-photo {:photo-path photo-path}]
;;; online
    [contact-online {:online online}]]
;;; name
   [view {:style {:justifyContent "center"}}
    [text {:style {:marginLeft 16
                   :fontSize   16
                   :fontFamily font
                   :color      text1-color}}
     (if (pos? (count name))
       name
       "Noname")]]])

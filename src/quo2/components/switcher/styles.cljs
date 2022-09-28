(ns quo2.components.switcher.styles
  (:require [quo2.foundations.colors :as colors]))

(def colors-map
  {:secondary-container-bg-color   colors/neutral-95
   :title-color                    colors/white
   :subtitle-color                 colors/neutral-40
   :last-message-text-color        colors/white
   :close-button-bg-color          colors/neutral-80-opa-40
   :close-button-icon-color        colors/white
   :community-channel              colors/white})

(defn base-container [background-color]
  {:width            160
   :height           160
   :border-radius    16
   :background-color (colors/alpha background-color 0.4)})

(defn secondary-container []
  {:width            160
   :height           120
   :border-radius    16
   :bottom           0
   :position         :absolute
   :background-color (:secondary-container-bg-color colors-map)})

(defn title []
  {:position          :absolute
   :top               28
   :margin-horizontal 12
   :color             (:title-color colors-map)})

(defn title-props []
  {:size            :paragraph-1
   :weight          :semi-bold
   :number-of-lines 1
   :ellipsize-mode  :tail
   :style           (title)})

(defn subtitle []
  {:position          :absolute
   :top               50
   :margin-horizontal 12
   :color             (:subtitle-color colors-map)})

(defn subtitle-props []
  {:size            :paragraph-2
   :weight          :medium
   :style           (subtitle)})

(defn content-container [new-notifications?]
  {:position          :absolute
   :max-width         (if new-notifications? 108 136)
   :flex-shrink       1
   :bottom            12
   :margin-left       12
   :margin-right      (if new-notifications? 8 12)})

(defn notification-container []
  {:position        :absolute
   :width           20
   :height          20
   :bottom          12
   :right           12
   :justify-content :center
   :align-items     :center})

(defn last-message-text []
  {:color (:last-message-text-color colors-map)})

(defn last-message-text-props []
  {:size            :paragraph-2
   :weight          :regular
   :number-of-lines 1
   :ellipsize-mode  :tail
   :style           (last-message-text)})

(defn close-button []
  {:position         :absolute
   :right            8
   :top              8
   :background-color (:close-button-bg-color colors-map)
   :icon-color       (:close-button-icon-color colors-map)})

(defn close-button-props [on-press]
  {:size           24
   :type           :grey
   :icon           true
   :on-press       on-press
   :override-theme :dark
   :style          (close-button)})

(defn avatar-container []
  {:width    48
   :height   48
   :left     12
   :top      12
   :position :absolute})

(defn unread-dot [background-color]
  {:width            8
   :height           8
   :border-radius    4
   :background-color background-color})

;; Supporting Components

(defn sticker []
  {:width  24
   :height 24})

(defn gif []
  {:width         24
   :height        24
   :border-radius 8})

(defn community-avatar []
  {:width         48
   :height        48
   :border-radius 24})

(defn community-channel []
  {:margin-left 8
   :color       (:community-channel colors-map)})

(defn community-channel-props []
  {:size            :paragraph-2
   :weight          :medium
   :number-of-lines 1
   :ellipsize-mode  :tail
   :style           (community-channel)})

(ns status-im2.contexts.shell.components.switcher-cards.style
  (:require [quo2.foundations.colors :as colors]))

(def colors-map
  {:secondary-container-bg-color colors/neutral-95
   :title-color                  colors/white
   :subtitle-color               colors/neutral-40
   :last-message-text-color      colors/white
   :close-button-bg-color        colors/neutral-80-opa-40
   :close-button-icon-color      colors/white
   :community-channel            colors/white})

(defn base-container
  [background-color]
  {:width            160
   :height           160
   :border-radius    16
   :background-color (colors/alpha background-color 0.4)})

(defn empty-card
  []
  (merge
   (base-container nil)
   {:background-color colors/neutral-95}))

(def secondary-container
  {:width            160
   :height           120
   :border-radius    16
   :bottom           0
   :position         :absolute
   :background-color (:secondary-container-bg-color colors-map)})

(def title
  {:position          :absolute
   :top               28
   :margin-horizontal 12
   :color             (:title-color colors-map)})

(def subtitle
  {:position          :absolute
   :top               50
   :margin-horizontal 12
   :color             (:subtitle-color colors-map)})

(defn content-container
  [new-notifications?]
  {:position     :absolute
   :max-width    (if new-notifications? 108 136)
   :flex-shrink  1
   :bottom       12
   :margin-left  12
   :margin-right (if new-notifications? 8 12)})

(def notification-container
  {:position        :absolute
   :width           20
   :height          20
   :bottom          12
   :right           12
   :justify-content :center
   :align-items     :center})

(def last-message-text
  {:color (:last-message-text-color colors-map)})

(def close-button
  {:position         :absolute
   :right            8
   :top              8
   :background-color (:close-button-bg-color colors-map)
   :icon-color       (:close-button-icon-color colors-map)})

(def avatar-container
  {:width           48
   :height          48
   :left            12
   :top             12
   :border-radius   26
   :border-width    26
   :border-color    colors/neutral-95
   :justify-content :center
   :align-items     :center
   :position        :absolute})

(defn unread-dot
  [background-color]
  {:width            8
   :height           8
   :border-radius    4
   :background-color background-color})

;; Supporting Components

(def sticker
  {:width  24
   :height 24})

(def gif
  {:width         24
   :height        24
   :border-radius 8})

(defn community-avatar
  [customization-color]
  {:width            48
   :height           48
   :border-radius    24
   ;; TODO - Update to fall back community avatar once designs are available
   :justify-content  :center
   :align-items      :center
   :background-color (colors/custom-color
                      (or customization-color :primary)
                      60)})

(def community-channel
  {:margin-left 8
   :color       (:community-channel colors-map)})

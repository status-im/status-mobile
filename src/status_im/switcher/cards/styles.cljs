(ns status-im.switcher.cards.styles
  (:require [quo.theme :as theme]
            [quo2.foundations.colors :as colors]))

(def themes
  {:light {:messaging-card-container-background-color           "#26A69A"
           :messaging-card-secondary-container-background-color colors/white
           :messaging-card-title-color                          colors/black
           :messaging-card-subtitle-color                       colors/neutral-50
           :messaging-card-last-message-text-color              colors/black
           :messaging-card-close-button-bg-color                colors/white-opa-50
           :messaging-card-close-button-icon-color              colors/black}
   :dark  {:messaging-card-container-background-color           "#26A69A"
           :messaging-card-secondary-container-background-color colors/neutral-90
           :messaging-card-title-color                          colors/white
           :messaging-card-subtitle-color                       colors/neutral-40
           :messaging-card-last-message-text-color              colors/white
           :messaging-card-close-button-bg-color                colors/neutral-80-opa-60
           :messaging-card-close-button-icon-color              colors/white}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

;; Messaging Card

(defn messaging-card-main-container []
  {:width            160
   :height           172
   :border-radius    16
   :margin           8
   :background-color (get-color :messaging-card-container-background-color)})

(defn messaging-card-secondary-container []
  {:width            160
   :height           132
   :background-color (get-color :messaging-card-secondary-container-background-color)
   :border-radius    16
   :position         :absolute
   :bottom           0})

(defn messaging-card-title []
  {:position          :absolute
   :top               32
   :margin-horizontal 12
   :color             (get-color :messaging-card-title-color)})

(defn messaging-card-title-props []
  {:size            :paragraph-1
   :weight          :semi-bold
   :number-of-lines 1
   :ellipsize-mode  :tail
   :style           (messaging-card-title)})

(defn messaging-card-subtitle []
  {:position          :absolute
   :top               54
   :margin-horizontal 12
   :color             (get-color :messaging-card-subtitle-color)})

(defn messaging-card-subtitle-props []
  {:size            :paragraph-2
   :weight          :medium
   :style           (messaging-card-subtitle)})

(defn messaging-card-details-container []
  {:position          :absolute
   :bottom            12
   :margin-horizontal 12
   :width             136
   :height            36})

(defn messaging-card-last-message-text []
  {:color           (get-color :messaging-card-last-message-text-color)})

(defn messaging-card-last-message-text-props []
  {:size            :paragraph-2
   :weight          :regular
   :number-of-lines 2
   :ellipsize-mode  :tail
   :style           (messaging-card-last-message-text)})

(defn messaging-card-close-button []
  {:position         :absolute
   :right            8
   :top              8
   :background-color (get-color :messaging-card-close-button-bg-color)
   :icon-color       (get-color :messaging-card-icon-color)})

(defn messaging-card-close-button-props []
  {:size  24
   :type  :grey
   :icon  true
   :on-press #(print "close pressed")
   :style (messaging-card-close-button)})

(defn messaging-card-avatar-container []
  {:width    48
   :height   48
   :border-radius 24
   :position :absolute
   :left     12
   :top      16
   :background-color :pink})

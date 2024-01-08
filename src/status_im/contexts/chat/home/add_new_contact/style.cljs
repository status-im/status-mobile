(ns status-im.contexts.chat.home.add-new-contact.style
  (:require [quo.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(defn container-outer
  []
  {:flex               1
   :background-color   (colors/theme-colors colors/white colors/neutral-95)
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 20})

(def container-inner {:align-items :flex-start})

(def container-invalid
  {:style {:flex-direction :row
           :align-items    :center
           :margin-top     8}})

(defn text-title
  []
  {:size   :heading-1
   :weight :semi-bold
   :style  {:margin-top    32
            :margin-bottom 6
            :color         (colors/theme-colors colors/neutral-100 colors/white)}})

(defn text-subtitle
  []
  {:size   :paragraph-1
   :weight :regular
   :style  {:margin-bottom 20
            :color         (colors/theme-colors colors/neutral-100 colors/white)}})

(defn text-description
  []
  {:size   :paragraph-2
   :weight :medium
   :style  {:margin-bottom 6
            :color         (colors/theme-colors colors/neutral-50 colors/neutral-40)}})

(def icon-invalid
  {:size  16
   :color colors/danger-50})

(def text-invalid
  {:size  :paragraph-2
   :align :left
   :style {:margin-left 4
           :color       colors/danger-50}})

(def input-and-scan-container
  {:flex-direction :row
   :align-items    :flex-start})

(def scan-button-container
  {:margin-left     12
   :height          66
   :justify-content :flex-end})

(def found-user
  {:padding-top    16
   :flex-direction :column
   :align-self     :stretch})

(defn found-user-container
  []
  {:flex-direction   :row
   :align-items      :center
   :padding-top      8
   :padding-left     12
   :padding-right    12
   :padding-bottom   8
   :color            (colors/theme-colors
                      colors/black
                      colors/white)
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-95)
   :border-width     1
   :border-radius    12
   :border-color     (colors/theme-colors
                      colors/neutral-20
                      colors/neutral-80)})

(def found-user-text
  {:margin-left    8
   :flex-direction :column})

(defn found-user-display-name
  []
  {:color (colors/theme-colors
           colors/black
           colors/white)})

(defn found-user-key
  []
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40)})

(def button-view-profile
  {:margin-top    24
   :width         "100%"
   :margin-bottom (+ (safe-area/get-bottom) 12)})

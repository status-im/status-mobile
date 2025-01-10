(ns status-im.contexts.chat.home.add-new-contact.style
  (:require [quo.foundations.colors :as colors]))

(defn container-outer
  [theme]
  {:flex               1
   :background-color   (colors/theme-colors colors/white colors/neutral-95 theme)
   :margin-top         2
   :padding-horizontal 20})

(def container-invalid
  {:flex-direction :row
   :margin-top     8})

(defn text-description
  [theme]
  {:margin-bottom 6
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def icon-invalid
  {:size  16
   :color colors/danger-50})

(def text-invalid
  {:margin-left 4
   :color       colors/danger-50})

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
  [theme]
  {:flex-direction   :row
   :align-items      :center
   :padding-top      8
   :padding-left     12
   :padding-right    12
   :padding-bottom   8
   :color            (colors/theme-colors
                      colors/black
                      colors/white
                      theme)
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-95
                      theme)
   :border-width     1
   :border-radius    12
   :border-color     (colors/theme-colors
                      colors/neutral-20
                      colors/neutral-80
                      theme)})

(def found-user-text
  {:margin-left    8
   :flex-direction :column})

(defn found-user-display-name
  [theme]
  {:color (colors/theme-colors
           colors/black
           colors/white
           theme)})

(defn found-user-key
  [theme]
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40
           theme)})



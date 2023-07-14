(ns status-im2.contexts.add-new-contact.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]))

(defn container-outer
  []
  {:style {:flex             1
           :background-color (colors/theme-colors colors/white colors/neutral-95)
           :justify-content  :space-between
           :align-items      :center
           :padding          16
           :border-radius    20}})

(def container-inner
  {:style {:flex-direction :column
           :align-items    :flex-start}})

(def container-text-input
  {:style {:flex-direction  :row
           :justify-content :space-between}})

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
            :color         (colors/theme-colors
                            colors/neutral-100
                            colors/white)}})

(defn text-subtitle
  []
  {:size   :paragraph-1
   :weight :regular
   :style  {:margin-bottom 20
            :color         (colors/theme-colors
                            colors/neutral-100
                            colors/white)}})

(defn text-description
  []
  {:size   :paragraph-2
   :weight :medium
   :style  {:margin-bottom 6
            :color         (colors/theme-colors
                            colors/neutral-50
                            colors/neutral-40)}})

(def icon-invalid
  {:size  16
   :color colors/danger-50})

(def text-invalid
  {:size  :paragraph-2
   :align :left
   :style {:margin-left 4
           :color       colors/danger-50}})

(defn text-input-container
  [invalid?]
  {:style {:padding-top      1
           :padding-left     12
           :padding-right    7
           :padding-bottom   7
           :margin-right     10
           :flex             1
           :flex-direction   :row
           :background-color (colors/theme-colors
                              colors/white
                              colors/neutral-95)
           :border-width     1
           :border-radius    12
           :border-color     (if invalid?
                               colors/danger-50-opa-40
                               (colors/theme-colors
                                colors/neutral-20
                                colors/neutral-80))}})

(defn text-input
  []
  {:accessibility-label :enter-contact-code-input
   :auto-capitalize :none
   :placeholder-text-color (colors/theme-colors
                            colors/neutral-40
                            colors/neutral-50)
   :multiline true
   :style
   (merge typography/monospace
          typography/paragraph-1
          {:flex         1
           :margin-right 5
           :margin-top   (if platform/android?
                           4
                           0)
           :padding      0
           :color        (colors/theme-colors
                          colors/black
                          colors/white)})})

(def button-paste
  {:type  :outline
   :size  24
   :style {:margin-top 6}})

(defn button-close
  []
  {:type                      :grey
   :icon                      true
   :accessibility-label       :new-contact-close-button
   :size                      32
   :override-background-color (colors/theme-colors
                               colors/neutral-10
                               colors/neutral-90)})

(def button-qr
  {:type :outline
   :icon true
   :size 40})

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

(defn button-view-profile
  [state]
  {:type                :primary
   :size                40
   :width               335
   :style               {:margin-top    24
                         :margin-bottom (+ (safe-area/get-bottom) 12)}
   :accessibility-label :new-contact-button
   :before              :i/profile
   :disabled            (not= state :valid)})

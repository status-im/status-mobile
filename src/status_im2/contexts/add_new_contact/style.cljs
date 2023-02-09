(ns status-im2.contexts.add-new-contact.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [quo2.foundations.typography :as typography]))

(defn container-kbd
  []
  {:style                  {:flex 1}
   :keyboardVerticalOffset 60})

(def container-image
  {:style {:flex           1
           :flex-direction :row}})

(def image
  {:flex 1})

(defn container-outer
  []
  {:style {:flex             (if platform/ios? 4.5 5)
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

(def container-error
  {:style {:flex-direction :row
           :align-items    :center
           :margin-top     8}})

(defn text-title
  []
  {:size   :heading-1
   :weight :semi-bold
   :style  {:margin-bottom 6
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

(def icon-error
  {:size  16
   :color colors/danger-50})

(def text-error
  {:size  :paragraph-2
   :align :left
   :style {:margin-left 4
           :color       colors/danger-50}})

(defn text-input-container
  [error?]
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
           :border-color     (if error?
                               colors/danger-50-opa-40
                               (colors/theme-colors
                                colors/neutral-20
                                colors/neutral-80))}})

(defn text-input
  []
  {:accessibility-label    :enter-contact-code-input
   :auto-capitalize        :none
   :placeholder-text-color (colors/theme-colors
                            colors/neutral-40
                            colors/neutral-50)
   :multiline              true
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

(defn button-paste
  []
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
                               colors/white-opa-60
                               colors/neutral-80-opa-60)
   :style                     {:position :absolute
                               :left     20
                               :top      20}})

(def button-qr
  {:type :outline
   :icon true
   :size 40})

(defn button-view-profile
  [state]
  {:type                :primary
   :size                40
   :width               335
   :style               {:margin-top    24
                         :margin-bottom 24}
   :accessibility-label :new-contact-button
   :before              :i/profile
   :disabled            (not= state :valid)})

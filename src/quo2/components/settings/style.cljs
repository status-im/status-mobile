(ns quo2.components.settings.style
  (:require
   [quo2.foundations.colors :as colors]))

(def bullet-container
  {:width        20
   :height       20
   :margin-right 8
   :align-self   :flex-start})

(defn bullet
  []
  {:background-color (colors/theme-colors colors/neutral-40 colors/neutral-50)
   :border-radius    100
   :width            8
   :height           8
   :position         :absolute
   :left             6
   :top              6})

(def list-container
  {:margin-right 12})

(def list-item
  {:flex               1
   :flex-direction     :row
   :align-items        :center
   :padding-vertical   6
   :padding-horizontal 12})

(defn selection-indicator-container
  [active?]
  {:height        20
   :width         20
   :border-radius 20
   :border-width  1
   :border-color  (if active?
                    (colors/theme-colors colors/primary-50 colors/primary-60)
                    (colors/theme-colors colors/neutral-20 colors/neutral-80))})

(defn selection-indicator
  [active?]
  {:margin-left      :auto
   :height           14
   :width            14
   :background-color (if active?
                       (colors/theme-colors colors/primary-50 colors/primary-60)
                       (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40))
   :border-radius    20
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})

(def card-footer-label-container
  {:flex        0.9
   :margin-left 16})

(def card-footer-toggle-container
  {:flex              0.1
   :margin-horizontal 12
   :align-self        :center})

(defn card-footer
  []
  {:flex              1
   :flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 12
   :margin-top        8
   :margin-bottom     12
   :padding-vertical  12
   :border-radius     12
   :border-width      1
   :background-color  (colors/theme-colors colors/neutral-5 colors/neutral-90)
   :border-color      (colors/theme-colors colors/neutral-10 colors/neutral-80)})

(def card-header-container
  {:flex-direction     :row
   :align-items        :center
   :padding-top        12
   :padding-bottom     8
   :padding-horizontal 12})

(def card-header-label-container
  {:flex        1
   :margin-left 4})

(defn privacy-option-card
  [active?]
  {:border-radius 16
   :border-width  1
   :border-color  (if active?
                    (colors/theme-colors colors/primary-50 colors/primary-60)
                    (colors/theme-colors colors/neutral-10 colors/neutral-80))})

(ns status-im.ui2.screens.chat.composer.style
  (:require [quo2.foundations.typography :as quo2.typography]
            [quo.design-system.colors :as quo.colors]
            [status-im.utils.platform :as platform]
            [quo2.foundations.colors :as colors]))

(defn text-input []
  (merge quo2.typography/font-regular
         quo2.typography/paragraph-1
         {:flex              1
          :min-height        34
          :margin            0
          :flex-shrink       1
          :color             (:text-01 @quo.colors/theme)
          :margin-horizontal 20}
         (if platform/android?
           {:padding-vertical    8
            :text-align-vertical :top}
           {:margin-top    8
            :margin-bottom 8})))

(defn input-bottom-sheet [window-height]
  (merge {:border-top-left-radius  20
          :border-top-right-radius 20
          :position                :absolute
          :left                    0
          :right                   0
          :bottom                  (- window-height)
          :height                  window-height
          :flex                    1
          :background-color        (colors/theme-colors colors/white colors/neutral-90)
          :z-index                 2}
         (if platform/ios?
           {:shadow-radius  16
            :shadow-opacity 1
            :shadow-color   "rgba(9, 16, 28, 0.04)"
            :shadow-offset  {:width 0 :height -2}}
           {:elevation 2})))

(defn bottom-sheet-handle []
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white)
   :opacity          0.05
   :border-radius    100
   :align-self       :center
   :margin-top       8})

(defn bottom-sheet-controls [insets]
  {:flex-direction     :row
   :padding-horizontal 20
   :elevation          2
   :z-index            3
   :position           :absolute
   :background-color   (colors/theme-colors colors/white colors/neutral-90)
   ;these 3 props play together, we need this magic to hide message text in the safe area
   :padding-top        10
   :padding-bottom     (+ 12 (:bottom insets))
   :bottom             (- 2 (:bottom insets))})

(defn bottom-sheet-background [window-height]
  {:pointerEvents    :none
   :position         :absolute
   :left             0
   :right            0
   :bottom           0
   :height           window-height
   :background-color colors/neutral-95-opa-70
   :z-index          1})

(defn reply-content [pin?]
  {:padding-horizontal (when-not pin? 10)
   :flex               1
   :flex-direction     :row})

(defn quoted-message [pin?]
  (merge {:flex-direction :row
          :align-items    :center
          :width          "45%"}
         (when-not pin? {:position :absolute
                         :left     34
                         :top      3})))

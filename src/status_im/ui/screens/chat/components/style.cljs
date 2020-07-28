(ns status-im.ui.screens.chat.components.style
  (:require [quo.platform :as platform]
            [quo.design-system.colors :as colors]
            [quo.design-system.typography :as typography]))

(defn toolbar []
  {:min-height       52
   :padding-vertical 8
   :border-top-width 1
   :border-top-color (:ui-01 @colors/theme)
   :background-color (:ui-background @colors/theme)
   :align-items      :flex-end
   :flex-direction   :row})

(defn input-container []
  {:background-color           (:ui-01 @colors/theme)
   :flex                       1
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-right-radius 4
   :border-bottom-left-radius  16
   :margin-horizontal          8})

(defn input-row []
  {:flex-direction :row
   :align-items    :flex-end})

(defn text-input-wrapper []
  (merge {:flex-direction :row
          :align-items    :flex-start
          :flex           1
          :min-height     34
          :max-height     144}
         (when platform/ios?
           {:padding-top 2})))

(defn text-input []
  (merge typography/font-regular
         typography/base
         {:flex               1
          :margin             0
          :border-width       0
          :flex-shrink        1
          :color              (:text-01 @colors/theme)
          :padding-horizontal 12}
         (if platform/android?
           {:padding-vertical 2}
           {:padding-top    2
            :padding-bottom 6})))

(defn actions-wrapper [invisible]
  {:flex-direction :row
   :padding-left   4
   :min-height     34
   :left           (if invisible -88 0)})

(defn touchable-icon []
  {:padding-horizontal 10
   :padding-vertical   5
   :justify-content    :center
   :align-items        :center})

(defn in-input-touchable-icon []
  {:padding-horizontal 6
   :padding-vertical   5
   :justify-content    :center
   :align-items        :center})

(defn icon [active]
  {:color (if active
            (:icon-04 @colors/theme)
            (:icon-02 @colors/theme))})

(defn reply-container [image]
  {:border-top-left-radius     14
   :border-top-right-radius    14
   :border-bottom-right-radius 4
   :border-bottom-left-radius  14
   :margin                     2
   :flex-direction             :row
   :background-color           (if image
                                 (:ui-03 @colors/theme)
                                 (:ui-02 @colors/theme))})

(defn reply-content []
  {:padding-vertical   6
   :padding-horizontal 10
   :flex               1})

(defn close-button []
  {:padding 4})

(defn send-message-button []
  {:margin-vertical   4
   :margin-horizontal 5})

(defn send-message-container []
  {:background-color  (:interactive-01 @colors/theme)
   :width             26
   :height            26
   :border-radius     13
   :justify-content   :center
   :align-items       :center})

(defn in-input-buttons []
  {:flex-direction :row
   :height 34})

(defn send-icon-color []
  colors/white)

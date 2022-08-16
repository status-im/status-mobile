(ns status-im.ui.screens.chat.components.style
  (:require [quo.platform :as platform]
            [quo.design-system.colors :as colors]
            [quo.design-system.typography :as typography]))

(defn toolbar []
  {:min-height       52
   :padding-vertical 8
   :border-top-width 1
   :border-top-color (:ui-01 @colors/theme)
   :align-items      :flex-end
   :flex-direction   :row})

(defn input-container [contact-request]
  {:background-color           (:ui-01 @colors/theme)
   :flex                       1
   :height                     (when contact-request 44)
   :border-top-left-radius     (if contact-request 8 16)
   :border-top-right-radius    (if contact-request 8 16)
   :border-bottom-right-radius (if contact-request 8 4)
   :border-bottom-left-radius  (if contact-request 8 16)
   :margin-horizontal          8})

(def input-row
  {:flex-direction :row
   :overflow       :hidden
   :align-items    :flex-end})

(defn text-input-wrapper []
  (merge {:flex-direction :row
          :align-items    :flex-start
          :flex           1
          :min-height     34
          :max-height     144}
         (when platform/ios?
           {:padding-top 2})))

(defn text-input [contact-request]
  (merge typography/font-regular
         typography/base
         {:flex               1
          :min-height         34
          :max-height         144
          :margin             0
          :flex-shrink        1
          :color              (:text-01 @colors/theme)
          :padding-horizontal 12}
         (if platform/android?
           {:padding-vertical 2}
           {:padding-top    (if contact-request 10 2)
            :padding-bottom (if contact-request 5 6)})))

(defn actions-wrapper [show-send]
  (merge
   (when show-send
     {:width 0 :left -88})
   {:flex-direction :row
    :padding-left   4
    :min-height     34}))

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

(defn reply-container-image []
  {:border-top-left-radius     14
   :border-top-right-radius    14
   :border-bottom-right-radius 4
   :border-bottom-left-radius  14
   :margin                     2
   :flex-direction             :row
   :background-color           (:ui-03 @colors/theme)})

(defn reply-container []
  {:flex-direction             :row})

(defn reply-content-old []
  {:padding-vertical   6
   :padding-horizontal 10
   :flex               1})

(defn reply-content []
  {:padding-horizontal 10
   :flex               1
   :flex-direction     :row})

(defn contact-request-content []
  {:flex               1
   :flex-direction     :row
   :justify-content    :space-between})

(defn close-button []
  {:margin-top 3})

(defn send-message-button []
  {:margin-vertical   4
   :margin-horizontal 5})

(defn send-message-container [contact-request]
  {:background-color (:interactive-01 @colors/theme)
   :width            26
   :height           (if contact-request 44 26)
   :border-radius    (if contact-request 22 13)
   :justify-content  :center
   :align-items      :center})

(defn send-icon-color []
  colors/white)

(defn autocomplete-container [bottom]
  {:position         :absolute
   :left             0
   :right            0
   :bottom           bottom
   :background-color (colors/get-color :ui-background)
   :border-top-width 1
   :border-top-color (colors/get-color :ui-01)})

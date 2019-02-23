(ns status-im.ui.screens.network-settings.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]))

(def wrapper
  {:flex 1})

(defstyle badge-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16}})

(defstyle badge-connected-text
  {:color   colors/gray
   :ios     {:margin-top 5}
   :android {:font-size 13}})

(defstyle paste-json-text-input
  {:ios {:font-size      17
         :line-height    24
         :letter-spacing -0.2}})

(def connect-button-container
  {:margin-top        8
   :margin-bottom     16
   :margin-horizontal 16})

(defstyle connect-button
  {:height           52
   :align-items      :center
   :justify-content  :center
   :background-color colors/blue
   :border-radius    8
   :ios              {:opacity 0.9}})

(defstyle connect-button-label
  {:color   colors/white
   :ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 14}})

(defstyle connect-button-description
  {:color   colors/gray
   :ios     {:margin-top 8
             :height     20}
   :android {:margin-top 12
             :font-size  12}})

(defstyle network-config-container
  {:height            160
   :margin-top        8
   :padding-top       16
   :padding-left      16
   :margin-horizontal 16
   :background-color  colors/gray-lighter
   :ios               {:border-radius 9
                       :opacity       0.9}
   :android           {:border-radius 4}})

(defstyle network-config-text
  {:color   colors/black
   :ios     {:opacity        0.8
             :font-size      17
             :line-height    24
             :letter-spacing -0.2}
   :android {:opacity     0.4
             :font-size   16
             :line-height 24}})

(def edit-button-container
  {:margin-top        16
   :align-items       :center
   :margin-bottom     16
   :margin-horizontal 16})

(defstyle edit-button
  {:height           52
   :align-items      :center
   :justify-content  :center
   :background-color colors/blue
   :border-radius    8
   :ios              {:width 343}

   :android          {:width 328}})

(defstyle edit-button-label
  {:color   colors/blue
   :ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 14}})

(defstyle edit-button-description
  {:text-align :center
   :color      colors/gray
   :ios        {:margin-top     8
                :font-size      14
                :letter-spacing -0.2}
   :android    {:margin-top 12
                :font-size  12}})

(defn network-icon [connected? size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color (if connected? colors/blue colors/gray-lighter)
   :align-items      :center :justify-content :center})

(def network-badge
  {:height         88
   :padding-left   16
   :flex-direction :row
   :align-items    :center})

(defstyle network-item
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(defstyle network-item-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :android {:font-size 16}})

(defstyle network-item-connected-text
  {:color   colors/gray
   :ios     {:font-size      14
             :margin-top     6
             :letter-spacing -0.2}
   :android {:font-size  12
             :margin-top 2}})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def delete-button
  {:background-color colors/white})

(def delete-button-text
  {:color colors/red})

(def container
  (merge components.styles/flex
         {:background-color colors/white}))

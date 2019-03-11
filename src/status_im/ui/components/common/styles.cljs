(ns status-im.ui.components.common.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(def gradient-top
  {:flex-direction   :row
   :height           3
   :background-color colors/gray-lighter})

(def gradient-top-colors
  ["rgba(25, 53, 76, 0.01)"
   "rgba(25, 53, 76, 0.1)"])

(def gradient-bottom
  {:flex-direction   :row
   :height           2
   :background-color colors/gray-lighter})

(def gradient-bottom-colors
  ["rgba(25, 53, 76, 0.1)"
   "rgba(25, 53, 76, 0.01)"])

(def separator-wrapper
  {:background-color colors/white})

(defstyle separator
  {:android {:height 0}
   :ios     {:height           1
             :background-color colors/gray-light
             :opacity          0.5}})

(def list-separator
  {:margin-left 72})

(defstyle form-title-container
  {:flex-direction :row})

(defstyle form-title-inner-container
  {:padding-left   16
   :padding-right  16
   :flex           1
   :flex-direction :row
   :ios            {:background-color colors/white
                    :padding-top      19
                    :padding-bottom   15
                    :margin-top       16}
   :android        {:background-color colors/gray-lighter
                    :padding-top      20
                    :padding-bottom   17
                    :margin-top       8}})

(def form-title
  {:flex-shrink 1
   :color       colors/text
   :font-size   16})

(def form-title-count
  (merge form-title
         {:flex-shrink   0
          :opacity       0.6
          :padding-left  8
          :padding-right 5
          :color         colors/text-gray}))

(defstyle list-header-footer-spacing
  {:android {:background-color colors/white
             :height           8}})

(def network-container
  {:flex-direction     :row
   :padding-horizontal 13
   :padding-vertical   11
   :align-items        :center})

(defn network-text [text-color]
  {:flex        1
   :color       (or text-color colors/black)
   :font-size   14
   :margin-left 16})

(def network-icon
  {:width            40
   :height           40
   :border-radius    (/ 40 2)
   :background-color colors/green
   :align-items      :center
   :justify-content  :center})

(def label-action-text
  {:color     colors/blue
   :font-size 15})

(defn logo-container [size]
  {:width            size
   :height           size
   :border-radius    size
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn logo [icon-size]
  {:color  :white
   :width  icon-size
   :height icon-size})

(defn bottom-button [disabled?]
  {:flex-direction :row
   :align-items    :center
   :opacity        (if disabled? 0.4 1)})

(def bottom-button-label
  {:font-size 15
   :color     colors/blue})

(defn button [style background? disabled?]
  (merge
   {:padding-vertical   12
    :padding-horizontal 42
    :border-radius      8
    :background-color   (cond disabled?
                              colors/gray-lighter
                              background?
                              (colors/alpha colors/blue 0.1))}
   style))

(def button-label
  {:font-size  15
   :text-align :center
   :color      colors/blue})

(defn counter-container [size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defnstyle counter-label [size]
  {:font-size  (/ size 2)
   :color      colors/white
   :android    {:line-height (+ (/ size 2) 2)}
   :text-align :center})

(def image-contain
  {:align-self :stretch})

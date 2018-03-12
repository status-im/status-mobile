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

(defstyle form-title
  {:flex-shrink 1
   :ios         {:color          styles/text1-color
                 :letter-spacing -0.2
                 :font-size      16}
   :android     {:color     styles/text4-color
                 :font-size 14}})

(def form-title-count
  (merge form-title
         {:flex-shrink   0
          :opacity       0.6
          :padding-left  8
          :padding-right 5
          :color         styles/text4-color}))

(defstyle form-spacer
  {:ios     {:height 16}
   :android {:height 11}})

(defstyle list-header-footer-spacing
  {:android {:background-color colors/white
             :height           8}})

(def network-container
  {:flex-direction     :row
   :padding-horizontal 13
   :padding-vertical   11
   :align-items        :center})

(defn network-text [text-color]
  {:flex           1
   :color          (or text-color colors/black)
   :letter-spacing -0.2
   :font-size      14
   :margin-left    16})

(def network-icon
  {:width            40
   :height           40
   :border-radius    (/ 40 2)
   :background-color styles/color-green-4
   :align-items      :center
   :justify-content  :center})

(defstyle label-action-text
  {:padding-right 16
   :color         colors/blue
   :ios           {:font-size 15}
   :android       {:font-size 14}})

(defstyle logo-shaddow
  {:ios     {:shadowColor   colors/black
             :shadowOffset  {:height 5}
             :shadowRadius  10
             :shadowOpacity 0.14}
   :android {:elevation 2}})

(defn logo-container [size shadow?]
  (merge
    {:width            size
     :height           size
     :border-radius    size
     :background-color colors/blue
     :align-items      :center
     :justify-content  :center}
    (when shadow?
      logo-shaddow)))

(defn logo [icon-size]
  {:color  :white
   :width  icon-size
   :height icon-size})

(defn bottom-button [disabled?]
  {:flex-direction :row
   :align-items    :center
   :opacity        (if disabled? 0.4 1)})

(def bottom-button-label
  {:font-size      15
   :letter-spacing -0.2
   :color          colors/blue})

(defn button [style background?]
  (merge
    {:padding-vertical   12
     :padding-horizontal 42
     :border-radius      8
     :background-color   (when background?
                           (colors/alpha colors/blue 0.1))}
    style))

(def button-label
  {:font-size      15
   :letter-spacing -0.2
   :text-align     :center
   :color          colors/blue})

(defn counter-container [size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn counter-label [size]
  {:font-size      (/ size 2)
   :letter-spacing -0.2
   :text-align     :center
   :color          colors/white})

(def image-contain
  {:align-self :stretch})

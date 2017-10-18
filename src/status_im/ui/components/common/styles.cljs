(ns status-im.ui.components.common.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]))

(def gradient-top
  {:flexDirection   :row
   :height          3
   :backgroundColor styles/color-light-gray})

(def gradient-top-colors
  ["rgba(25, 53, 76, 0.01)"
   "rgba(25, 53, 76, 0.1)"])

(def gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor styles/color-light-gray})

(def gradient-bottom-colors
  ["rgba(25, 53, 76, 0.1)"
   "rgba(25, 53, 76, 0.01)"])

(def separator-wrapper
  {:background-color styles/color-white})

(defstyle separator
  {:android {:height 0}
   :ios     {:height           1
             :background-color styles/color-gray5
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
   :ios            {:background-color styles/color-white
                    :padding-top      19
                    :padding-bottom   15
                    :margin-top       16}
   :android        {:background-color styles/color-light-gray
                    :padding-top      20
                    :padding-bottom   17
                    :margin-top       8}})

(defstyle form-title-extend-container
  {:ios     {:margin-top       16
             :background-color styles/color-white}
   :android {:margin-top       8
             :background-color styles/color-light-gray}})

(def form-title-extend-button
  {:padding 16})

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
  {:android {:background-color styles/color-white
             :height           8}})

(def network-container
  {:flex-direction     :row
   :padding-horizontal 13
   :padding-vertical   11
   :align-items        :center})

(defn network-text [text-color]
  {:flex           1
   :color          (or text-color styles/color-black)
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
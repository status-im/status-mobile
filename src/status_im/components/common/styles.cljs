(ns status-im.components.common.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.utils.platform :as p]
            [status-im.components.styles :refer [color-white
                                                 color-light-gray
                                                 color-gray5
                                                 text4-color
                                                 text1-color]]))

(def gradient-top
  {:flexDirection   :row
   :height          3
   :backgroundColor color-light-gray})

(def gradient-top-colors
  ["rgba(25, 53, 76, 0.01)"
   "rgba(25, 53, 76, 0.1)"])

(def gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor color-light-gray})

(def gradient-bottom-colors
  ["rgba(25, 53, 76, 0.1)"
   "rgba(25, 53, 76, 0.01)"])

(def separator-wrapper
  {:background-color color-white})

(defstyle separator
  {:android {:height 0}
   :ios     {:height           1
             :background-color color-gray5
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
   :ios            {:background-color color-white
                    :padding-top      19
                    :padding-bottom   15
                    :margin-top       16}
   :android        {:background-color color-light-gray
                    :padding-top      20
                    :padding-bottom   17
                    :margin-top       8}})

(defstyle form-title-extend-container
  {:ios          {:margin-top 16
                  :background-color color-white}
   :android      {:margin-top 8
                  :background-color color-light-gray}})

(def form-title-extend-button
  {:padding 16})

(defstyle form-title
  {:flex-shrink 1
   :ios         {:color          text1-color
                 :letter-spacing -0.2
                 :font-size      16}
   :android     {:color     text4-color
                 :font-size 14}})

(def form-title-count
  (merge form-title
         {:flex-shrink   0
          :opacity       0.6
          :padding-left  8
          :padding-right 5
          :color         text4-color}))

(defstyle form-spacer
  {:ios     {:height 16}
   :android {:height 11}})

(defstyle list-header-footer-spacing
  {:android {:background-color color-white
             :height           8}})

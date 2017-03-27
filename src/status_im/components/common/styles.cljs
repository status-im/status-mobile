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
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor color-light-gray})

(def gradient-bottom-colors
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.05)"])

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
  {:padding-left 16
   :flex 1
   :flex-direction :row
   :ios          {:background-color color-white
                  :padding-top      19
                  :padding-bottom   15
                  :margin-top       16}
   :android      {:background-color color-light-gray
                  :padding-top      25
                  :padding-bottom   18
                  :margin-top       0}})

(defstyle form-title
  {:ios     {:color     text1-color
             :font-size 16}
   :android {:color     text4-color
             :font-size 14}})

(def form-title-count
  (merge form-title
         {:opacity      0.6
          :padding-left 8
          :color        text4-color}))


(defstyle form-spacer
  {:ios     {:height 16}
   :android {:height 11}})

(ns status-im.components.action-button.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-light-blue-transparent
                                                 color-light-blue
                                                 color-black]]))

(defstyle action-button
  {:padding-left   16
   :flex-direction :row
   :align-items    :center
   :ios            {:height 64}
   :android        {:height 56}})

(defstyle action-button-icon-container
  {:border-radius    50
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :ios              {:background-color color-light-blue-transparent}})

(def action-button-label-container
  {:padding-left 16})

(defstyle action-button-label
  {:ios     {:color          color-light-blue
             :letter-spacing -0.2
             :font-size      17
             :line-height    20}
   :android {:color          color-black
             :font-size      16
             :line-height    24}})

(defstyle actions-list
  {:background-color color-white
   :android          {:padding-top    8
                      :padding-bottom 8}})



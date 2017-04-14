(ns status-im.components.action-button.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.utils.platform :as p]
            [status-im.components.styles :refer [color-white
                                                 color-light-blue-transparent
                                                 color-light-blue
                                                 color-light-gray
                                                 color-black
                                                 color-gray4]]))

(defstyle action-button
  {:padding-left   16
   :flex-direction :row
   :align-items    :center
   :ios            {:height 63}
   :android        {:height 56}})

(defnstyle action-button-icon-container [cyrcle-color]
  {:border-radius    50
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :ios              {:background-color (or cyrcle-color color-light-blue-transparent)}})

(def action-button-label-container
  {:padding-left 16})

(defstyle action-button-label
  {:ios     {:color          color-light-blue
             :letter-spacing -0.2
             :font-size      17
             :line-height    20}
   :android {:color          color-black
             :font-size      16}})

(defstyle actions-list
  {:background-color color-white
   :android          {:padding-top    8
                      :padding-bottom 8}})


(def action-button-label-disabled
  (merge action-button-label
         {:color color-gray4}))

(defstyle action-button-icon-container-disabled
  {:border-radius    50
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :ios              {:background-color color-light-gray}})


(ns status-im.ui.screens.accounts.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as common]))

(def accounts-container
  {:flex             1
   :background-color common/color-blue2})

(def bottom-actions-container
  {:margin-bottom 16})

(def photo-image
  {:height        40
   :width         40
   :border-radius 20})

(defstyle account-title-conatiner
  {:justify-content :center
   :align-items     :center
   :ios             {:height 56}
   :android         {:height 55}})

(defstyle account-title-text
  {:color     :white
   :font-size 17
   :ios       {:letter-spacing -0.2}})

(defstyle accounts-list-container
  {:flex              1
   :margin-horizontal 16
   :ios               {:margin-top    10
                       :margin-bottom 10}
   :android           {:margin-top    16
                       :margin-bottom 16}})

(def accounts-action-button
  {:label-style  {:color :white}
   :cyrcle-color "#ffffff33"})

(def accounts-separator
  {:background-color "#7482eb"
   :opacity          1
   :margin-left      72})

(def accounts-separator-wrapper
  {:background-color common/color-blue2})

(defstyle account-view
  {:background-color :white
   :justify-content  :center
   :height           64
   :border-radius    8})

(def account-badge
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16})

(def account-badge-text-view
  {:margin-left 16
   :flex-shrink 1})

(defstyle account-badge-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16
             :color     common/color-black}})

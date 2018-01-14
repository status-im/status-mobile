(ns status-im.ui.screens.dev.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]))

;; setting
(defstyle setting-container
  {:flex-direction      :row
   :align-items         :center
   :justify-content     :space-between
   :padding-vertical    15
   :padding-horizontal  15
   :background-color    styles/color-white
   :border-bottom-width 1
   :border-color        styles/color-gray5})

(defstyle setting-text
  {:color   styles/text1-color
   :android {:font-size      16}
   :ios     {:font-size      17
             :letter-spacing -0.2}})

;; select-log-level
(defstyle select-log-level-container
  {:flex           1
   :flex-direction :column})

;; check-box
(defnstyle check-box [checked?]
  {:background-color (if checked? styles/color-light-blue styles/color-gray5)
   :align-items     :center
   :justify-content :center
   :margin-right    16
   :android         {:border-radius 2
                     :width         17
                     :height        17}
   :ios             {:border-radius 50
                     :width         24
                     :height        24}})

(def check-icon
  {:width  12
   :height 12})

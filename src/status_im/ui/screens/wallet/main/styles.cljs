(ns status-im.ui.screens.wallet.main.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

;; Toolbar

(def toolbar-title-container
  {:flex-direction :row})

(def toolbar-title-text
  {:flex      -1
   :color     colors/white
   :font-size 17})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

;; Main section

(def main-section
  {:background-color colors/blue})

(def total-balance-container
  {:align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     colors/white})

(def total-value
  {:font-size 14
   :color     styles/color-white-transparent})

(defstyle total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       styles/color-white-transparent-5
   :android     {:letter-spacing 1.5}
   :ios         {:letter-spacing 1.16}})

(defstyle buttons
  {:margin-top 34
   :android    {:margin-horizontal 21}
   :ios        {:margin-horizontal 30}})

(defstyle main-button-text
  {:padding-vertical   13
   :padding-horizontal nil
   :android            {:font-size      13
                        :letter-spacing 0.46}
   :ios                {:font-size      15
                        :letter-spacing -0.2}})

;; Actions section

(def action-section
  {:background-color colors/blue})

(def action
  {:background-color colors/white-transparent
   :border-radius    50})

(def action-label
  {:color :white})

(def action-separator
  {:height           1
   :background-color colors/white-light-transparent
   :margin-left      70})

;; Assets section

(def asset-section
  {:flex             1
   :padding-vertical 16})

(def asset-section-title
  {:font-size   14
   :margin-left 16
   :color       styles/color-gray4})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:flex      -1
   :font-size 16
   :color     styles/color-black})

(defstyle add-asset-icon
  {:flex             1
   :justify-content  :center
   :align-items      :center
   :width            40
   :height           40
   :border-radius    32
   :ios              {:background-color styles/color-blue4-transparent}})

(defstyle add-asset-text
  {:font-size 16
   :ios       {:color colors/blue}
   :android   {:color styles/color-black}})

(def asset-item-currency
  {:font-size   16
   :color       styles/color-gray4
   :margin-left 6})

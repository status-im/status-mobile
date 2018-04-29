(ns status-im.ui.screens.wallet.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as styles]))

;; wallet

(def toolbar
  {:background-color colors/blue})

(defstyle toolbar-bottom-line
  {:ios {:border-bottom-width 1
         :border-bottom-color colors/white-transparent}})

(defn button-container [enabled?]
  (merge {:flex-direction :row
          :align-items    :center}
         (when-not enabled?
           {:opacity 0.4})))

(def wallet-modal-container
  {:flex             1
   :background-color colors/blue})

;; Components

(def cartouche-container
  {:flex              1
   :margin-top        16
   :margin-horizontal 16})

(def cartouche-header
  {:color colors/white})

(defn cartouche-content-wrapper [disabled?]
  (merge
    {:flex-direction     :row
     :margin-top         8
     :border-radius      styles/border-radius
     :padding-left       14
     :padding-right      8}
    (if disabled?
      {:border-color colors/white-light-transparent
       :border-width 1}
      {:background-color colors/white-transparent})))

(def cartouche-icon-wrapper
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def cartouche-text-wrapper
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal 15
   :padding-vertical   15})

(def cartouche-primary-text
  {:color colors/white})

(def cartouche-secondary-text
  {:color colors/white-transparent})

;; Main section

(def main-section
  {:flex 1})

(def scroll-top
  (let [height (:height (react/get-dimensions "window"))]
    {:background-color colors/blue
     :zIndex           -1
     :position         :absolute
     :height           height
     :top              (- height)
     :left             0
     :right            0}))

(def section
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
   :color     colors/white-transparent})

(defstyle total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       colors/white-lighter-transparent
   :android     {:letter-spacing 1.5}
   :ios         {:letter-spacing 1.16}})

;; Actions section

(def action-section
  {:background-color colors/blue})

(def action
  {:background-color colors/white-transparent
   :width            40
   :height           40
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
   :padding-top      16
   :background-color colors/white})

(def asset-section-title
  {:font-size   14
   :margin-left 16
   :color       colors/gray})

(def asset-item-container
  {:flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:flex      -1
   :font-size 16
   :color     colors/black})

(def asset-item-currency
  {:font-size   16
   :color       colors/gray
   :margin-left 6})

(def asset-item-price
  {:font-size   16
   :color       colors/gray
   :margin-left 6})

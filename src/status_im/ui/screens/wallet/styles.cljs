(ns status-im.ui.screens.wallet.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

;; wallet

(def toolbar
  {:background-color colors/blue})

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
  {:color styles/color-white})

(def cartouche-secondary-text
  {:color styles/color-white-transparent})

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

(def asset-item-currency
  {:font-size   16
   :color       styles/color-gray4
   :margin-left 6})

(def qr-code-preview
  {:width           256
   :height          256
   :justify-content :center
   :align-items     :center})

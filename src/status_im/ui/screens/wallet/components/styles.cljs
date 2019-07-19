(ns status-im.ui.screens.wallet.components.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def cartouche-container
  {:flex              1
   :margin-top        16
   :margin-horizontal 16})

(def cartouche-header
  {:color colors/white})

(defn cartouche-content-wrapper [disabled?]
  (merge
   {:flex-direction :row
    :margin-top     8
    :border-radius  styles/border-radius
    :padding-left   14
    :padding-right  8}
   (if disabled?
     {:border-width 1
      :border-color colors/white-light-transparent}
     {:background-color colors/white-light-transparent})))

(def cartouche-icon-wrapper
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def text-content
  {:color colors/white})

(def text-secondary-content
  {:color colors/white-transparent})

(def text
  {:margin-right 10})

(def text-input
  (merge text-content
         {:flex           1
          :padding-bottom 0
          :padding-top    0
          :height         52}))

(def contact-code-text-input
  {:text-align-vertical :top
   :padding-top         16
   :padding-left        2
   :padding-right       8
   :height              72})

(def label
  {:color colors/white})

(def network
  {:color     colors/white
   :font-size 13})

(def network-container
  {:padding-horizontal 10
   :height             27
   :border-radius      100
   :border-width       1
   :border-color       colors/white-light-transparent
   :align-items        :center
   :justify-content    :center})

(def asset-container
  {:margin-top       8
   :height           52
   :background-color colors/white-light-transparent
   :justify-content  :center
   :padding-left     14
   :padding-vertical 14
   :padding-right    8
   :border-radius    8})

(def asset-container-read-only
  {:margin-top       8
   :height           52
   :border-color     colors/white-light-transparent
   :border-width     1
   :justify-content  :center
   :padding-left     14
   :padding-vertical 14
   :padding-right    8
   :border-radius    8})

(def asset-content-container
  {:flex-direction  :row
   :align-items     :center
   :margin-vertical 11})

(def asset-icon
  {:background-color colors/gray-lighter
   :border-radius    50})

(def asset-text-content
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center
   :flex-wrap       :wrap
   :margin-left     10})

(def asset-label-content
  {:flex-direction :row
   :margin-right   10})

(def asset-label
  {:margin-right 10})

(def asset-text
  {:color colors/white})

(defstyle container-disabled
  {:border-width     1
   :border-color     colors/white-light-transparent
   :background-color nil
   :border-radius    8})

(def wallet-container
  {:flex-direction :row
   :margin-top     8
   :height         52
   :border-width   1
   :border-color   colors/white-light-transparent
   :align-items    :center
   :padding        14
   :border-radius  8})

(def wallet-name
  {:color colors/white})

(defn participant [address?]
  {:color       (if address? :white colors/white-transparent)
   :flex-shrink 1})

(def recipient-container
  {:flex-direction :row})

(def recipient-icon
  {:margin-top 11})

(def recipient-name
  {:flex              1
   :flex-direction    :column
   :margin-horizontal 12
   :margin-vertical   16})

(def recipient-address
  {:margin-vertical 17
   :color           colors/white})

(def recipient-no-address
  {:color colors/white-transparent})

(def recent-recipients
  {:flex             1
   :background-color colors/white})

(def wallet-value-container
  {:flex           1
   :flex-direction :row})

(def wallet-value
  {:padding-left 6
   :color        colors/white-transparent})

(def wallet-value-amount
  {:flex -1})

(def separator
  {:height            1
   :margin-horizontal 15
   :background-color  colors/white-light-transparent})

(def button-text
  {:color colors/white})

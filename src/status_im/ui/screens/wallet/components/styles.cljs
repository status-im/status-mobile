(ns status-im.ui.screens.wallet.components.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def text-content
  {:color colors/white})

(def text-secondary-content
  {:color colors/white-lighter-transparent})

(def text
  {:margin-right 10})

(def text-list-primary-content
  (merge text {:color colors/black}))

(def text-input
  (merge text-content
    {:font-size      15
     :padding-bottom 0
     :padding-top    0
     :height         52
     :letter-spacing -0.2}))

(defstyle label
  {:color   :white
   :ios     {:font-size      14
             :line-height    16
             :letter-spacing -0.2}
   :android {:font-size   12}})

(def amount-text-input-container
  {:margin-top 8})

(def label-transparent
  (merge label
         {:color colors/white-lighter-transparent}))

(defn amount-container [active?]
  {:height           52
   :background-color (if active?
                       colors/white-light-transparent
                       styles/color-white-transparent-3)
   :border-radius      8})

(def network
  {:color          :white
   :font-size      13
   :letter-spacing -0.2})

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
   :background-color styles/color-white-transparent-3
   :justify-content  :center
   :padding-left     14
   :padding-vertical 14
   :padding-right    8
   :border-radius    8})

(def asset-container-read-only
  {:margin-top       8
   :height           52
   :border-color     styles/color-white-transparent-3
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
  {:background-color styles/color-gray9
   :border-radius    50})

(def asset-text-content
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def asset-label-content
  {:flex-direction    :row
   :margin-horizontal 10})

(def asset-label
  {:margin-right 10})

(def asset-text
  {:color styles/color-white})

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
  {:color          :white
   :font-size      15
   :letter-spacing -0.2})

(defn participant [address?]
  {:color          (if address? :white colors/white-lighter-transparent)
   :flex-shrink    1
   :font-size      15
   :letter-spacing -0.2})

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
  {:color colors/white-lighter-transparent})

(def recent-recipients
  {:flex             1
   :background-color colors/white})

(def wallet-value-container
  {:flex           1
   :flex-direction :row})

(def wallet-value
  {:padding-left   6
   :color          styles/color-white-transparent-5
   :font-size      15
   :letter-spacing -0.2})

(def wallet-value-amount
  {:flex -1})

(def separator
  {:height            1
   :margin-horizontal 15
   :background-color  styles/color-white-transparent-1})

(def button-text
  {:color          :white
   :font-size      15
   :letter-spacing -0.2})

(def tooltip-container
  {:position    :absolute
   :align-items :center
   :left        0
   :right       0
   :top         0})

(defn tooltip-animated [bottom-value opacity-value]
  {:position    :absolute
   :align-items :center
   :left        0
   :right       0
   :bottom      bottom-value
   :opacity     opacity-value})

(def tooltip-text-container
  {:padding-horizontal 16
   :padding-vertical   9
   :background-color   :white
   :border-radius      8})

(def tooltip-text
  {:color     styles/color-red-2
   :font-size 15})

(def tooltip-triangle
  {:width   16
   :height  8})

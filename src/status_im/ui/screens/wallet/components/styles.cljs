(ns status-im.ui.screens.wallet.components.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as styles]))

(def text-content
  {:color :white})

(def text-secondary-content
  {:color styles/color-white-transparent})

(def text
  {:margin-right 10})

(def text-list-primary-content
  (merge text {:color styles/color-black}))

(def text-input
  (merge text-content
    {:padding-left   14
     :padding-right  14
     :font-size      15
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
         {:color styles/color-white-transparent}))

(defnstyle amount-container [active?]
  {:height           52
   :background-color (if active?
                       styles/color-white-transparent-4
                       styles/color-white-transparent-3)
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(def network
  {:color          :white
   :font-size      13
   :letter-spacing -0.2})

(def network-container
  {:padding-horizontal 10
   :height             27
   :border-radius      100
   :border-width       1
   :border-color       styles/color-white-transparent-4
   :align-items        :center
   :justify-content    :center})

(defstyle asset-container
  {:margin-top       8
   :height           52
   :background-color styles/color-white-transparent-3
   :justify-content  :center
   :padding-left     14
   :padding-vertical 14
   :padding-right    8
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(def asset-content-container
  {:flex-direction :row
   :align-items    :center})

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
   :border-color     styles/color-white-transparent-4
   :background-color nil
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})


(defstyle recipient-container
  {:flex-direction   :row
   :flex             1
   :margin-top       8
   :height           52
   :align-items      :center
   :background-color styles/color-white-transparent-3
   :padding-vertical 14
   :padding-left     14
   :padding-right    8
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(defstyle wallet-container
  {:flex-direction :row
   :margin-top     8
   :height         52
   ;;TODO disabled
   :border-width   1
   :border-color   styles/color-white-transparent-4
   ;:background-color styles/color-white-transparent-3
   :align-items    :center
   :padding        14
   :ios            {:border-radius 8}
   :android        {:border-radius 4}})

(def wallet-name
  {:color          :white
   :font-size      15
   :letter-spacing -0.2})

(defn participant [address?]
  {:color          (if address? :white styles/color-white-transparent)
   :flex-shrink    1
   :font-size      15
   :letter-spacing -0.2})

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
   :height  8
   :viewBox "0 0 16 8"})

(def recipient-name-container
  {:padding-right 6})

(def today-variation-container
  {:border-radius      100
   :margin-left        8
   :padding-horizontal 8
   :padding-vertical   4})

(def today-variation-container-positive
  (merge today-variation-container
         {:background-color styles/color-green-1}))

(def today-variation-container-negative
  (merge today-variation-container
         {:background-color styles/color-red-3}))

(def today-variation
  {:font-size 12})

(def today-variation-positive
  (merge today-variation
         {:color styles/color-green-2}))

(def today-variation-negative
  (merge today-variation
         {:color styles/color-red-4}))
(ns status-im.ui.screens.wallet.components.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as styles]))

(def text-input
  {:color          :white
   :padding-left   14
   :padding-right  14
   :font-size      15
   :padding-bottom 0
   :padding-top    0
   :height         52
   :letter-spacing -0.2})

(defstyle label
  {:color   :white
   :ios     {:font-size      14
             :line-height    16
             :letter-spacing -0.2}
   :android {:font-size   12
             :line-height 12}})

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

(defstyle currency-container
  {:margin-top      8
   :height          52
   ;;TODO disabled
   :border-width    1
   :border-color    styles/color-white-transparent-4
   ;:background-color styles/color-white-transparent-3
   :justify-content :center
   :padding         14
   :ios             {:border-radius 8}
   :android         {:border-radius 4}})

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

(def wallet-value
  {:padding-left   6
   :color          styles/color-white-transparent-5
   :font-size      15
   :letter-spacing -0.2})

(def separator
  {:height            1
   :margin-horizontal 15
   :background-color  styles/color-white-transparent-1
   :margin-top        16})

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
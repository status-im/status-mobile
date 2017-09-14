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

(def label
  {:color          :white
   :font-size      14
   :line-height    16
   :letter-spacing -0.2})

(defnstyle amount-container [active?]
  {:margin-top       8
   :height           52
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
  {:margin-top       8
   :height           52
   :background-color styles/color-white-transparent-3
   :justify-content  :center
   :padding          14
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(defstyle wallet-container
  {:flex-direction   :row
   :margin-top       8
   :height           52
   :background-color styles/color-white-transparent-3
   :align-items      :center
   :padding          14
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(def wallet-name
  {:color          :white
   :font-size      15
   :letter-spacing -0.2})

(def wallet-value
  {:padding-left   6
   :color          styles/color-white-transparent-5
   :font-size      15
   :letter-spacing -0.2})

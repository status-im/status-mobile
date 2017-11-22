(ns status-im.ui.screens.wallet.assets.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.tabs.styles :as tabs.styles]))

;; TODO(goranjovic) - the following styles will be removed once reusable components
;; from other Wallet screens have been generalized and extracted
;; The main difference is in colors - Wallet main screen uses blue background and
;; white or very light text colors, while on assets screens its usually black text on white background

(defnstyle tab [active?]
           {:flex                1
            :height              tabs.styles/tab-height
            :justify-content     :center
            :align-items         :center
            :border-bottom-width (if active? 2 1)
            :border-bottom-color (if active?
                                   styles/color-blue4
                                   styles/color-gray10-transparent)})

(def tabs-container
  {:flex-direction :row})

(defnstyle tab-title [active?]
  {:ios        {:font-size 15}
   :android    {:font-size 14}
   :text-align :center
   :color      (if active?
                 styles/color-blue4
                 styles/color-black)})

(def total-balance-container
  {:padding-top     20
   :padding-bottom  24
   :align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     styles/color-black})

(defstyle total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       styles/color-gray4
   :android     {:letter-spacing 1.5}
   :ios         {:letter-spacing 1.16}})

(def value-variation
  {:flex-direction :row
   :align-items    :center})

(defstyle value-variation-title
  {:font-size 14
   :color     styles/color-gray4
   :android   {:letter-spacing -0.18}
   :ios       {:letter-spacing -0.2}})


(defstyle main-button-text
  {:color              styles/color-blue4
   :background-color   styles/color-blue4-transparent
   :padding 20
   :padding-horizontal nil
   :android            {:font-size      13
                        :letter-spacing 0.46}
   :ios                {:font-size      15
                        :letter-spacing -0.2}})


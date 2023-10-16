(ns quo2.components.wallet.token-input.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]
    [react-native.platform :as platform]))

(defn main-container
  [width]
  {:padding-vertical 8
   :width            width})

(def amount-container
  {:padding-horizontal 20
   :padding-bottom     4
   :height             36
   :flex-direction     :row
   :justify-content    :space-between})

(def token
  {:width  32
   :height 32})

(defn text-input
  [theme]
  (merge typography/heading-1
         {:font-weight  "600"
          :margin-left  8
          :margin-right (if platform/ios? 6 4)
          :color        (colors/theme-colors colors/neutral-100 colors/white theme)
          :padding      0
          :text-align   :center
          :height       "100%"}))

(defn divider
  [width theme]
  {:height           1
   :width            width
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :margin-vertical  8})

(def data-container
  {:padding-top        4
   :padding-horizontal 20
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center})

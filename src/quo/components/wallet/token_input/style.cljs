(ns quo.components.wallet.token-input.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]
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

(defn token-name
  [theme]
  {:color          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :margin-right   8
   :padding-bottom 2})

(def token-label-container
  {:position       :absolute
   :left           32
   :right          0
   :bottom         0
   :top            0
   :flex-direction :row
   :align-items    :flex-end})

(def text-input-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     32 ; token image size
   :right    0})

(def text-input-dimensions
  (assoc typography/heading-1
         :font-weight  "600"
         :margin-left  8
         :margin-right (if platform/ios? 6 4)
         :padding      0
         :height       "100%"))

(defn text-input
  [theme]
  (assoc text-input-dimensions :color (colors/theme-colors colors/neutral-100 colors/white theme)))

(defn placeholder-text
  [theme]
  (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))

(defn divider
  [theme]
  {:margin-vertical  8
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)})

(def data-container
  {:padding-top        4
   :padding-horizontal 20
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center})

(defn fiat-amount
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

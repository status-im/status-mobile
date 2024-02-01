(ns quo.components.wallet.amount-input.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn get-selection-color
  [customization-color theme]
  (colors/resolve-color customization-color
                        theme
                        (if platform/ios? 1 0.2)))

(def container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def input-container {:flex 1})

(defn input-text
  [theme type]
  {:padding 0
   :color   (if (= type :error)
              (colors/resolve-color :danger theme 60)
              (colors/theme-colors colors/neutral-100 colors/white theme))})

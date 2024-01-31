(ns quo.components.inputs.amount-input.style
  (:require
   [quo.foundations.colors :as colors]
   [react-native.platform :as platform]))

(defn get-selection-color
  [customization-color theme]
  (colors/alpha
   (colors/resolve-color customization-color theme
                         (if (= :dark theme) 60 50))
   (if platform/ios? 1 0.2)))

(def container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def input-container {:flex 1})

(defn input-text
  [theme type]
  {:padding             0
   :color               (if (= type :error)
                          colors/danger-60
                          (colors/theme-colors colors/neutral-100 colors/white theme))})

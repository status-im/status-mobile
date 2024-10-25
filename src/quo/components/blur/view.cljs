(ns quo.components.blur.view
  (:require [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]))

(defn- view-android
  ([props] (view-android props nil))
  ([{:keys [style]} child]
   (let [theme (quo.theme/use-theme)]
     [rn/view
      {:style (assoc style
                     :pointer-events :box-none
                     :background-color
                     (or (:background-color style)
                         (colors/theme-colors colors/white colors/neutral-80 theme)))}
      child])))

(def view (if platform/ios? blur/view view-android))

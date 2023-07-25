(ns quo2.components.gradient.gradient-cover.view
  (:require [quo2.components.gradient.gradient-cover.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.linear-gradient :as linear-gradient]))

(defn- view-internal
  [{:keys [color] :or {color :blue}}]
  (let [color-top    (colors/custom-color-by-theme color 50 50 20 20)
        color-bottom (colors/custom-color-by-theme color 50 50 0 0)]
    [linear-gradient/linear-gradient
     {:colors [color-top color-bottom]
      :start  {:x 0 :y 0}
      :end    {:x 0 :y 1}
      :style  style/root-container}]))

(def view (quo.theme/with-theme view-internal))

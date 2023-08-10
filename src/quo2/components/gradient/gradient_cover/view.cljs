(ns quo2.components.gradient.gradient-cover.view
  (:require [quo2.components.gradient.gradient-cover.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.linear-gradient :as linear-gradient]))

(defn- view-internal
  [{:keys [customization-color] :or {customization-color :blue}}]
  (let [color-top    (colors/custom-color customization-color 50 20)
        color-bottom (colors/custom-color customization-color 50 0)]
    [linear-gradient/linear-gradient
     {:accessibility-label :gradient-cover
      :colors              [color-top color-bottom]
      :start               {:x 0 :y 0}
      :end                 {:x 0 :y 1}
      :style               style/root-container}]))

(def view (quo.theme/with-theme view-internal))

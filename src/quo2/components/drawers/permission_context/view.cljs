(ns quo2.components.drawers.permission-context.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.drawers.permission-context.style :as style]))

(defn view
  [children on-press]
  [rn/touchable-highlight
   {:on-press       on-press
    :style          (style/container)
    :underlay-color (colors/theme-colors :transparent colors/neutral-95-opa-70)}
   children])

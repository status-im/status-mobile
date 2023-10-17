(ns quo2.components.drawers.permission-context.view
  (:require
    [quo2.components.drawers.permission-context.style :as style]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn view
  [children on-press]
  [rn/touchable-highlight
   {:on-press       on-press
    :style          (style/container)
    :underlay-color (colors/theme-colors :transparent colors/neutral-95-opa-70)}
   children])

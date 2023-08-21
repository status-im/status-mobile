(ns quo2.components.settings.data-item.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.components.common.not-implemented.view :as not-implemented]
            [quo2.components.settings.data-item.content.left-side :as left-side]
            [quo2.components.settings.data-item.content.right-side :as right-side]))

(def view-internal
  (fn [{:keys [blur? card? icon-right? label description status size theme on-press title subtitle icon]}]
    (let [icon-color (cond
                       (or blur? (= :dark theme)) colors/white
                       (= :light theme)           colors/neutral-100)]
      (if (= :graph label)
        [not-implemented/not-implemented blur?]
        [rn/pressable
         {:disabled (not icon-right?)
          :on-press on-press
          :style    (style/container size card? blur?)}
         [left-side/view theme title status size blur? description icon subtitle label icon-color]
         (when (and (= :default status) (not= :small size))
           [right-side/view label icon-right? icon-color])]))))

(def view (quo.theme/with-theme view-internal))

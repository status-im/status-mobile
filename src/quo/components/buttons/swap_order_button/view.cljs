(ns quo.components.buttons.swap-order-button.view
  (:require [quo.components.buttons.swap-order-button.schema :as swap-order-button.schema]
            [quo.components.buttons.swap-order-button.style :as style]
            [quo.components.icon :as icon]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [schema.core :as schema]))

(defn- view-internal
  [{:keys [disabled? on-press container-style]}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true) [])
        on-press-out           (rn/use-callback #(set-pressed false) [])]
    [rn/pressable
     {:style               (merge (style/container pressed? disabled? theme)
                                  container-style)
      :accessibility-label :swap-order-button
      :disabled            disabled?
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
     [icon/icon :i/arrow-down
      {:size  20
       :color (colors/theme-colors colors/neutral-100 colors/white theme)}]]))

(def view (schema/instrument #'view-internal swap-order-button.schema/?schema))

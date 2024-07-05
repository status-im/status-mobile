(ns quo.components.buttons.wallet-button.view
  (:require
    [quo.components.buttons.wallet-button.style :as style]
    [quo.components.icon :as quo.icons]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [on-press on-long-press disabled? icon accessibility-label container-style]}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed nil))]
    [rn/pressable
     {:accessibility-label (or accessibility-label :wallet-button)
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-long-press       on-long-press
      :disabled            disabled?
      :style               (merge (style/main {:pressed?  pressed?
                                               :theme     theme
                                               :disabled? disabled?})
                                  container-style)}
     [quo.icons/icon icon
      {:color (colors/theme-colors colors/neutral-100 colors/white theme)}]]))

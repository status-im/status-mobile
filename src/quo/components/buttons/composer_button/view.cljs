(ns quo.components.buttons.composer-button.view
  (:require
    [quo.components.buttons.composer-button.style :as style]
    [quo.components.icon :as quo.icons]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [on-press on-long-press disabled? blur? icon accessibility-label container-style]}]
  (let [[pressed? set-pressed] (rn/use-state false)
        theme                  (quo.theme/use-theme)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed nil))]
    [rn/pressable
     {:accessibility-label (or accessibility-label :composer-button)
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-long-press       on-long-press
      :disabled            disabled?
      :style               (merge (style/main {:pressed?  pressed?
                                               :blur?     blur?
                                               :theme     theme
                                               :disabled? disabled?})
                                  container-style)}
     [quo.icons/icon icon {:color (style/get-label-color {:blur? blur? :theme theme})}]]))

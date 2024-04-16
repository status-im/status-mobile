(ns quo.components.buttons.logout-button.view
  (:require
    [quo.components.buttons.logout-button.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn view
  [{:keys [on-press on-long-press disabled? container-style]}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed nil))]
    [rn/pressable
     {:accessibility-label :log-out-button
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-long-press       on-long-press
      :disabled            disabled?
      :style               (merge (style/main {:pressed?  pressed?
                                               :theme     theme
                                               :disabled? disabled?})
                                  container-style)}
     [icon/icon :i/log-out {:color (if pressed? colors/white-opa-40 colors/white-opa-70)}]
     [text/text {:weight :medium :size :paragraph-1}
      (i18n/label :t/logout)]]))

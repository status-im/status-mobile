(ns quo.components.selectors.filter.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.selectors.filter.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [blur? customization-color on-press-out pressed?]}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state pressed?)
        on-press-out           (fn []
                                 (set-pressed (not pressed?))
                                 (when on-press-out (on-press-out pressed?)))]
    [rn/pressable
     {:accessibility-label :selector-filter
      :on-press-out        on-press-out
      :style               (style/container-outer customization-color pressed? theme)}
     [rn/view {:style (style/container-inner pressed? blur? theme)}
      [icon/icon :i/unread
       {:color (style/icon-color pressed? theme)
        :size  20}]]]))

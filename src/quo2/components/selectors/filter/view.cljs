(ns quo2.components.selectors.filter.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.selectors.filter.style :as style]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn view
  [initial-props]
  (let [pressed? (reagent/atom (:pressed? initial-props))]
    (fn [{:keys [blur? override-theme on-press-out]
          :or   {override-theme (theme/get-theme)}}]
      [rn/touchable-without-feedback
       {:accessibility-label :selector-filter
        :on-press-out        (fn []
                               (swap! pressed? not)
                               (when on-press-out
                                 (on-press-out @pressed?)))}
       [rn/view {:style (style/container-outer @pressed? override-theme)}
        [rn/view {:style (style/container-inner @pressed? blur? override-theme)}
         [icon/icon :i/unread
          {:color (style/icon-color @pressed? override-theme)
           :size  20}]]]])))

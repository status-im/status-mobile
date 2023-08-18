(ns quo2.components.selectors.filter.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.selectors.filter.style :as style]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn view-internal
  [initial-props]
  (let [pressed? (reagent/atom (:pressed? initial-props))]
    (fn [{:keys [blur? customization-color theme on-press-out]}]
      [rn/touchable-without-feedback
       {:accessibility-label :selector-filter
        :on-press-out        (fn []
                               (swap! pressed? not)
                               (when on-press-out
                                 (on-press-out @pressed?)))}
       [rn/view {:style (style/container-outer customization-color @pressed? theme)}
        [rn/view {:style (style/container-inner @pressed? blur? theme)}
         [icon/icon :i/unread
          {:color (style/icon-color @pressed? theme)
           :size  20}]]]])))

(def view (quo.theme/with-theme view-internal))

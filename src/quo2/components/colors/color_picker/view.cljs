(ns quo2.components.colors.color-picker.view
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.components.colors.color.view :as color]))

(def color-list
  [:blue :yellow :purple :turquoise :magenta :sky :orange :army :flamingo :camel :copper])

(defn- on-change-handler
  [selected color-name on-change]
  (reset! selected color-name)
  (when on-change (on-change color-name)))

(defn view
  "Options
   - `default-selected?` Default selected color name.
   - `selected` Selected color name.
   - `on-change` Callback called when a color is selected `(fn [color-name])`.
   - `blur?` Boolean to enable blur background support.}"
  [{:keys [default-selected?]}]
  (let [internal-selected (reagent/atom default-selected?)]
    (fn [{:keys [blur? on-change selected feng-shui?]}]
      (when (and (not (nil? selected)) (not= @internal-selected selected))
        (reset! internal-selected selected))
      [rn/scroll-view
       {:horizontal                        true
        :shows-horizontal-scroll-indicator false}
       (doall (map-indexed (fn [index color]
                             [color/view
                              {:selected? (= color @internal-selected)
                               :on-press  #(on-change-handler internal-selected % on-change)
                               :blur?     blur?
                               :key       color
                               :color     color}])
                           ;; TODO: using :feng-shui? temporarily while b & w is being developed.
                           ;; https://github.com/status-im/status-mobile/issues/15442
                           (if feng-shui? (conj color-list :feng-shui) color-list)))])))

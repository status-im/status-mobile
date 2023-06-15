(ns quo2.components.colors.color-picker.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [reagent.core :as reagent]
            [quo2.components.colors.color-picker.style :as style]))

;; TODO: using this to keep alignment of colors correct while b & w is being developed.
;; https://github.com/status-im/status-mobile/issues/15442
(def empty-color :no-color)

(def color-list
  [:blue :yellow :turquoise :copper :sky :camel :orange :army :pink :purple :magenta empty-color])

(defn picker-colors
  [blur?]
  (map (fn [color]
         {:name  color
          :color (colors/custom-color-by-theme color (if blur? 60 50) 60)})
       color-list))

(defn- on-change-handler
  [selected color-name on-change]
  (reset! selected color-name)
  (when on-change (on-change color-name)))

(defn empty-color-item
  []
  [rn/view {:style style/color-button-common}])

(defn- color-item
  [{:keys [name
           color
           secondary-color
           selected?
           on-press
           blur?]}]
  (let [border? (and (not blur?) (and secondary-color (not selected?)))]
    (if-not name
      [empty-color-item]
      [rn/touchable-opacity
       {:style               (style/color-button color selected?)
        :accessibility-label :color-picker-item
        :on-press            #(on-press name)}
       [rn/view
        {:accessibile         true
         :accessibility-label name
         :style               (style/color-circle color border?)}
        (when (and secondary-color (not selected?))
          [rn/view
           {:style (style/secondary-overlay secondary-color border?)}])
        (when selected?
          [icon/icon :i/check
           {:size  20
            :color (or secondary-color
                       colors/white)}])]])))

(defn view
  "Options
   - `default-selected?` Default selected color name.
   - `selected` Selected color name.
   - `on-change` Callback called when a color is selected `(fn [color-name])`.
   - `blur?` Boolean to enable blur background support.}"
  [{:keys [default-selected?]}]
  (let [internal-selected (reagent/atom default-selected?)]
    (fn [{:keys [blur? on-change selected]}]
      (when (and (not (nil? selected)) (not= @internal-selected selected))
        (reset! internal-selected selected))
      [rn/view {:style style/color-picker-container}
       (doall (map-indexed (fn [index color]
                             [:<> {:key (color :name)}
                              [color-item
                               (merge color
                                      {:selected? (= (color :name) @internal-selected)
                                       :on-press  #(on-change-handler internal-selected % on-change)
                                       :blur?     blur?})]
                              (when (= index 5) [rn/view {:style style/flex-break}])])
                           (picker-colors blur?)))])))


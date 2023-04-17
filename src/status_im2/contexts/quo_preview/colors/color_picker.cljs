(ns status-im2.contexts.quo-preview.colors.color-picker
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Color:"
    :key     :color
    :type    :select
    :options (map (fn [color] (let [key (get color :name)] {:key key :value key})) (quo/picker-colors))}
   {:label "Blur:"
    :key   :blur
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:color "orange" :blur false})
        blur  (reagent/cursor state [:blur])
        color (reagent/cursor state [:color])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [(if @blur blur/view :<>)
         [rn/view {:padding-vertical 60 :align-items :center}
          [quo/color-picker
           {:blur?     @blur
            :selected  @color
            :on-change #(reset! color %)}]]]]])))

(defn preview-color-picker
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])


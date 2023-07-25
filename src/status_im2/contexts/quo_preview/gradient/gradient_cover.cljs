(ns status-im2.contexts.quo-preview.gradient.gradient-cover
  (:require [quo2.components.gradient.gradient-cover.view :as gradient-cover]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Color:"
    :key     :color
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}
   {:label "Blur (dark only)?"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:color :blue :blur? false})
        blur? (reagent/cursor state [:blur?])
        color (reagent/cursor state [:color])]
    [:f>
     (fn []
       (rn/use-effect (fn []
                        (when @blur?
                          (quo.theme/set-theme :dark)))
                      [@blur?])
       [:<>
        [preview/customizer state descriptor]
        [rn/view
         {:style {:height        332
                  :margin-top    24
                  :overflow      :hidden
                  :border-radius 12}}
         (when @blur?
           [rn/image
            {:style {:height 332}
             :source
             {:uri
              "https://4kwallpapers.com/images/wallpapers/giau-pass-mountain-pass-italy-dolomites-landscape-mountain-750x1334-4282.jpg"}}])
         [(if @blur? blur/view rn/view)
          {:style     {:height           332
                       :position         :absolute
                       :top              0
                       :left             0
                       :right            0
                       :bottom           0
                       :padding-vertical 40}
           :blur-type :dark}
          [gradient-cover/view @state]]]
        [rn/view
         {:style {:padding-vertical   20
                  :padding-horizontal 16}}
         [quo/color-picker
          {:blur?     @blur?
           :selected  @color
           :on-change #(reset! color %)}]]])]))

(defn preview-gradient-cover
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

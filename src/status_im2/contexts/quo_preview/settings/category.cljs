(ns status-im2.contexts.quo-preview.settings.category
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def item
  {:title     "Item 1"
   :left-icon :i/browser
   :chevron?  true})

(def descriptor
  [{:label "Category label:"
    :key   :label
    :type  :text}
   {:label "Category size:"
    :key   :size
    :type  :text}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}])

(def image-uri
  "https://4kwallpapers.com/images/wallpapers/giau-pass-mountain-pass-italy-dolomites-landscape-mountain-750x1334-4282.jpg")

(def label "Label")

(defn preview
  []
  (let [state                  (reagent/atom {:label "Label"
                                              :size  "5"
                                              :blur? false})
        {:keys [width height]} (rn/get-window)]
    [:f>
     (fn []
       (let [data (repeat (js/parseInt (:size @state)) item)]
         (rn/use-effect (fn []
                          (if (:blur? @state)
                            (theme/set-theme :dark)
                            (theme/set-theme :light)))
                        [(:blur? @state)])
         [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
          [rn/view
           {:style {:flex             1
                    :padding-bottom   150
                    :margin-bottom    50
                    :background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)}}
           [rn/view
            {:style {:min-height 180
                     :z-index    1}} [preview/customizer state descriptor]]
           (when (:blur? @state)
             [fast-image/fast-image
              {:source {:uri image-uri}
               :style  {:width    width
                        :height   height
                        :position :absolute}}])
           [rn/view
            {:style {:background-color (if (:blur? @state)
                                         colors/neutral-80-opa-80
                                         (colors/theme-colors colors/neutral-5 colors/neutral-95))}}
            [quo/category {:label (:label @state) :data data :blur? (:blur? @state)}]]]]))]))

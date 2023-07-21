(ns status-im2.contexts.quo-preview.settings.reorder-category
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(defn create-item-array [n right-icon?]
  (vec (for [i (range n)]
         {:title      (str "Item " i)
          :subtitle   "subtitle"
          :left-icon  :i/browser
          :chevron?   true
          :right-icon (when right-icon? :i/globe)
          :image-size 32
          :image      (resources/get-mock-image :diamond)})))

(def descriptor
  [{:label "Category label:"
    :key   :label
    :type  :text}
   {:label "Category size:"
    :key   :size
    :type  :text}
   {:label "Right icon:"
    :key   :right-icon?
    :type  :boolean}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:label       "Label"
                             :size        "5"
                             :blur?       false
                             :right-icon? false})
        {:keys [width height]} (rn/get-window)]
    [:f>
     (fn []
       (let [data (reagent/atom (create-item-array (max (js/parseInt (:size @state)) 1) (:right-icon? @state)))]
         (rn/use-effect (fn []
                          (if (:blur? @state)
                            (theme/set-theme :dark)
                            (theme/set-theme :light))
                          (reset! data (create-item-array (max (js/parseInt (:size @state)) 1) (:right-icon? @state))))
                        [(:blur? @state) (:right-icon? @state)])
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
              {:source (resources/get-mock-image :dark-blur-bg)
               :style  {:width    width
                        :height   height
                        :position :absolute}}])
           [rn/view
            {:style {:background-color (if (:blur? @state)
                                         colors/neutral-80-opa-80
                                         (colors/theme-colors colors/neutral-5 colors/neutral-95))}}
            [quo/reorder-category {:label (:label @state) :data @data :blur? (:blur? @state)}]]]]))]))

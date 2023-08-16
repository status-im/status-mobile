(ns status-im2.contexts.quo-preview.buttons.composer-button
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo2.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Blur?:"
    :key   :blur?
    :type  :boolean}
   {:label "Disabled?:"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:icon          :i/placeholder
                             :on-press      #(js/alert "pressed")
                             :on-long-press #(js/alert "long pressed")})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:flex 1 :padding-bottom 20}
        [rn/view {:height 200}
         [preview/customizer state descriptor]]
        [rn/view
         {:flex            1
          :align-items     :center
          :justify-content :center}
         (when (:blur? @state)
           [rn/image
            {:source (if (= :light (quo2.theme/get-theme))
                       (resources/get-mock-image :community-cover)
                       (resources/get-mock-image :dark-blur-bg))
             :style  {:position :absolute
                      :top      200
                      :left     0
                      :right    0
                      :bottom   0}}])
         [quo/composer-button @state]]]])))

(defn preview-composer-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [cool-preview]])

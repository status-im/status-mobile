(ns status-im2.contexts.quo-preview.selectors.selectors
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.selectors.selectors.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Disabled"
    :key   :disabled?
    :type  :boolean}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}
   {:label   "Customization color"
    :key     :customization-color
    :type    :select
    :options (map (fn [[color _]]
                    {:key color :value (name color)})
                  colors/customization)}])

(defn selector-preview
  [text component {:keys [disabled? blur? customization-color]}]
  [rn/view
   {:style {:margin      6
            :align-items :center}}
   [text/text {:size :paragraph-1} text]
   [component
    {:container-style     {:margin 4}
     :disabled?           disabled?
     :blur?               blur?
     :customization-color customization-color}]])

(defn preview-selectors
  []
  (let [state (reagent/atom {:disabled?           false
                             :blur?               false
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:margin-vertical 24}}
        [preview/blur-view
         {:style                 {:width       "100%"
                                  :align-items :center
                                  :top         (if (:blur? @state) 32 16)
                                  :position    (if (:blur? @state)
                                                 :absolute
                                                 :relative)}
          :height                300
          :show-blur-background? (:blur? @state)}

         [rn/view
          {:style {:flex        1
                   :align-items :center}}
          [selector-preview "Toggle" quo2/toggle @state]
          [selector-preview "Radio" quo2/radio @state]
          [selector-preview "Checkbox" quo2/checkbox @state]
          [selector-preview "Checkbox Prefill" quo2/checkbox-prefill @state]]]]])))

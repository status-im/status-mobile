(ns status-im2.contexts.quo-preview.selectors.disclaimer
  (:require [quo2.components.buttons.button :as button]
            [quo2.components.selectors.disclaimer.view :as disclaimer]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Checked:"
    :key   :checked?
    :type  :boolean}
   {:label "Blur (only for dark theme):"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:checked? false
                             :blur?    true})]
    (fn []
      [rn/view {:style {:flex 1}}
       [rn/view {:style {:flex 1}}
        [preview/customizer state descriptor]]
       [rn/view {:style {:padding-horizontal 15}}
        (when (and (:blur? @state) (theme/dark?))
          [rn/view
           {:style {:position :absolute
                    :top      0
                    :bottom   0
                    :left     0
                    :right    0}}
           [preview/blur-view
            {:style                 {:flex        1
                                     :align-items :center}
             :show-blur-background? true}]])
        [rn/view
         {:style {:margin-vertical 50
                  :width           "100%"}}
         [disclaimer/view
          {:blur?     (:blur? @state)
           :checked?  (:checked? @state)
           :on-change #(swap! state update :checked? not)}
          "I agree with the community rules"]]
        [button/button {:disabled (not (:checked? @state))}
         "submit"]]])))

(defn preview-disclaimer
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])

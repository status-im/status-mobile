(ns quo.previews.controls
  (:require
   [quo.core :as quo]
   [quo.design-system.colors :as colors]
   [quo.react-native :as rn]
   [reagent.core :as reagent]))

(defn preview
  []
  (let [switch-state   (reagent/atom true)
        radio-state    (reagent/atom true)
        checkbox-state (reagent/atom true)]
    (fn []
      [rn/view
       {:background-color (:ui-background @colors/theme)
        :flex             1}
       [rn/view
        {:padding         20
         :flex-direction  :row
         :align-items     :center
         :justify-content :space-between}
        [rn/touchable-opacity
         {:style    {:margin-vertical 10
                     :padding         10}
          :on-press #(swap! switch-state not)}
         [quo/text (str "Switch state: " @switch-state)]]
        [quo/switch
         {:value     @switch-state
          :on-change #(reset! switch-state %)}]]

       [rn/view
        {:padding         20
         :flex-direction  :row
         :align-items     :center
         :justify-content :space-between}
        [rn/touchable-opacity
         {:style    {:margin-vertical 10
                     :padding         10}
          :on-press #(swap! radio-state not)}
         [quo/text (str "Radio state: " @radio-state)]]
        [quo/radio
         {:value     @radio-state
          :on-change #(reset! radio-state %)}]]
       [rn/view
        {:padding         20
         :flex-direction  :row
         :align-items     :center
         :justify-content :space-between}
        [rn/touchable-opacity
         {:style    {:margin-vertical 10
                     :padding         10}
          :on-press #(swap! checkbox-state not)}
         [quo/text (str "Checkbox state: " @checkbox-state)]]
        [quo/checkbox
         {:value     @checkbox-state
          :on-change #(reset! checkbox-state %)}]]])))

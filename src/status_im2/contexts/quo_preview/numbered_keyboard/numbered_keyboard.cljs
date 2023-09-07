(ns status-im2.contexts.quo-preview.numbered-keyboard.numbered-keyboard
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.blur :as blur]))

(def descriptor
  [{:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label "Disable:"
    :key   :disabled?
    :type  :boolean}
   {:label "Delete Key:"
    :key   :delete-key?
    :type  :boolean}
   {:label   "Left Action:"
    :type    :select
    :key     :left-action
    :options [{:key   :dot
               :value "Dot"}
              {:key   :face-id
               :value "Face ID"}
              {:key   :none
               :value "None"}]}])

(defn preview-numbered-keyboard
  []
  (let [state (reagent/atom {:disabled?   false
                             :on-press    (fn [item] (js/alert (str item " pressed")))
                             :blur?       false
                             :delete-key? true
                             :left-action :dot})
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       (when @blur?
         [blur/view
          {:style         {:position         :absolute
                           :left             0
                           :right            0
                           :bottom           0
                           :height           220
                           :background-color colors/neutral-80-opa-70}
           :overlay-color :transparent}])
       [quo/numbered-keyboard @state]])))

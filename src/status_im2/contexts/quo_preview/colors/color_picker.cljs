(ns status-im2.contexts.quo-preview.colors.color-picker
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:selected            :blue
                             :customization-color :blue
                             :blur?               false
                             :feng-shui?          true})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/color-picker
        (merge @state
               {:on-change #(swap! state assoc :selected %)
                :color     (:customization-color @state)})]])))

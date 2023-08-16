(ns status-im2.contexts.quo-preview.colors.color-picker
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :selected
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:selected :orange
                             :blur?    false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/color-picker (assoc @state :on-change #(swap! state assoc :selected %))]])))

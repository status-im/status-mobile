(ns status-im2.contexts.quo-preview.buttons.predictive-keyboard
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :error}
              {:key :empty}
              {:key :info}
              {:key :words}]}
   {:key  :text
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:type :info
                             :text "Enter 12, 18 or 24 words separated by a space"})
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state                state
        :descriptor           descriptor
        :blur?                @blur?
        :blur-container-style {:background-color colors/neutral-80-opa-70}
        :blur-view-props      {:overlay-color :transparent}}
       [quo/predictive-keyboard
        (merge @state
               {:words    ["label" "label" "labor" "ladder" "lady" "lake"]
                :on-press #(js/alert (str "Pressed: " %))})]])))

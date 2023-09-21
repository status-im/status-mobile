(ns status-im2.contexts.quo-preview.list-items.token-value
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   :eth
               :value "ETH"}
              {:key   :snt
               :value "SNT"}]}
   {:label   "State:"
    :key     :state
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :pressed
               :value "Pressed"}
              {:key   :active
               :value "Active"}]}
   {:label   "Status:"
    :key     :status
    :type    :select
    :options [{:key   :empty
               :value "Empty"}
              {:key   :positive
               :value "Positive"}
              {:key   :negative
               :value "Negative"}]}
   (preview/customization-color-option)
   {:label "Metrics?:"
    :key   :metrics?
    :type  :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:token               :snt
                             :state               :default
                             :status              :empty
                             :customization-color :blue
                             :metrics?            true
                             :values              {:crypto-value      "0.00"
                                                   :fiat-value        "€0.00"
                                                   :percentage-change "0.00"
                                                   :fiat-change       "€0.00"}})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        [rn/view
         {:style {:align-items :center
                  :margin-top  50}}
         [quo/token-value @state]]]])))


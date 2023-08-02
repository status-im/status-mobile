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
   {:label   "Color:"
    :key     :color
    :type    :select
    :options [{:key   :blue
               :value "Blue"}
              {:key   :orange
               :value "Orange"}
              {:key   :green
               :value "Green"}]}
   {:label "Metrics?:"
    :key :metrics?
    :type :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:amount "5.123456"
                             :token  :snt
                             :state :default
                             :status :empty
                             :color :blue
                             :metrics? true
                             :values {:crypto-value "0.00"
                                      :fiat-value "€0.00"
                                      :percentage-change "0.00"
                                      :fiat-change "€0.00"}})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex               1}}
        [rn/view {:style {:min-height 300}} [preview/customizer state descriptor]]
        [rn/view {:style {:align-items :center
                          :margin-top 50}} [quo/token-value @state]]]])))

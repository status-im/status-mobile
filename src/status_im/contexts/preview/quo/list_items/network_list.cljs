(ns status-im.contexts.preview.quo.list-items.network-list
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :transparent}
              {:key :active}
              {:key :pressed}]}
   {:key     :network-name
    :type    :select
    :options [{:key :ethereum}
              {:key :arbitrum}
              {:key :optimism}]}
   {:key  :label
    :type :text}
   {:key  :token-value
    :type :text}
   {:key  :fiat-value
    :type :text}
   {:key  :show-alert-on-press?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:network-name        :ethereum
                             :label               "Mainnet"
                             :token-value         "0.00 ETH"
                             :fiat-value          "€0.00"
                             :state               :transparent
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/network-list
        (merge @state
               (when (:show-alert-on-press? @state)
                 {:on-press #(js/alert "Pressed!")}))]])))

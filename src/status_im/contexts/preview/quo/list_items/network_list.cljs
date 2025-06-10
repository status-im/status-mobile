(ns status-im.contexts.preview.quo.list-items.network-list
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :default}
              {:key :active}
              {:key :pressed}]}
   {:key     :network-image
    :type    :select
    :options [{:key   (quo.resources/get-network :ethereum)
               :value :ethereum}
              {:key   (quo.resources/get-network :arbitrum)
               :value :arbitrum}
              {:key   (quo.resources/get-network :optimism)
               :value :optimism}]}
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
  (let [state (reagent/atom {:network-image       (quo.resources/get-network :ethereum)
                             :label               "Ethereum"
                             :token-value         "0.00 ETH"
                             :fiat-value          "€0.00"
                             :state               :default
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/network-list
        (merge @state
               (when (:show-alert-on-press? @state)
                 {:on-press #(js/alert "Pressed!")}))]])))

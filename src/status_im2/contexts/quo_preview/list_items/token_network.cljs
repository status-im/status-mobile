(ns status-im2.contexts.quo-preview.list-items.token-network
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}
   {:source (quo.resources/get-network :zksync)}
   {:source (quo.resources/get-network :polygon)}])

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :default}
              {:key :active}
              {:key :selected}]}
   {:key     :token
    :type    :select
    :options [{:value :eth
               :key   (quo.resources/get-token :eth)}
              {:value :snt
               :key   (quo.resources/get-token :snt)}
              {:value :dai
               :key   (quo.resources/get-token :dai)}]}
   {:key  :label
    :type :text}
   {:key  :token-value
    :type :text}
   {:key  :fiat-value
    :type :text}
   (preview/customization-color-option)
   {:key  :show-alert-on-press?
    :type :boolean}])

(defn preview-token-network
  []
  (let [state (reagent/atom {:token               (quo.resources/get-token :snt)
                             :label               "Status"
                             :token-value         "0.00 SNT"
                             :fiat-value          "€0.00"
                             :networks            networks-list
                             :state               :default
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/token-network
        (merge @state
               (when (:show-alert-on-press? @state)
                 {:on-press #(js/alert "Pressed!")}))]])))

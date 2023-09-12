(ns status-im2.contexts.quo-preview.wallet.network-link
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def networks
  [{:key   :ethereum
    :value "Ethereum"}
   {:key   :optimism
    :value "Optimism"}
   {:key   :arbitrum
    :vault "Arbitrum"}])

(def descriptor
  [{:label   "Shape:"
    :key     :shape
    :type    :select
    :options [{:key   :linear
               :value "Linear"}
              {:key   :1x
               :value "1x"}
              {:key   :2x
               :value "2x"}]}
   {:label   "Source:"
    :key     :source
    :type    :select
    :options networks}
   {:label   "Destination:"
    :key     :destination
    :type    :select
    :options networks}])

(defn preview
  []
  (let [state (reagent/atom {:shape       :linear
                             :source      :ethereum
                             :destination :optimism})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:flex        1
                 :padding-top 40
                 :align-items :center}}
        [quo/network-link @state]]])))

(ns status-im.contexts.preview.quo.wallet.network-link
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def networks
  [{:key   :ethereum
    :value "Ethereum"}
   {:key   :optimism
    :value "Optimism"}
   {:key   :arbitrum
    :vault "Arbitrum"}])

(def descriptor
  [{:key     :shape
    :type    :select
    :options [{:key :linear}
              {:key :1x}
              {:key :2x}]}
   {:key     :source
    :type    :select
    :options networks}
   {:key     :destination
    :type    :select
    :options networks}
   {:key  :width
    :type :number}])

(defn view
  []
  (let [state (reagent/atom {:shape       :linear
                             :source      :ethereum
                             :destination :optimism
                             :width       63})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [rn/view {:style {:width (max (:width @state) 63)}}
        [quo/network-link @state]]])))

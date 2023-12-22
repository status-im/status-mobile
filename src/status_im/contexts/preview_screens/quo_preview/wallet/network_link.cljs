(ns status-im.contexts.preview-screens.quo-preview.wallet.network-link
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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
    :options networks}])

(defn view
  []
  (let [state (reagent/atom {:shape       :linear
                             :source      :ethereum
                             :destination :optimism})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [quo/network-link @state]])))

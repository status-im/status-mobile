(ns status-im.contexts.preview.quo.wallet.network-bridge
  (:require
    [quo.components.wallet.network-bridge.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor
  (conj (preview-gen/schema->descriptor ?schema {:exclude-keys #{:network}})
        {:key     :network
         :type    :select
         :options [{:key :ethereum}
                   {:key :optimism}
                   {:key :arbitrum}]}))

(defn view
  []
  (let [state (reagent/atom {:network :ethereum
                             :status  :default
                             :amount  "50 SNT"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 20
                                    :margin-top         50
                                    :align-items        :center}}
       [quo/network-bridge @state]])))

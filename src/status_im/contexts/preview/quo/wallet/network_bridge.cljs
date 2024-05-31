(ns status-im.contexts.preview.quo.wallet.network-bridge
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))


(def descriptor
  [{:key     :network
    :type    :select
    :options [{:key :ethereum}
              {:key :optimism}
              {:key :arbitrum}]}
   {:key     :status
    :type    :select
    :options [{:key :default :value :default}
              {:key :locked :value :locked}
              {:key :loading :value :loading}
              {:key :disabled :value :disabled}
              {:key :edit :value :edit}]}])

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

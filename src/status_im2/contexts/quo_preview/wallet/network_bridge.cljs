(ns status-im2.contexts.quo-preview.wallet.network-bridge
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))


(def descriptor
  [{:label   "Network:"
    :key     :network
    :type    :select
    :options [{:key   :ethereum
               :value "Ethereum"}
              {:key   :optimism
               :value "Optimism"}
              {:key   :arbitrum
               :value "Arbitrum"}]}
   {:label   "State:"
    :key     :state
    :type    :select
    :options [{:key :default :value :default}
              {:key :locked :value :locked}
              {:key :loading :value :loading}
              {:key :disabled :value :disabled}]}])

(defn preview
  []
  (let [state (reagent/atom {:network :ethereum
                             :state   :default
                             :amount  "50 SNT"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [rn/view {:style {:min-height 150}} [preview/customizer state descriptor]]
        [rn/view
         {:style {:flex        1
                  :margin-top  50
                  :align-items :center}} [quo/network-bridge @state]]]])))

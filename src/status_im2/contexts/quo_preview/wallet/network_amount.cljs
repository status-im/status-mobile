(ns status-im2.contexts.quo-preview.wallet.network-amount
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))


(def descriptor
  [{:label "Amount:"
    :key   :amount
    :type  :text}
   {:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   :eth
               :value "ETH"}
              {:key   :snt
               :value "SNT"}]}])

(defn preview
  []
  (let [state (reagent/atom {:amount "5.123456"
                             :token  :eth})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [quo/network-amount @state]]])))

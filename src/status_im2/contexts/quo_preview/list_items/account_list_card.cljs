(ns status-im2.contexts.quo-preview.list-items.account-list-card
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
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [rn/view {:style {:min-height 150}} [preview/customizer state descriptor]]
        [quo/account-list-card @state]]])))


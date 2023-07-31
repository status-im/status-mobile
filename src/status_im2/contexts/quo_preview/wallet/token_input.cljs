(ns status-im2.contexts.quo-preview.wallet.token-input
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))


(def descriptor
  [{:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   :eth
               :value "ETH"}
              {:key   :snt
               :value "SNT"}]}
   {:label   "Currency:"
    :key     :currency
    :type    :select
    :options [{:key   :usd
               :value "USD"}
              {:key   :eur
               :value "EUR"}]}])


(defn preview
  []
  (let [state (reagent/atom {:token      :snt
                             :currency   :eur
                             :conversion 0.02})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [rn/view {:style {:min-height 150}} [preview/customizer state descriptor]]
        [rn/view
         {:style {:flex        1
                  :margin-top  50
                  :align-items :center}} [quo/token-input @state]]]])))

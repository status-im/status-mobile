(ns status-im2.contexts.quo-preview.wallet.token-overview
  (:require [quo2.components.wallet.token-overview :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.utils.currency :as currencies]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   "SNT"
               :value "SNT"}
              {:key   "ETH"
               :value "ETH"}]}

   {:label "Account Balance:"
    :key   :account-balance
    :type  :text}
   {:label "Price:"
    :key   :price
    :type  :text}
   {:label "Percentage-Increase:"
    :key   :percentage-change
    :type  :text}
   {:label   "Currency:"
    :key     :currency
    :type    :select
    :options [{:key   :usd
               :value "$"}
              {:key   :eur
               :value "€"}]}])

(def eth-token (js/require "../resources/images/tokens/mainnet/ETH.png"))
(def snt-token (js/require "../resources/images/tokens/mainnet/SNT.png"))

(defn cool-preview
  []
  (let [state (reagent/atom {:token             "ETH"
                             :account-balance   "3.00"
                             :price             "1.00"
                             :percentage-change "-3.0"
                             :currency          (get-in currencies/currencies [:usd :symbol])})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:border       :black
          :border-width 1
          :align-items  :center}
         [quo2/token-balance
          (assoc @state :token-img-src (if (= (:token @state) "ETH") eth-token snt-token))]
         [rn/view
          {:padding-vertical 25
           :align-items      :center}]
         [quo2/token-price
          (assoc @state
                 :token-img-src (if (= (:token @state) "ETH") eth-token snt-token)
                 :label         (i18n/label :token-price))]]]])))

(defn preview-token-overview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])

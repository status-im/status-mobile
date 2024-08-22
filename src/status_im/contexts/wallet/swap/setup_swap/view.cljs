(ns status-im.contexts.wallet.swap.setup-swap.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.swap.setup-swap.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- data-item
  [{:keys [title subtitle size subtitle-icon loading?]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :subtitle-type   (if subtitle-icon :editable :default)
    :status          (if loading? :loading :default)
    :title           title
    :subtitle        subtitle
    :size            size
    :icon            subtitle-icon}])

(defn- transaction-details
  [{:keys [max-slippage native-currency-symbol loading-fees?]}]
  (let [max-fees (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol])]
    [rn/view {:style style/details-container}
     [data-item
      {:title    (i18n/label :t/max-fees)
       :subtitle max-fees
       :loading? loading-fees?
       :size     :small}]
     [data-item
      {:title         (i18n/label :t/max-slippage)
       :subtitle      max-slippage
       :subtitle-icon :i/edit
       :loading?      loading-fees?}]]))

(defn view
  []
  (let [[pay-value set-pay-value]        (rn/use-state "")
        {:keys [color]}                  (rf/sub [:wallet/current-viewing-account])
        {:keys [max-slippage swap-proposal loading-fees?
                receive-amount network]} (rf/sub [:wallet/swap])
        currency                         (rf/sub [:profile/currency])
        currency-symbol                  (rf/sub [:profile/currency-symbol])
        asset-to-pay                     (rf/sub [:wallet/swap-asset-to-pay])
        asset-to-receive                 (rf/sub [:wallet/swap-asset-to-receive])

        pay-token-fiat-value             (utils/calculate-token-fiat-value
                                          {:currency currency
                                           :balance  (or pay-value 0)
                                           :token    asset-to-pay})
        receive-token-fiat-value         (utils/calculate-token-fiat-value
                                          {:currency currency
                                           :balance  (or receive-amount 0)
                                           :token    asset-to-receive})
        native-currency-symbol           (get-in swap-proposal
                                                 [:from :native-currency-symbol])
        pay-token-symbol                 (:symbol asset-to-pay)
        receive-token-symbol             (:symbol asset-to-receive)
        on-press                         (fn [v] (set-pay-value (str pay-value v)))
        delete                           (fn []
                                           (set-pay-value #(subs % 0 (dec (count %)))))]
    [rn/view {:style style/container}
     [account-switcher/view
      {:on-press      events-helper/navigate-back
       :icon-name     :i/arrow-left
       :margin-top    (safe-area/get-top)
       :switcher-type :select-account}]
     [rn/view {:style style/inputs-container}
      [quo/swap-input
       {:type                 :pay
        :error?               false
        :token                pay-token-symbol
        :customization-color  :blue
        :show-approval-label? false
        :status               :default
        :currency-symbol      currency-symbol
        :on-swap-press        #(js/alert "Swap Pressed")
        :on-token-press       #(js/alert "Token Pressed")
        :on-max-press         #(js/alert "Max Pressed")
        :value                pay-value
        :fiat-value           pay-token-fiat-value
        :network-tag-props    {:title    (i18n/label :t/max-token
                                                     {:number       200
                                                      :token-symbol pay-token-symbol})
                               :networks [{:source (:source network)}]}
        :approval-label-props {:status              :approve
                               :token-value         pay-value
                               :button-props        {:on-press
                                                     #(js/alert "Approve Pressed")}
                               :customization-color color
                               :token-symbol        pay-token-symbol}}]
      [quo/swap-order-button
       {:container-style style/swap-order-button
        :on-press        #(js/alert "Pressed")}]
      [quo/swap-input
       {:type                 :receive
        :error?               false
        :token                receive-token-symbol
        :customization-color  color
        :show-approval-label? false
        :enable-swap?         true
        :status               :default
        :currency-symbol      currency-symbol
        :on-swap-press        #(js/alert "Swap Pressed")
        :on-token-press       #(js/alert "Token Pressed")
        :on-max-press         #(js/alert "Max Pressed")
        :value                receive-amount
        :fiat-value           receive-token-fiat-value
        :container-style      style/receive-token-swap-input-container}]]
     [rn/view {:style style/footer-container}
      (when swap-proposal
        [transaction-details
         {:native-currency-symbol native-currency-symbol
          :max-slippage           max-slippage
          :loading-fees?          loading-fees?}])
      [quo/bottom-actions
       {:actions          :one-action
        :button-one-label (i18n/label :t/review-swap)
        :button-one-props {:disabled?           (or (not swap-proposal)
                                                    loading-fees?)
                           :customization-color color
                           :on-press            #(js/alert "Review swap pressed")}}]]
     [quo/numbered-keyboard
      {:container-style style/keyboard-container
       :left-action     :dot
       :delete-key?     true
       :on-press        on-press
       :on-delete       delete}]]))

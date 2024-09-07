(ns status-im.contexts.wallet.swap.setup-swap.view
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im.common.controlled-input.utils :as controlled-input]
            [status-im.common.events-helper :as events-helper]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.swap.setup-swap.style :as style]
            [utils.hex :as hex]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.number :as number]
            [utils.re-frame :as rf]
            [utils.string :as utils.string]))

(def ^:private default-text-for-unfocused-input "0.00")

(defn- on-close
  []
  (rf/dispatch [:wallet/clean-swap-proposal])
  (events-helper/navigate-back))

(defn- fetch-swap-proposal
  [{:keys [amount valid-input?]}]
  (if valid-input?
    (rf/dispatch [:wallet/start-get-swap-proposal {:amount-in amount}])
    (rf/dispatch [:wallet/clean-swap-proposal])))

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
  []
  (let [max-fees               (rf/sub [:wallet/wallet-swap-proposal-fee-fiat-formatted
                                        constants/token-for-fees-symbol])
        max-slippage           (rf/sub [:wallet/swap-max-slippage])
        loading-fees?          (rf/sub [:wallet/swap-loading-fees?])
        loading-swap-proposal? (rf/sub [:wallet/swap-loading-swap-proposal?])
        loading?               (or loading-fees? loading-swap-proposal?)]
    [rn/view {:style style/details-container}
     [data-item
      {:title    (i18n/label :t/max-fees)
       :subtitle max-fees
       :loading? loading?
       :size     :small}]
     [data-item
      {:title         (i18n/label :t/max-slippage)
       :subtitle      max-slippage
       :subtitle-icon :i/edit
       :size          :small
       :loading?      loading?}]]))

(defn- pay-token-input
  [{:keys [input-state on-max-press on-input-focus on-token-press on-approve-press input-focused?]}]
  (let [account-color                    (rf/sub [:wallet/current-viewing-account-color])
        network                          (rf/sub [:wallet/swap-network])
        asset-to-pay                     (rf/sub [:wallet/swap-asset-to-pay])
        currency                         (rf/sub [:profile/currency])
        loading-swap-proposal?           (rf/sub [:wallet/swap-loading-swap-proposal?])
        swap-proposal                    (rf/sub [:wallet/swap-proposal])
        approval-required                (rf/sub [:wallet/swap-proposal-approval-required])
        approval-amount-required         (rf/sub [:wallet/swap-proposal-approval-amount-required])
        currency-symbol                  (rf/sub [:profile/currency-symbol])
        approval-transaction-status      (rf/sub [:wallet/swap-approval-transaction-status])
        pay-input-num-value              (controlled-input/numeric-value input-state)
        pay-input-amount                 (controlled-input/input-value input-state)
        pay-token-symbol                 (:symbol asset-to-pay)
        pay-token-decimals               (:decimals asset-to-pay)
        pay-token-balance-selected-chain (get-in asset-to-pay
                                                 [:balances-per-chain
                                                  (:chain-id network) :balance]
                                                 0)
        pay-token-fiat-value             (str
                                          (utils/calculate-token-fiat-value
                                           {:currency currency
                                            :balance  pay-input-num-value
                                            :token    asset-to-pay}))
        available-crypto-limit           (number/remove-trailing-zeroes
                                          (.toFixed (money/bignumber
                                                     pay-token-balance-selected-chain)
                                                    (min pay-token-decimals
                                                         constants/min-token-decimals-to-display)))
        approval-amount-required-num     (when approval-amount-required
                                           (str (number/convert-to-whole-number
                                                 (native-module/hex-to-number
                                                  (hex/normalize-hex
                                                   approval-amount-required))
                                                 pay-token-decimals)))
        pay-input-error?                 (and (not (string/blank? pay-input-amount))
                                              (money/greater-than
                                               (money/bignumber pay-input-num-value)
                                               (money/bignumber
                                                pay-token-balance-selected-chain)))
        valid-pay-input?                 (and
                                          (not (string/blank?
                                                pay-input-amount))
                                          (> pay-input-amount 0)
                                          (not pay-input-error?))
        request-fetch-swap-proposal      (rn/use-callback
                                          (fn []
                                            (fetch-swap-proposal
                                             {:amount       pay-input-amount
                                              :valid-input? valid-pay-input?}))
                                          [pay-input-amount])]
    (rn/use-effect
     (fn []
       (request-fetch-swap-proposal))
     [pay-input-amount])
    [quo/swap-input
     {:type                 :pay
      :error?               pay-input-error?
      :token                pay-token-symbol
      :customization-color  :blue
      :show-approval-label? (and swap-proposal approval-required)
      :auto-focus?          true
      :show-keyboard?       false
      :status               (cond
                              (and loading-swap-proposal? (not input-focused?)) :loading
                              input-focused?                                    :typing
                              :else                                             :disabled)
      :currency-symbol      currency-symbol
      :on-token-press       on-token-press
      :on-max-press         #(on-max-press available-crypto-limit)
      :on-input-focus       on-input-focus
      :value                pay-input-amount
      :fiat-value           pay-token-fiat-value
      :network-tag-props    {:title    (i18n/label :t/max-token
                                                   {:number       available-crypto-limit
                                                    :token-symbol pay-token-symbol})
                             :networks [{:source (:source network)}]}
      :approval-label-props {:status              (case approval-transaction-status
                                                    :pending   :approving
                                                    :confirmed :approved
                                                    :finalised :approved
                                                    :approve)
                             :token-value         approval-amount-required-num
                             :button-props        {:on-press on-approve-press}
                             :customization-color account-color
                             :token-symbol        pay-token-symbol}}]))

(defn- swap-order-button
  [{:keys [on-press]}]
  (let [approval-required? (rf/sub [:wallet/swap-proposal-approval-required])]
    [quo/swap-order-button
     {:container-style (style/swap-order-button approval-required?)
      :on-press        on-press}]))

(defn- receive-token-input
  [{:keys [on-input-focus on-token-press input-focused?]}]
  (let [account-color            (rf/sub [:wallet/current-viewing-account-color])
        asset-to-receive         (rf/sub [:wallet/swap-asset-to-receive])
        loading-swap-proposal?   (rf/sub [:wallet/swap-loading-swap-proposal?])
        currency                 (rf/sub [:profile/currency])
        currency-symbol          (rf/sub [:profile/currency-symbol])
        amount-out               (rf/sub [:wallet/swap-proposal-amount-out])
        approval-required?       (rf/sub [:wallet/swap-proposal-approval-required])
        receive-token-symbol     (:symbol asset-to-receive)
        receive-token-decimals   (:decimals asset-to-receive)
        amount-out-whole-number  (when amount-out
                                   (number/convert-to-whole-number
                                    (native-module/hex-to-number
                                     (utils.hex/normalize-hex
                                      amount-out))
                                    receive-token-decimals))
        amount-out-num           (if amount-out-whole-number
                                   (number/remove-trailing-zeroes
                                    (.toFixed amount-out-whole-number receive-token-decimals))
                                   default-text-for-unfocused-input)
        receive-token-fiat-value (str (utils/calculate-token-fiat-value
                                       {:currency currency
                                        :balance  (or amount-out-whole-number 0)
                                        :token    asset-to-receive}))]
    [quo/swap-input
     {:type                 :receive
      :error?               false
      :token                receive-token-symbol
      :customization-color  account-color
      :show-approval-label? false
      :enable-swap?         true
      :input-disabled?      true
      :show-keyboard?       false
      :status               (cond
                              (and loading-swap-proposal? (not input-focused?)) :loading
                              input-focused?                                    :typing
                              :else                                             :disabled)
      :currency-symbol      currency-symbol
      :on-token-press       on-token-press
      :on-input-focus       on-input-focus
      :value                amount-out-num
      :fiat-value           receive-token-fiat-value
      :container-style      (style/receive-token-swap-input-container approval-required?)}]))

(defn- action-button
  [{:keys [on-press]}]
  (let [account-color               (rf/sub [:wallet/current-viewing-account-color])
        swap-proposal               (rf/sub [:wallet/swap-proposal])
        loading-fees?               (rf/sub [:wallet/swap-loading-fees?])
        loading-swap-proposal?      (rf/sub [:wallet/swap-loading-swap-proposal?])
        approval-required?          (rf/sub [:wallet/swap-proposal-approval-required])
        approval-transaction-status (rf/sub [:wallet/swap-approval-transaction-status])]
    [quo/bottom-actions
     {:actions          :one-action
      :button-one-label (i18n/label :t/review-swap)
      :button-one-props {:disabled?           (or (not swap-proposal)
                                                  (and approval-required?
                                                       (not= approval-transaction-status :confirmed))
                                                  loading-swap-proposal?
                                                  loading-fees?)
                         :customization-color account-color
                         :on-press            on-press}}]))

(defn view
  []
  (let [[pay-input-state set-pay-input-state]       (rn/use-state controlled-input/init-state)
        [pay-input-focused? set-pay-input-focused?] (rn/use-state true)
        error-response                              (rf/sub [:wallet/swap-error-response])
        loading-swap-proposal?                      (rf/sub [:wallet/swap-loading-swap-proposal?])
        swap-proposal                               (rf/sub [:wallet/swap-proposal])
        asset-to-pay                                (rf/sub [:wallet/swap-asset-to-pay])
        pay-input-amount                            (controlled-input/input-value pay-input-state)
        pay-token-decimals                          (:decimals asset-to-pay)
        on-review-swap-press                        (rn/use-callback
                                                     (fn []
                                                       (rf/dispatch [:navigate-to-within-stack
                                                                     [:screen/wallet.swap-confirmation
                                                                      :screen/wallet.setup-swap]])))
        on-press                                    (rn/use-callback
                                                     (fn [c]
                                                       (let
                                                         [new-text (str pay-input-amount c)
                                                          valid-amount?
                                                          (utils.string/valid-amount-for-token-decimals?
                                                           pay-token-decimals
                                                           new-text)]
                                                         (when valid-amount?
                                                           (set-pay-input-state
                                                            #(controlled-input/add-character % c))))))
        on-long-press                               (rn/use-callback
                                                     (fn []
                                                       (set-pay-input-state controlled-input/delete-all)
                                                       (rf/dispatch [:wallet/clean-suggested-routes])))
        delete                                      (rn/use-callback
                                                     (fn []
                                                       (set-pay-input-state
                                                        controlled-input/delete-last)
                                                       (rf/dispatch [:wallet/clean-swap-proposal])))
        on-max-press                                (rn/use-callback
                                                     (fn [max-value]
                                                       (set-pay-input-state
                                                        (fn [input-state]
                                                          (controlled-input/set-input-value
                                                           input-state
                                                           max-value)))))]
    [rn/view {:style style/container}
     [account-switcher/view
      {:on-press      on-close
       :icon-name     :i/arrow-left
       :margin-top    (safe-area/get-top)
       :switcher-type :select-account}]
     [rn/view {:style style/inputs-container}
      [pay-token-input
       {:input-state      pay-input-state
        :on-max-press     on-max-press
        :input-focused?   pay-input-focused?
        :on-token-press   #(js/alert "Token Pressed")
        :on-approve-press #(rf/dispatch [:open-modal :screen/wallet.swap-set-spending-cap])
        :on-input-focus   (fn []
                            (when platform/android? (rf/dispatch [:dismiss-keyboard]))
                            (set-pay-input-focused? true))}]
      [swap-order-button {:on-press #(js/alert "Swap Order Pressed")}]
      [receive-token-input
       {:input-focused? (not pay-input-focused?)
        :on-token-press #(js/alert "Token Pressed")
        :on-input-focus #(set-pay-input-focused? false)}]]
     [rn/view {:style style/footer-container}
      (when error-response
        [quo/alert-banner
         {:container-style style/alert-banner
          :text            (i18n/label :t/something-went-wrong-please-try-again-later)}])
      (when (or loading-swap-proposal? swap-proposal)
        [transaction-details])
      [action-button {:on-press on-review-swap-press}]]
     [quo/numbered-keyboard
      {:container-style style/keyboard-container
       :left-action     :dot
       :delete-key?     true
       :on-press        on-press
       :on-delete       delete
       :on-long-press   on-long-press}]]))

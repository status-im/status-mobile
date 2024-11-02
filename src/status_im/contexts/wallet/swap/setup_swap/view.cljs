(ns status-im.contexts.wallet.swap.setup-swap.view
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.common.events-helper :as events-helper]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.contexts.wallet.sheets.select-asset.view :as select-asset]
    [status-im.contexts.wallet.sheets.slippage-settings.view :as slippage-settings]
    [status-im.contexts.wallet.swap.setup-swap.style :as style]
    [status-im.contexts.wallet.swap.utils :as swap-utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]
    [utils.string :as utils.string]))

(def ^:private default-text-for-unfocused-input "0.00")
(def ^:private swap-proposal-debounce-time-ms 1000)

(defn- on-close
  [start-point]
  (when (= start-point :action-menu)
    (rf/dispatch [:centralized-metrics/track :metric/swap-closed]))
  (rf/dispatch [:wallet/clean-swap-proposal
                {:clean-amounts?              true
                 :clean-approval-transaction? true}])
  (events-helper/navigate-back))

(defn- start-get-swap-proposal
  [amount clean-approval-transaction?]
  (rf/dispatch [:wallet/stop-get-swap-proposal])
  (rf/dispatch [:wallet.swap/set-loading-swap-proposal])
  (debounce/debounce-and-dispatch [:wallet/start-get-swap-proposal
                                   {:amount-in                   amount
                                    :clean-approval-transaction? clean-approval-transaction?}]
                                  swap-proposal-debounce-time-ms))

(defn- fetch-swap-proposal
  [{:keys [amount valid-input? clean-approval-transaction?]}]
  (debounce/clear-all)
  (if valid-input?
    (start-get-swap-proposal amount clean-approval-transaction?)
    (rf/dispatch [:wallet/clean-swap-proposal
                  {:clean-amounts?              true
                   :clean-approval-transaction? clean-approval-transaction?}])))

(defn- data-item
  [{:keys [title subtitle size subtitle-icon subtitle-color on-press loading?]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :subtitle-type   (if subtitle-icon :editable :default)
    :status          (if loading? :loading :default)
    :title           title
    :subtitle        subtitle
    :size            size
    :icon            subtitle-icon
    :subtitle-color  subtitle-color
    :on-press        on-press}])

(defn- transaction-details
  []
  (let [theme                  (quo.theme/use-theme)
        swap-proposal-fee      (rf/sub [:wallet/wallet-swap-proposal-fee-fiat
                                        constants/token-for-fees-symbol])
        approval-gas-fee       (rf/sub [:wallet/approval-gas-fees])
        max-slippage           (rf/sub [:wallet/swap-max-slippage])
        loading-swap-proposal? (rf/sub [:wallet/swap-loading-swap-proposal?])
        currency-symbol        (rf/sub [:profile/currency-symbol])
        approval-required?     (rf/sub [:wallet/swap-proposal-approval-required])
        approval-status        (rf/sub [:wallet/swap-approval-transaction-status])
        max-fee                (if (and approval-required? (not= approval-status :confirmed))
                                 (money/add approval-gas-fee swap-proposal-fee)
                                 swap-proposal-fee)
        max-fee-formatted      (utils/fiat-formatted-for-ui
                                currency-symbol
                                max-fee)
        error-response         (rf/sub [:wallet/swap-error-response])]
    [rn/view {:style style/details-container}
     [data-item
      (cond-> {:title    (i18n/label :t/max-fees)
               :subtitle max-fee-formatted
               :loading? loading-swap-proposal?
               :size     :small}
        error-response (assoc :subtitle-color
                              (colors/theme-colors colors/danger-50
                                                   colors/danger-60
                                                   theme)))]
     [data-item
      {:title         (i18n/label :t/max-slippage)
       :subtitle      (str max-slippage "%")
       :subtitle-icon :i/edit
       :size          :small
       :on-press      (fn []
                        (when-not loading-swap-proposal?
                          (rf/dispatch [:show-bottom-sheet
                                        {:content slippage-settings/view}])))}]]))

(defn- pay-token-input
  [{:keys [input-state on-max-press on-input-focus on-token-press on-approve-press input-focused?]}]
  (let [account-color                    (rf/sub [:wallet/current-viewing-account-color])
        network                          (rf/sub [:wallet/swap-network])
        pay-token-symbol                 (rf/sub [:wallet/swap-asset-to-pay-symbol])
        pay-token-decimals               (rf/sub [:wallet/swap-asset-to-pay-decimals])
        loading-swap-proposal?           (rf/sub [:wallet/swap-loading-swap-proposal?])
        swap-proposal                    (rf/sub [:wallet/swap-proposal-without-fees])
        approval-required                (rf/sub [:wallet/swap-proposal-approval-required])
        approval-amount-required         (rf/sub [:wallet/swap-proposal-approval-amount-required])
        approval-transaction-status      (rf/sub [:wallet/swap-approval-transaction-status])
        approval-transaction-id          (rf/sub [:wallet/swap-approval-transaction-id])
        approved-amount                  (rf/sub [:wallet/swap-approved-amount])
        error-response                   (rf/sub [:wallet/swap-error-response])
        overlay-shown?                   (boolean (:sheets (rf/sub [:bottom-sheet])))
        input-ref                        (rn/use-ref-atom nil)
        set-input-ref                    (rn/use-callback (fn [ref] (reset! input-ref ref)))
        pay-input-num-value              (controlled-input/value-numeric input-state)
        pay-input-amount                 (controlled-input/input-value input-state)
        pay-token-balance-selected-chain (rf/sub [:wallet/swap-asset-to-pay-balance-for-chain
                                                  (:chain-id network)])
        pay-token-fiat-value             (rf/sub [:wallet/swap-asset-to-pay-amount-in-fiat
                                                  pay-input-num-value])
        available-crypto-limit           (money/bignumber
                                          pay-token-balance-selected-chain)
        display-decimals                 (min pay-token-decimals
                                              constants/min-token-decimals-to-display)
        available-crypto-limit-display   (rf/sub [:wallet/swap-asset-to-pay-balance-for-chain-ui
                                                  (:chain-id network)])
        approval-amount-required-num     (when approval-amount-required
                                           (number/to-fixed (number/hex->whole
                                                             approval-amount-required
                                                             pay-token-decimals)
                                                            pay-token-decimals))
        approval-label-token-value       (when (or approval-amount-required-num approved-amount)
                                           (utils/sanitized-token-amount-to-display
                                            (or approval-amount-required-num approved-amount)
                                            display-decimals))
        pay-input-error?                 (or (and (not (string/blank? pay-input-amount))
                                                  (money/greater-than
                                                   (money/bignumber pay-input-amount)
                                                   available-crypto-limit))
                                             (money/equal-to available-crypto-limit
                                                             (money/bignumber 0)))
        valid-pay-input?                 (and
                                          (not (string/blank?
                                                pay-input-amount))
                                          (> pay-input-amount 0)
                                          (not pay-input-error?))
        request-fetch-swap-proposal      (rn/use-callback
                                          (fn []
                                            (fetch-swap-proposal
                                             {:amount                      pay-input-amount
                                              :valid-input?                valid-pay-input?
                                              :clean-approval-transaction? true}))
                                          [pay-input-amount])]
    (rn/use-unmount #(rf/dispatch [:wallet/clean-swap]))
    (rn/use-effect
     (fn []
       (request-fetch-swap-proposal))
     [pay-input-amount])
    (rn/use-effect
     (fn []
       (when-not overlay-shown?
         (some-> @input-ref
                 (oops/ocall "focus"))))
     [overlay-shown?])
    [quo/swap-input
     {:get-ref              set-input-ref
      :type                 :pay
      :error?               pay-input-error?
      :token                pay-token-symbol
      :customization-color  :blue
      :show-approval-label? (or (and swap-proposal approval-required)
                                approval-transaction-id)
      :auto-focus?          true
      :show-keyboard?       false
      :status               (cond
                              (and loading-swap-proposal? (not input-focused?)) :loading
                              input-focused?                                    :typing
                              :else                                             :disabled)
      :on-token-press       on-token-press
      :on-max-press         #(on-max-press (str pay-token-balance-selected-chain))
      :on-input-focus       on-input-focus
      :value                pay-input-amount
      :fiat-value           pay-token-fiat-value
      :network-tag-props    {:title    (i18n/label :t/max-token
                                                   {:number       available-crypto-limit-display
                                                    :token-symbol pay-token-symbol})
                             :networks [{:source (:source network)}]}
      :approval-label-props {:status              (case approval-transaction-status
                                                    :pending   :approving
                                                    :confirmed :approved
                                                    :finalised :approved
                                                    :approve)
                             :token-value         approval-label-token-value
                             :button-props        (merge {:on-press on-approve-press}
                                                         (when (or loading-swap-proposal?
                                                                   error-response)
                                                           {:disabled? true}))
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
        prices-per-token         (rf/sub [:wallet/prices-per-token])
        receive-token-symbol     (:symbol asset-to-receive)
        receive-token-decimals   (:decimals asset-to-receive)
        amount-out-whole-number  (when amount-out
                                   (number/hex->whole amount-out receive-token-decimals))
        amount-out-num           (if amount-out-whole-number
                                   (number/to-fixed amount-out-whole-number receive-token-decimals)
                                   default-text-for-unfocused-input)
        receive-token-fiat-value (utils/formatted-token-fiat-value
                                  {:currency         currency
                                   :currency-symbol  currency-symbol
                                   :balance          (or amount-out-whole-number 0)
                                   :token            asset-to-receive
                                   :prices-per-token prices-per-token})]
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
      :on-token-press       on-token-press
      :on-input-focus       on-input-focus
      :value                amount-out-num
      :fiat-value           receive-token-fiat-value
      :container-style      (style/receive-token-swap-input-container approval-required?)}]))

(defn- alert-banner
  [{:keys [pay-input-error?]}]
  (let [error-response         (rf/sub [:wallet/swap-error-response])
        error-response-code    (rf/sub [:wallet/swap-error-response-code])
        error-response-details (rf/sub [:wallet/swap-error-response-details])
        error-text             (if pay-input-error?
                                 (i18n/label :t/insufficient-funds-for-swaps)
                                 (swap-utils/error-message-from-code error-response-code
                                                                     error-response-details))
        props                  (cond-> {:container-style      style/alert-banner
                                        :text-number-of-lines 0
                                        :text                 error-text}
                                 pay-input-error?
                                 (merge {:action?         true
                                         :on-button-press (fn []
                                                            (rf/dispatch [:centralized-metrics/track
                                                                          :metric/swap-buy-assets])
                                                            (rf/dispatch [:show-bottom-sheet
                                                                          {:content buy-token/view}]))
                                         :button-text     (i18n/label :t/add-assets)})
                                 (= error-response-code
                                    constants/router-error-code-not-enough-native-balance)
                                 (merge {:action?         true
                                         :on-button-press (fn []
                                                            (rf/dispatch [:centralized-metrics/track
                                                                          :metric/swap-buy-eth])
                                                            (rf/dispatch
                                                             [:show-bottom-sheet
                                                              {:content (fn []
                                                                          [buy-token/view])}]))
                                         :button-text     (i18n/label :t/add-eth)}))]
    (when (or pay-input-error? error-response)
      [quo/alert-banner props])))

(defn- action-button
  [{:keys [on-press]}]
  (let [account-color               (rf/sub [:wallet/current-viewing-account-color])
        swap-proposal               (rf/sub [:wallet/swap-proposal-without-fees])
        error-response              (rf/sub [:wallet/swap-error-response])
        loading-swap-proposal?      (rf/sub [:wallet/swap-loading-swap-proposal?])
        approval-required?          (rf/sub [:wallet/swap-proposal-approval-required])
        approval-transaction-status (rf/sub [:wallet/swap-approval-transaction-status])]
    [quo/bottom-actions
     {:actions          :one-action
      :button-one-label (i18n/label :t/review-swap)
      :button-one-props {:disabled?           (or (not swap-proposal)
                                                  error-response
                                                  (and approval-required?
                                                       (not= approval-transaction-status :confirmed))
                                                  loading-swap-proposal?)
                         :customization-color account-color
                         :on-press            on-press}}]))

(defn- pay-token-bottom-sheet
  []
  (let [asset-to-receive (rf/sub [:wallet/swap-asset-to-receive])
        network          (rf/sub [:wallet/swap-network])]
    [select-asset/view
     {:title            (i18n/label :t/select-asset-to-pay)
      :network          network
      :on-select        (fn [token]
                          (rf/dispatch [:wallet.swap/select-asset-to-pay {:token token}]))
      :hide-token-fn    (fn [type {:keys [balances-per-chain]}]
                          (let [balance (get-in balances-per-chain [(:chain-id network) :balance] "0")]
                            (and (= type constants/swap-tokens-my)
                                 (= balance "0"))))
      :disable-token-fn (fn [_ token]
                          (= (:symbol token)
                             (:symbol asset-to-receive)))}]))

(defn- receive-token-bottom-sheet
  []
  (let [asset-to-pay (rf/sub [:wallet/swap-asset-to-pay])
        network      (rf/sub [:wallet/swap-network])]
    [select-asset/view
     {:title            (i18n/label :t/select-asset-to-receive)
      :network          network
      :on-select        (fn [token]
                          (rf/dispatch [:wallet.swap/select-asset-to-receive {:token token}]))
      :disable-token-fn (fn [_ token]
                          (= (:symbol token)
                             (:symbol asset-to-pay)))}]))

(defn view
  []
  (let [[pay-input-state set-pay-input-state]       (rn/use-state controlled-input/init-state)
        [pay-input-focused? set-pay-input-focused?] (rn/use-state true)
        refetch-interval                            (rn/use-ref-atom nil)
        error-response                              (rf/sub [:wallet/swap-error-response])
        loading-swap-proposal?                      (rf/sub [:wallet/swap-loading-swap-proposal?])
        swap-proposal                               (rf/sub [:wallet/swap-proposal-without-fees])
        asset-to-pay                                (rf/sub [:wallet/swap-asset-to-pay])
        asset-to-receive                            (rf/sub [:wallet/swap-asset-to-receive])
        network                                     (rf/sub [:wallet/swap-network])
        pay-token-balance-selected-chain            (rf/sub [:wallet/swap-asset-to-pay-balance-for-chain
                                                             (:chain-id network)])
        start-point                                 (rf/sub [:wallet/swap-start-point])
        current-account-address                     (rf/sub [:wallet/current-viewing-account-address])
        pay-input-amount                            (controlled-input/input-value pay-input-state)
        pay-token-decimals                          (:decimals asset-to-pay)
        pay-input-error?                            (and (not (string/blank? pay-input-amount))
                                                         (money/greater-than
                                                          (money/bignumber pay-input-amount)
                                                          (money/bignumber
                                                           pay-token-balance-selected-chain)))
        valid-pay-input?                            (and
                                                     (not (string/blank?
                                                           pay-input-amount))
                                                     (> pay-input-amount 0)
                                                     (not pay-input-error?))
        on-review-swap-press                        (rn/use-callback
                                                     (fn []
                                                       (rf/dispatch [:wallet.swap/review-swap])))
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
                                                            #(controlled-input/add-character %
                                                                                             c
                                                                                             ##Inf)))))
                                                     [pay-input-amount pay-token-decimals])
        on-long-press                               (rn/use-callback
                                                     (fn []
                                                       (set-pay-input-state controlled-input/delete-all)
                                                       (rf/dispatch [:wallet/clean-suggested-routes])))
        delete                                      (rn/use-callback
                                                     (fn []
                                                       (set-pay-input-state
                                                        controlled-input/delete-last)
                                                       (rf/dispatch [:wallet/clean-swap-proposal
                                                                     {:clean-amounts? true
                                                                      :clean-approval-transaction?
                                                                      true}])))
        on-max-press                                (rn/use-callback
                                                     (fn [max-value]
                                                       (set-pay-input-state
                                                        (fn [input-state]
                                                          (controlled-input/set-input-value
                                                           input-state
                                                           max-value)))))
        on-refresh-swap-proposal                    (rn/use-callback
                                                     (fn []
                                                       (let
                                                         [bottom-sheets (rf/sub
                                                                         [:bottom-sheet-sheets])
                                                          approval-transaction-status
                                                          (rf/sub
                                                           [:wallet/swap-approval-transaction-status])]
                                                         (when-not valid-pay-input?
                                                           (when @refetch-interval
                                                             (js/clearTimeout @refetch-interval))
                                                           (reset! refetch-interval nil))
                                                         (when @refetch-interval
                                                           (js/clearTimeout @refetch-interval))
                                                         (reset! refetch-interval nil)
                                                         (when (and valid-pay-input?
                                                                    (not loading-swap-proposal?)
                                                                    (not bottom-sheets)
                                                                    (not= approval-transaction-status
                                                                          :pending))
                                                           (fetch-swap-proposal
                                                            {:amount pay-input-amount
                                                             :valid-input? valid-pay-input?
                                                             :clean-approval-transaction? false}))))
                                                     [valid-pay-input? loading-swap-proposal?
                                                      pay-input-amount])
        refetch-swap-proposal                       (fn []
                                                      (when valid-pay-input?
                                                        (fetch-swap-proposal
                                                         {:amount                      pay-input-amount
                                                          :valid-input?                valid-pay-input?
                                                          :clean-approval-transaction? true})))]
    (rn/use-effect (fn []
                     (when @refetch-interval
                       (js/clearInterval @refetch-interval)
                       (reset! refetch-interval nil))
                     (when (or swap-proposal error-response)
                       (reset! refetch-interval
                         (js/setInterval
                          on-refresh-swap-proposal
                          constants/swap-proposal-refresh-interval-ms))))
                   [swap-proposal error-response])
    (rn/use-effect (fn []
                     (rf/dispatch [:wallet/clean-swap-proposal
                                   {:clean-amounts?              false
                                    :clean-approval-transaction? true}])
                     (when @refetch-interval
                       (js/clearInterval @refetch-interval)
                       (reset! refetch-interval nil))
                     (refetch-swap-proposal))
                   [current-account-address])
    (rn/use-unmount (fn []
                      (rf/dispatch [:wallet/clean-swap-proposal
                                    {:clean-amounts?              true
                                     :clean-approval-transaction? true}])
                      (when @refetch-interval
                        (js/clearInterval @refetch-interval)
                        (reset! refetch-interval nil))))
    (rn/use-effect
     (fn []
       (when asset-to-pay
         (let [swap-amount (rf/sub [:wallet/swap-amount])]
           (when (and swap-amount refetch-interval)
             (js/clearTimeout @refetch-interval)
             (reset! refetch-interval nil))
           (cond (and swap-amount (not= swap-amount pay-input-amount))
                 (set-pay-input-state
                  (fn [input-state]
                    (controlled-input/set-input-value
                     input-state
                     swap-amount)))
                 (and pay-input-amount
                      (not (number/valid-decimal-count? pay-input-amount (:decimals asset-to-pay))))
                 (set-pay-input-state controlled-input/delete-all)
                 :else
                 (refetch-swap-proposal)))))
     [asset-to-pay])
    (rn/use-effect
     refetch-swap-proposal
     [asset-to-receive])
    [rn/view {:style style/container}
     [account-switcher/view
      {:on-press      #(on-close start-point)
       :icon-name     :i/arrow-left
       :margin-top    (safe-area/get-top)
       :switcher-type :select-account
       :params        {:show-account-balances? true
                       :asset-symbol           (:symbol asset-to-pay)
                       :network                network}}]
     [rn/view {:style style/inputs-container}
      [pay-token-input
       {:input-state      pay-input-state
        :on-max-press     on-max-press
        :input-focused?   pay-input-focused?
        :on-token-press   #(rf/dispatch [:show-bottom-sheet {:content pay-token-bottom-sheet}])
        :on-approve-press #(rf/dispatch [:wallet.swap/approve])
        :on-input-focus   (fn []
                            (when platform/android? (rf/dispatch [:dismiss-keyboard]))
                            (set-pay-input-focused? true))}]
      [swap-order-button
       {:on-press (fn []
                    (when @refetch-interval
                      (js/clearTimeout @refetch-interval)
                      (reset! refetch-interval nil))
                    (rf/dispatch [:wallet.swap/flip-assets]))}]
      [receive-token-input
       {:input-focused? (not pay-input-focused?)
        :on-token-press #(rf/dispatch [:show-bottom-sheet {:content receive-token-bottom-sheet}])
        :on-input-focus #(set-pay-input-focused? false)}]]
     [rn/view {:style style/footer-container}
      (when-not loading-swap-proposal? [alert-banner {:pay-input-error? pay-input-error?}])
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

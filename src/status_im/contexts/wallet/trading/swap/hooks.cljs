(ns status-im.contexts.wallet.trading.swap.hooks
  (:require [react-native.core :as rn]
            [status-im.common.controlled-input.utils :as controlled-input]
            [status-im.contexts.wallet.trading.swap.constants :as swap-constants]
            [status-im.contexts.wallet.trading.swap.utils :as utils]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]))

(defn use-init-swap-trading
  []
  (let [view-id           (rf/sub [:view-id])
        swap-initialized? (rf/sub [:trading.swap/initialized?])
        wallet-loaded?    (rf/sub [:wallet/home-tokens-loaded?])]
    (rn/use-effect (fn []
                     (when (and wallet-loaded?
                                (not swap-initialized?)
                                (= view-id :trading-stack))
                       (rf/dispatch [:trading.swap/init])))
                   [view-id wallet-loaded? swap-initialized?])))

(defn use-input-validation
  [pay-amount]
  (let [pay-token-balance (rf/sub [:trading.swap/pay-token-balance])]
    {:input-error? (utils/pay-input-error? pay-amount pay-token-balance)
     :valid-input? (utils/pay-input-valid? pay-amount pay-token-balance)}))

(defn use-swap-input
  [default-state]
  (let [[input-state set-input-state] (rn/use-state (controlled-input/init-value default-state))
        add-input-char                (rn/use-callback (fn [character]
                                                         (set-input-state
                                                          #(controlled-input/add-character %
                                                                                           character
                                                                                           ##Inf)))
                                                       [set-input-state])
        delete-last-input-char        (rn/use-callback (fn []
                                                         (set-input-state
                                                          controlled-input/delete-last))
                                                       [set-input-state])
        set-input-value               (rn/use-callback (fn [value]
                                                         (set-input-state
                                                          (fn [input-state]
                                                            (controlled-input/set-input-value
                                                             input-state
                                                             value))))
                                                       [set-input-state])

        clear-input-value             (rn/use-callback (fn []
                                                         (set-input-state controlled-input/delete-all))
                                                       [set-input-state])]
    {:value                  (controlled-input/input-value input-state)
     :add-input-char         add-input-char
     :delete-last-input-char delete-last-input-char
     :set-input-value        set-input-value
     :clear-input-value      clear-input-value}))

(defn- use-ref-value
  [value]
  (let [value-ref (rn/use-ref-atom value)]
    (rn/use-effect
     (fn [] (reset! value-ref value))
     [value])

    value-ref))

(defn use-suggested-routes
  [input-amount]
  (let [pay-token-symbol       (rf/sub [:trading.swap/pay-token-symbol])
        receive-token-symbol   (rf/sub [:trading.swap/receive-token-symbol])
        account-address        (rf/sub [:trading.swap/account-address])
        chain-id               (rf/sub [:trading.swap/chain-id])
        {:keys [valid-input?]} (use-input-validation input-amount)
        valid-input-ref?       (use-ref-value valid-input?)]

    (rn/use-effect (fn []
                     (when-not valid-input?
                       (debounce/debounce-and-dispatch [:trading.swap/stop-get-routes]
                                                       swap-constants/get-suggested-routes-debounce-ms)))
                   [valid-input?])

    ;; TODO: add canceling route request
    (rn/use-effect (fn []
                     (when @valid-input-ref?
                       (debounce/debounce-and-dispatch [:trading.swap/get-suggested-routes input-amount]
                                                       swap-constants/get-suggested-routes-debounce-ms)))
                   [input-amount pay-token-symbol receive-token-symbol account-address chain-id])))

(defn use-approval-finished
  [cb]
  (let [last-approval-status (rf/sub [:trading.swap/last-approval-status])]
    (rn/use-effect
     (fn []
       (when (#{:confirmed :failed} last-approval-status)
         (cb)))
     [last-approval-status])))

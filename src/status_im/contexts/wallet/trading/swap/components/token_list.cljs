(ns status-im.contexts.wallet.trading.swap.components.token-list
  (:require [status-im.constants :as constants]
            [status-im.contexts.wallet.sheets.select-asset.view :as select-asset]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- view-fn
  [current-token-symbol]
  (fn []
    (let [network               (rf/sub [:trading.swap/network])
          pay-token-symbol      (rf/sub [:trading.swap/pay-token-symbol])
          receive-token-symbol  (rf/sub [:trading.swap/receive-token-symbol])
          address               (rf/sub [:trading.swap/account-address])
          pay-token-list?       (= current-token-symbol pay-token-symbol)
          disable-current-token (fn [_ token]
                                  (or (= (:symbol token)
                                         pay-token-symbol)
                                      (= (:symbol token)
                                         receive-token-symbol)))]
      [select-asset/view
       (cond->
         {:network          network
          :address          address
          :disable-token-fn disable-current-token}

         pay-token-list?
         (assoc
          :title (i18n/label :t/select-asset-to-pay)
          :on-select
          (fn [token]
            (if pay-token-list?
              (rf/dispatch [:trading.swap/set-pay-token (:symbol token)])
              (rf/dispatch [:trading.swap/set-receive-token (:symbol token)])))
          :hide-token-fn (fn [type {:keys [balances-per-chain]}]
                           (let [balance
                                 (get-in balances-per-chain [(:chain-id network) :balance] "0")]
                             (and (= type constants/swap-tokens-my)
                                  (= balance "0")))))

         (not pay-token-list?)
         (assoc :title     (i18n/label :t/select-asset-to-receive)
                :on-select (fn [token]
                             (rf/dispatch [:trading.swap/set-receive-token (:symbol token)]))))])))

(defn show
  [current-token-symbol]
  (fn []
    (rf/dispatch [:show-bottom-sheet
                  {:content (view-fn current-token-symbol)
                   :options {:overlay {:interceptTouchOutside false}}}])))

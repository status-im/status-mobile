(ns status-im.contexts.wallet.trading.swap.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as common.utils]
            [status-im.contexts.wallet.trading.swap.constants :as swap-constants]
            [status-im.contexts.wallet.trading.swap.routes :as routes]
            [status-im.contexts.wallet.trading.swap.utils :as utils]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.number :as number]))

(def swap-db-path [:wallet :ui :trading-swap])

(defn- get-default-state
  [db]
  (let [account-address (-> db utils/default-account :address)]
    {:pay-token-symbol     swap-constants/default-pay-asset
     :receive-token-symbol swap-constants/default-receive-asset
     :pay-amount           swap-constants/default-pay-amount
     :max-slippage         swap-constants/default-slippage
     :chain-id             swap-constants/default-network-chain-id
     :account-address      account-address}))

(rf/reg-event-fx :trading.swap/initialize-state
 (fn [{:keys [db]}
      [{:keys [pay-amount pay-token-symbol receive-token-symbol chain-id max-slippage
               account-address]}]]
   {:db (update-in db
                   swap-db-path
                   assoc
                   :focused?             false
                   :account-address      account-address
                   :pay-token-symbol     pay-token-symbol
                   :pay-amount           pay-amount
                   :receive-token-symbol receive-token-symbol
                   :chain-id             chain-id
                   :max-slippage         max-slippage
                   :initialized?         true)}))

(rf/reg-event-fx :trading.swap/init
 (fn [{:keys [db]}]
   (let [default-state (get-default-state db)]
     {:fx [[:dispatch
            [:trading.swap/initialize-state default-state]]
           [:dispatch
            [:centralized-metrics/track :metric/swap-start
             {:network       (:chain-id default-state)
              :pay_token     (:pay-token-symbol default-state)
              :receive_token (:receive-token-symbol default-state)
              :start_point   (:view-id db)
              :launch_screen :trading-tab}]]]})))

(rf/reg-event-fx :trading.swap/reset-swap
 (fn [{:keys [db]}]
   {:db (update-in db swap-db-path dissoc :pay-amount :requested-route-uuid :approvals :focused?)}))

;; == setters ==

(rf/reg-event-fx :trading.swap/focus-input
 (fn [{:keys [db]}]
   {:db (update-in db swap-db-path assoc :focused? true)}))

(rf/reg-event-fx :trading.swap/blur-input
 (fn [{:keys [db]}]
   {:db (update-in db swap-db-path assoc :focused? false)}))

(rf/reg-event-fx :trading.swap/set-pay-amount
 (fn [{:keys [db]} [amount]]
   {:db (update-in db swap-db-path assoc :pay-amount amount)}))

(rf/reg-event-fx :trading.swap/set-pay-token
 (fn [{:keys [db]} [token-symbol]]
   {:db (update-in db swap-db-path assoc :pay-token-symbol token-symbol)}))

(rf/reg-event-fx :trading.swap/set-receive-token
 (fn [{:keys [db]} [token-symbol]]
   {:db (update-in db swap-db-path assoc :receive-token-symbol token-symbol)}))

(rf/reg-event-fx :trading.swap/set-account-address
 (fn [{:keys [db]} [address]]
   {:db (update-in db swap-db-path assoc :account-address address)}))

(rf/reg-event-fx :trading.swap/set-chain-id
 (fn [{:keys [db]} [chain-id]]
   {:db (update-in db swap-db-path assoc :chain-id chain-id)}))

(rf/reg-event-fx :trading.swap/set-max-slippage
 (fn [{:keys [db]} [max-slippage]]
   {:db (update-in db swap-db-path assoc :max-slippage (number/parse-float max-slippage))}))

;; == routes ==

(rf/reg-event-fx :trading.swap/get-suggested-routes
 (fn [{:keys [db]} [pay-amount]]
   (let [{:keys [account-address pay-token-symbol
                 receive-token-symbol chain-id]} (get-in db swap-db-path)
         disabled-chain-ids                      (utils/router-disabled-chain-ids db chain-id)
         amount-in-hex                           (->> (utils/token-from-symbol db pay-token-symbol)
                                                      :decimals
                                                      (money/unit->token pay-amount)
                                                      money/to-hex)
         request-uuid                            (str (random-uuid))
         route-input-params                      {:uuid                 request-uuid
                                                  :sendType             constants/send-type-swap
                                                  :gasFeeMode           constants/gas-rate-medium
                                                  :addrFrom             account-address
                                                  :addrTo               account-address
                                                  :tokenID              pay-token-symbol
                                                  :toTokenID            receive-token-symbol
                                                  :disabledFromChainIDs disabled-chain-ids
                                                  :disabledToChainIDs   disabled-chain-ids
                                                  :amountIn             (or amount-in-hex "0x0")
                                                  :amountOut            "0x0"
                                                  :fromLockedAmount     {}}]
     (when (and (seq pay-amount) (not (zero? pay-amount)))
       (log/info "Requesting Swap route for" pay-amount pay-token-symbol "id: " request-uuid)
       {:db (update-in db
                       swap-db-path
                       (fn [swap]
                         (-> swap
                             (assoc
                              :requested-route-uuid
                              request-uuid))))
        :fx [[:dispatch
              [:wallet.routes/get-routes
               {:params   route-input-params
                :on-error (fn [error]
                            (rf/dispatch [:trading.swap/suggested-routes-error error]))}]]
             [:dispatch
              [:centralized-metrics/track :metric/swap-proposal-start
               {:network       chain-id
                :pay_token     pay-token-symbol
                :receive_token receive-token-symbol}]]]}))))

(rf/reg-event-fx :trading.swap/stop-get-routes
 (fn [{:keys [db]}]
   {:db (update-in db swap-db-path dissoc :requested-route-uuid)
    :fx [[:dispatch [:wallet.routes/stop-get-routes]]]}))

(rf/reg-event-fx :trading.swap/suggested-routes-error
 (fn [{:keys [db]} [error]]
   (let [{:keys [requested-route-uuid]} (get-in db swap-db-path)]
     (log/error "failed to get suggested routes (async)"
                {:event  :trading.swap/get-suggested-routes
                 :error  (:message error)
                 :params requested-route-uuid})
     {:fx [[:dispatch
            [:centralized-metrics/track :metric/swap-proposal-failed {:error (:code error)}]]]})))

(rf/reg-event-fx :trading.swap/reset-route
 (fn [{:keys [db]}]
   {:db (update-in db swap-db-path dissoc :requested-route-uuid)}))

(rf/reg-event-fx :trading.swap/flip-tokens
 (fn [{:keys [db]}]
   (let [{:keys [network pay-token-symbol
                 receive-token-symbol]} (get-in db swap-db-path)]
     {:db (update-in db
                     swap-db-path
                     (fn [swap-data]
                       (-> swap-data
                           (assoc
                            :pay-token-symbol     receive-token-symbol
                            :receive-token-symbol pay-token-symbol))))
      :fx [[:dispatch [:trading.swap/reset-route]]
           [:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-pay-changed
             {:network        (:chain-id network)
              :previous_token pay-token-symbol
              :new_token      receive-token-symbol}]]
           [:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-receive-changed
             {:network        (:chain-id network)
              :previous_token receive-token-symbol
              :new_token      pay-token-symbol}]]]})))

(rf/reg-event-fx :trading.swap/approve-confirmation
 (fn [{:keys [db]}]
   (let [{:keys [requested-route-uuid max-slippage]} (get-in db swap-db-path)]
     {:fx [[:dispatch
            [:wallet.routes/build-transactions
             {:request-uuid requested-route-uuid
              :slippage     max-slippage}]]]})))

(rf/reg-event-fx :trading.swap/on-approval-sent
 (fn [{:keys [db]} [approval-transaction]]
   (let [approval-hash                                                  (:hash approval-transaction)
         {:keys [account-address chain-id pay-token-symbol pay-amount]} (get-in db swap-db-path)]
     (log/info "Swap approval sent" approval-hash)
     {:db (update-in db
                     (conj swap-db-path :approvals)
                     (fnil into [])
                     [{:transaction-hash approval-hash
                       :account-address  account-address
                       :chain-id         chain-id
                       :pay-amount       pay-amount
                       :pay-token-symbol pay-token-symbol}])})))

(rf/reg-event-fx :trading.swap/on-approval-finished
 (fn [{:keys [db]} [approval-status]]
   (let [{:keys [chain-id
                 pay-token-symbol pay-amount
                 account-address]} (get-in db swap-db-path)
         route                     (get-in db [:wallet :route])
         provider-name             (-> route :best-route routes/processor-name)
         account-name              (get-in db [:wallet :accounts account-address :name])
         toast-label-args          {:token-amount  pay-amount
                                    :token-symbol  pay-token-symbol
                                    :provider-name provider-name
                                    :account-name  account-name}]
     (log/info "Swap approval confirmed for" pay-amount pay-token-symbol)
     {:fx [(if (= :confirmed approval-status)
             [:dispatch
              [:toasts/upsert
               {:type :positive
                :text (i18n/label :t/spending-cap-set toast-label-args)}]]
             [:dispatch
              [:toasts/upsert
               {:type :negative
                :text (i18n/label :t/spending-cap-failed toast-label-args)}]])
           [:dispatch
            [:centralized-metrics/track :metric/swap-approval-execution-finished
             {:network   chain-id
              :pay_token pay-token-symbol
              :succeeded (= approval-status :confirmed)}]]]})))

(rf/reg-event-fx :trading.swap/swap-confirmation
 (fn [{:keys [db]}]
   (let [{:keys [requested-route-uuid max-slippage]} (get-in db swap-db-path)]
     {:fx [[:dispatch
            [:wallet.routes/build-transactions
             {:request-uuid requested-route-uuid
              :slippage     max-slippage}]]]})))

(rf/reg-event-fx :trading.swap/on-swap-sent
 (fn [{:keys [db]} [error]]
   (let [{:keys [account-address pay-amount
                 pay-token-symbol receive-token-symbol
                 route]}          (get-in db swap-db-path)
         {:keys [decimals whole]} (routes/amount-out route)
         receive-amount           (common.utils/sanitized-token-amount-to-display
                                   whole
                                   (min decimals
                                        constants/min-token-decimals-to-display))]
     {:fx [[:dispatch [:trading.swap/reset-swap]]
           [:dispatch-later
            {:ms       500
             :dispatch (if error
                         [:toast/upsert
                          {:id   :swap-transaction-error
                           :type :negative
                           :text :t/swap-failed}]
                         [:toasts/upsert
                          {:id   :swap-transaction-pending
                           :icon :i/info
                           :type :neutral
                           :text (i18n/label :t/swapping-to
                                             {:pay-amount           pay-amount
                                              :pay-token-symbol     pay-token-symbol
                                              :receive-token-symbol receive-token-symbol
                                              :receive-amount       receive-amount})}])}]
           [:dispatch [:shell/change-tab :wallet-stack]]
           [:dispatch [:wallet/navigate-to-account-within-stack account-address]]
           [:dispatch [:wallet/select-account-tab :activity]]]})))

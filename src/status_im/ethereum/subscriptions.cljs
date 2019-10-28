(ns status-im.ethereum.subscriptions
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.tokens :as tokens]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.wallet.db :as wallet]
            [taoensso.timbre :as log]))

(fx/defn handle-subscription-data
  {:events [::subcription-data-received]}
  [cofx {:keys [subscription_id data] :as event}]
  (if-let [handler (get-in cofx [:db :ethereum/subscriptions subscription_id])]
    (handler data)
    (log/warn ::unknown-subscription :event event)))

(fx/defn handle-error
  {:events [::subcription-error-received]}
  [cofx {:keys [subscription_id data] :as event}]
  (log/error ::error event))

(fx/defn register-subscription
  [{:keys [db]} id handler]
  {:db (assoc-in db [:ethereum/subscriptions id] handler)})

(fx/defn new-block
  [{:keys [db] :as cofx} historical? block-number accounts]
  (let [{:keys [:wallet/all-tokens]} db
        chain (ethereum/chain-keyword db)
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))]
    (fx/merge cofx
              (cond-> {}
                (not historical?)
                (assoc :db (assoc db :ethereum/current-block block-number))

                ;;NOTE only get transfers if the new block contains some
                ;;     from/to one of the multiaccount accounts
                (not-empty accounts)
                (assoc ::transactions/get-transfers {:chain-tokens chain-tokens
                                                     :from-block block-number
                                                     :historical? historical?}))
              (transactions/check-watched-transactions))))

(fx/defn reorg
  [{:keys [db] :as cofx} block-number accounts]
  (let [{:keys [:wallet/all-tokens]} db
        chain (ethereum/chain-keyword db)
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))]
    {:db (update-in db [:wallet :transactions]
                    wallet/remove-transactions-since-block block-number)
     ::transactions/get-transfers {:chain-tokens chain-tokens
                                   :from-block block-number}}))

(fx/defn new-wallet-event
  {:events [::new-wallet-event]}
  [{:keys [db] :as cofx} {:keys [type blockNumber accounts] :as event}]
  (case type
    "newblock" (new-block cofx false blockNumber accounts)
    "history" (new-block cofx true blockNumber accounts)
    "reorg" (reorg cofx blockNumber accounts)
    (log/warn ::unknown-wallet-event :type type :event event)))

(defonce wallet-listener
  (status/add-listener "wallet"
                       #(re-frame/dispatch [::new-wallet-event %])
                       true))

(defonce subscription-data-listener
  (status/add-listener "subscriptions.data"
                       #(re-frame/dispatch [::subcription-data-received %])
                       true))

(defonce subscription-error-listener
  (status/add-listener "subscriptions.error"
                       #(re-frame/dispatch [::subcription-error-received %])
                       true))

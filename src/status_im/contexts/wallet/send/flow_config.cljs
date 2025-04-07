(ns status-im.contexts.wallet.send.flow-config
  (:require
    [status-im.contexts.wallet.db :as db]
    [status-im.contexts.wallet.db-path :as db-path]
    [status-im.contexts.wallet.send.utils :as send-utils]))

(defn- collectible-selected?
  [db]
  (let [{collectible-stored :collectible
         tx-type            :tx-type} (db/send db)]
    (and (some? collectible-stored)
         (send-utils/tx-type-collectible? tx-type))))

(defn- token-selected?
  [db]
  (-> db db/send :token some?))

(def steps
  [{:screen-id  :screen/wallet.select-address
    :skip-step? (fn [db] (some? (get-in db (conj db-path/send :recipient))))}
   {:screen-id  :screen/wallet.select-asset
    :skip-step? (fn [db] (or (token-selected? db) (collectible-selected? db)))}
   {:screen-id  :screen/wallet.send-input-amount
    :skip-step? (fn [db]
                  (-> db db/send :tx-type send-utils/tx-type-collectible?))}
   {:screen-id  :screen/wallet.select-collectible-amount
    :skip-step? (fn [db]
                  (or (not (collectible-selected? db))
                      (some? (get-in db (conj db-path/send :amount)))))}
   {:screen-id :screen/wallet.transaction-confirmation}])

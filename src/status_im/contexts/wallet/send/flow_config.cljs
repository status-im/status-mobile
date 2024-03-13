(ns status-im.contexts.wallet.send.flow-config)

(def send-asset
  [{:screen-id  :screen/wallet.select-address
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :recipient])))}
   {:screen-id  :screen/wallet.select-asset
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :token])))}
   {:screen-id  :screen/wallet.send-input-amount
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :amount])))}
   {:screen-id :screen/wallet.transaction-confirmation}
   {:screen-id :screen/wallet.transaction-progress}])
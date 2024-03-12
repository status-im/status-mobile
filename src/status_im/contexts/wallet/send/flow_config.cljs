(ns status-im.contexts.wallet.send.flow-config)

(def send-asset
  [{:screen-id  :wallet-select-address
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :recipient])))}
   {:screen-id  :wallet-select-asset
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :token])))}
   {:screen-id  :wallet-send-input-amount
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :amount])))}
   {:screen-id :wallet-transaction-confirmation}])
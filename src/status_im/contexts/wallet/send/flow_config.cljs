(ns status-im.contexts.wallet.send.flow-config)

(def send-asset
  [{:screen-id  :wallet-select-address
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :recipient])))
    :event      :wallet/select-send-address}
   {:screen-id  :wallet-select-asset
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :token])))
    :event      :wallet/send-select-token}
   {:screen-id  :wallet-send-input-amount
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :amount])))
    :event      :wallet/send-select-amount}
   {:screen-id :wallet-transaction-confirmation}])
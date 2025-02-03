(ns status-im.contexts.wallet.bridge.flow-config)

(def steps
  [{:screen-id  :screen/wallet.bridge-select-asset
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :token])))}
   {:screen-id  :screen/wallet.bridge-to
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :bridge-to-chain-id])))}
   {:screen-id  :screen/wallet.bridge-input-amount
    :skip-step? (fn [db] (some? (get-in db [:wallet :ui :send :amount])))}
   {:screen-id :screen/wallet.transaction-confirmation}])

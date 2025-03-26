(ns status-im.contexts.wallet.networks.events
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.networks.db :as networks.db]
            [status-im.contexts.wallet.rpc-data-store.networks :as rpc-data-store.networks]))

(rf/reg-event-fx
 :wallet/get-ethereum-chains
 (fn [_]
   {:json-rpc/call
    [{:method     "wallet_getEthereumChains"
      :params     []
      :on-success [:wallet/get-ethereum-chains-success]
      :on-error   [:wallet/log-rpc-error {:event :wallet/get-ethereum-chains}]}]}))

(rf/reg-event-fx
 :wallet/get-ethereum-chains-success
 (fn [{:keys [db]} [data]]
   (let [network-data          (rpc-data-store.networks/rpc->networks data)
         networks-by-id        (rpc-data-store.networks/networks-by-id network-data)
         default-network-names (->> (networks.db/get-testnet-mode-key db)
                                    (get network-data)
                                    (map :network-name)
                                    set)]
     {:db (-> db
              (assoc-in [:wallet :networks] network-data)
              (assoc-in [:wallet :networks-by-id] networks-by-id)
              (assoc-in [:wallet :ui :network-filter :default-networks] default-network-names))
      :fx [[:dispatch [:wallet.tokens/get-token-list]]
           [:dispatch [:wallet/reset-selected-networks]]]})))

(rf/reg-event-fx
 :wallet/navigate-to-chain-explorer
 (fn [{:keys [db]} [chain-id address]]
   (let [explorer-link (networks.db/get-block-explorer-address-url db chain-id address)]
     {:fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:browser.ui/open-url explorer-link]]]})))

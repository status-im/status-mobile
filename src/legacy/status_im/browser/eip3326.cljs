;reference https://eips.ethereum.org/EIPS/eip-3326 EIP-3326: Wallet Switch Ethereum Chain RPC Method
;(`wallet_switchEthereumChain`)
(ns legacy.status-im.browser.eip3326
  (:require
    [legacy.status-im.ui.screens.browser.eip3326.sheet :as sheet]
    [status-im2.constants :as constants]
    [utils.ethereum.chain :as chain]
    [utils.re-frame :as rf]))

(rf/defn deny-permission
  {:events [:eip3326.ui/dapp-permission-denied]}
  [{:keys [db] :as cofx} message-id _]
  {:browser/send-to-bridge {:type      constants/web3-send-async-callback
                            :messageId message-id
                            :error     {:code    4001
                                        :message "User rejected the request."}}
   :dispatch               [:bottom-sheet/hide-old]})

(rf/defn handle-switch-ethereum-chain
  {:events [:eip3326/handle-switch-ethereum-chain]}
  [{:keys [db] :as cofx} dapp-name id message-id {:keys [chainId] :as params}]
  (let [target-chain-id  (js/parseInt chainId 16)
        networks         (vals (get-in db [:networks/networks]))
        exist-chain-ids  (set (map chain/network->chain-id networks))
        current-chain-id (chain/chain-id db)]
    (if (exist-chain-ids target-chain-id)
      (if (= current-chain-id target-chain-id)
        {:browser/send-to-bridge {:type      constants/web3-send-async-callback
                                  :messageId message-id
                                  :result    {:jsonrpc "2.0"
                                              :id      (int id)
                                              :result  nil}}}
        (let [target-network    (first (filter #(= (chain/network->chain-id %1) target-chain-id)
                                               networks))
              target-network-id (:id target-network)
              current-network   (chain/current-network db)
              network-from      (:name current-network)
              network-to        (:name target-network)
              params            (assoc params
                                       :target-network-id target-network-id
                                       :network-from      network-from
                                       :network-to        network-to)]
          {:dispatch [:bottom-sheet/show-sheet-old
                      {:content (fn []
                                  [sheet/permissions-panel dapp-name message-id params])}]}))
      {:browser/send-to-bridge {:type constants/web3-send-async-callback
                                :messageId message-id
                                :error
                                {:code 4902
                                 :message
                                 (str
                                  "Unrecognized chain ID: "
                                  target-chain-id
                                  ". Try adding the chain using wallet_addEthereumChain first.")}}})))


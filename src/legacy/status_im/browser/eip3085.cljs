;reference https://eips.ethereum.org/EIPS/eip-3085 EIP-3085: Wallet Add Ethereum Chain RPC Method
;(`wallet_addEthereumChain`)
(ns legacy.status-im.browser.eip3085
  (:require
    [clojure.string :as string]
    [legacy.status-im.network.core :as network]
    [legacy.status-im.ui.screens.browser.eip3085.sheet :as sheet]
    [legacy.status-im.utils.random :as random]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn send-success-call-to-bridge
  {:events [:eip3085/send-success-call-to-bridge]}
  [_ id messageId]
  {:browser/send-to-bridge {:type      constants/web3-send-async-callback
                            :messageId messageId
                            :result    {:jsonrpc "2.0"
                                        :id      (int id)
                                        :result  nil}}})

(rf/defn allow-permission
  {:events [:eip3085.ui/dapp-permission-allowed]}
  [{:keys [db] :as cofx} message-id {:keys [new-networks id]}]
  {:db            (assoc db :networks/networks new-networks)
   :json-rpc/call [{:method     "settings_saveSetting"
                    :params     [:networks/networks (vals new-networks)]
                    :on-success #(re-frame/dispatch [:eip3085/send-success-call-to-bridge cofx id
                                                     message-id])
                    :on-error   #(log/error "failed to perform settings_saveSetting" %)}]
   :dispatch      [:bottom-sheet/hide-old]})

(rf/defn deny-permission
  {:events [:eip3085.ui/dapp-permission-denied]}
  [_ message-id _]
  {:browser/send-to-bridge {:type      constants/web3-send-async-callback
                            :messageId message-id
                            :error     {:code    4001
                                        :message "User rejected the request."}}
   :dispatch               [:bottom-sheet/hide-old]})

(rf/defn handle-add-ethereum-chain
  {:events [:eip3085/handle-add-ethereum-chain]}
  [{{:networks/keys [networks] :as db} :db :as cofx}
   dapp-name id message-id
   {:keys [chainId blockExplorerUrls chainName iconUrls nativeCurrency rpcUrls] :as params}]
  (let [manage {:name       {:value chainName}
                :symbol     {:value (:symbol nativeCurrency)}
                :url        {:value (first rpcUrls)}
                :network-id {:value chainId}
                :chain      {:value :custom}}]
    (if (network/valid-manage? manage)
      (let [{:keys [name url chain network-id symbol]} manage
            random-id                                  (string/replace (random/id) "-" "")
            network                                    (network/new-network random-id
                                                                            (:value name)
                                                                            (:value symbol)
                                                                            (:value url)
                                                                            (:value chain)
                                                                            (:value network-id))
            new-networks                               (assoc networks random-id network)
            params                                     (assoc params
                                                              :new-networks new-networks
                                                              :id           id
                                                              :new-network  network)]
        (if (network/chain-id-available? networks network)
          {:dispatch [:bottom-sheet/show-sheet-old
                      {:content (fn []
                                  [sheet/permissions-panel dapp-name message-id params])}]}
          (send-success-call-to-bridge cofx id message-id)))
      {:browser/send-to-bridge {:type      constants/web3-send-async-callback
                                :messageId message-id
                                :error     {:code    -32602
                                            :message "invalid network parameters"}}})))


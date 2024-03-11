;reference https://eips.ethereum.org/EIPS/eip-3085 EIP-3085: Wallet Add Ethereum Chain RPC Method
;(`wallet_addEthereumChain`)
(ns legacy.status-im.browser.eip3085
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
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


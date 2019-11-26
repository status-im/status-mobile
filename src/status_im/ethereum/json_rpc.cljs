(ns status-im.ethereum.json-rpc
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.decode :as decode]
            [status-im.native-module.core :as status]
            [status-im.utils.money :as money]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def json-rpc-api
  {"eth_call" {}
   "eth_getBalance"
   {:on-result money/bignumber}
   "eth_estimateGas"
   {:on-result #(money/bignumber (int (* % 1.2)))}
   "eth_gasPrice"
   {:on-result money/bignumber}
   "eth_getBlockByHash"
   {:on-result #(-> (update % :number decode/uint)
                    (update :timestamp decode/uint))}
   "eth_getTransactionByHash" {}
   "eth_getTransactionReceipt" {}
   "eth_newBlockFilter" {:subscription? true}
   "eth_newFilter" {:subscription? true}
   "eth_syncing" {}
   "net_version" {}
   "web3_clientVersion" {}
   "shhext_post" {}
   "shh_generateSymKeyFromPassword" {}
   "shh_getSymKey" {}
   "shh_markTrustedPeer" {}
   "shhext_requestMessages" {}
   "shhext_sendDirectMessage" {}
   "shhext_sendPublicMessage" {}
   "shhext_enableInstallation" {}
   "shhext_disableInstallation" {}
   "shhext_sendChatMessage" {}
   "shhext_reSendChatMessage" {}
   "shhext_getOurInstallations" {}
   "shhext_setInstallationMetadata" {}
   "shhext_loadFilters" {}
   "shhext_loadFilter" {}
   "shhext_removeFilters" {}
   "shhext_chats" {}
   "shhext_addSystemMessages" {}
   "shhext_deleteMessagesFrom" {}
   "shhext_deleteMessagesByChatID" {}
   "shhext_deleteMessage" {}
   "shhext_markMessagesSeen" {}
   "shhext_confirmMessagesProcessedByID" {}
   "shhext_updateMessageOutgoingStatus" {}
   "shhext_chatMessages" {}
   "shhext_saveChat" {}
   "shhext_contacts" {}
   "shhext_prepareContent" {}
   "shhext_blockContact" {}
   ;;TODO not used anywhere?
   "shhext_deleteChat" {}
   "shhext_saveContact" {}
   "shhext_verifyENSNames" {}
   "status_chats" {}
   "wallet_getTransfers" {}
   "wallet_getTokensBalances" {}
   "browsers_getBrowsers" {}
   "browsers_addBrowser" {}
   "browsers_deleteBrowser" {}
   "mailservers_getMailserverRequestGaps" {}
   "mailservers_addMailserverRequestGaps" {}
   "mailservers_deleteMailserverRequestGaps" {}
   "mailservers_deleteMailserverRequestGapsByChatID" {}
   "permissions_addDappPermissions" {}
   "permissions_getDappPermissions" {}
   "permissions_deleteDappPermissions" {}
   "settings_saveConfig" {}
   "settings_getConfig" {}
   "settings_getConfigs" {}
   "settings_saveNodeConfig" {}
   "accounts_getAccounts" {}
   "accounts_saveAccounts" {}
   "mailservers_ping" {}
   "mailservers_addMailserver" {}
   "mailservers_getMailservers" {}
   "mailservers_deleteMailserver" {}
   "mailservers_addMailserverTopic" {}
   "mailservers_getMailserverTopics" {}
   "mailservers_deleteMailserverTopic" {}
   "mailservers_addChatRequestRange" {}
   "mailservers_getChatRequestRanges" {}
   "mailservers_deleteChatRequestRange" {}})

(defn call
  [{:keys [method params on-success on-error] :as p}]
  (if-let [method-options (json-rpc-api method)]
    (let [{:keys [id on-result subscription?]
           :or {on-result identity
                id        1
                params    []}} method-options
          on-error (or on-error
                       #(log/warn :json-rpc/error method :error % :params params))]
      (if (nil? method)
        (log/error :json-rpc/method-not-found method)
        (status/call-private-rpc
         (types/clj->json {:jsonrpc "2.0"
                           :id      id
                           :method  (if subscription?
                                      "eth_subscribeSignal"
                                      method)
                           :params  (if subscription?
                                      [method params]
                                      params)})
         (fn [response]
           (if (string/blank? response)
             (on-error {:message "Blank response"})
             (let [{:keys [error result] :as response2} (types/json->clj response)]
               (if error
                 (on-error error)
                 (if subscription?
                   (re-frame/dispatch
                    [:ethereum.callback/subscription-success
                     result on-success])
                   (on-success (on-result result))))))))))

    (log/warn "method" method "not found" p)))

(defn eth-call
  [{:keys [contract method params outputs on-success on-error block]
    :or {block "latest"
         params []}}]
  (call {:method "eth_call"
         :params [{:to contract
                   :data (abi-spec/encode method params)}
                  (if (int? block)
                    (abi-spec/number-to-hex block)
                    block)]
         :on-success
         (if outputs
           #(on-success (abi-spec/decode % outputs))
           on-success)
         :on-error
         on-error}))

;; effects
(re-frame/reg-fx
 ::call
 (fn [params]
   (doseq [param params]
     (call param))))

(re-frame/reg-fx
 ::eth-call
 (fn [params]
   (doseq [param params]
     (eth-call param))))

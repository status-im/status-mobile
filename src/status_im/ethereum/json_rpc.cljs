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
   "shhext_enableInstallation" {}
   "shhext_disableInstallation" {}
   "shhext_getOurInstallations" {}
   "shhext_setInstallationMetadata" {}
   "shhext_loadFilter" {}
   "shhext_removeFilters" {}
   "shhext_chats" {}
   "shhext_saveChat" {}
   "shhext_contacts" {}
   "shhext_deleteChat" {}
   "shhext_saveContact" {}
   "status_joinPublicChat" {}
   "status_chats" {}
   "status_startOneOnOneChat" {}
   "status_removeChat" {}
   "wallet_getTransfers" {}
   "browsers_getBrowsers" {}
   "browsers_addBrowser" {}
   "browsers_deleteBrowser" {}
   "permissions_addDappPermissions" {}
   "permissions_getDappPermissions" {}
   "permissions_deleteDappPermissions" {}})

(defn call
  [{:keys [method params on-success on-error]}]
  (when-let [method-options (json-rpc-api method)]
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
                   (on-success (on-result result))))))))))))

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

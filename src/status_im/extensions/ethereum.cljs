(ns status-im.extensions.ethereum
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.hex :as hex]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
 :extensions/transaction-on-result
 (fn [cofx [_ on-result id result method]]
   (fx/merge cofx
             (when on-result
               {:dispatch (on-result {:error nil :result result})})
             (navigation/navigate-to-clean :wallet-transaction-sent nil))))

(handlers/register-handler-fx
 :extensions/transaction-on-error
 (fn [{db :db} [_ on-result message]]
   (when on-result {:dispatch (on-result {:error message :result nil})})))

;; EXTENSION TRANSACTION -> SEND TRANSACTION
(defn  prepare-extension-transaction [params contacts on-result]
  (let [{:keys [to value data gas gasPrice nonce]} params
        contact (get contacts (hex/normalize-hex to))]
    (cond-> {:id               "extension-id"
             :to-name          (or (when (nil? to)
                                     (i18n/label :t/new-contract))
                                   contact)
             :symbol           :ETH
             :method           constants/web3-send-transaction
             :to               to
             :amount           (money/bignumber (or value 0))
             :gas              (cond
                                 gas
                                 (money/bignumber gas)
                                 (and value (empty? data))
                                 (money/bignumber 21000))
             :gas-price        (when gasPrice
                                 (money/bignumber gasPrice))
             :data             data
             :on-result        [:extensions/transaction-on-result on-result]
             :on-error         [:extensions/transaction-on-error on-result]}
      nonce
      (assoc :nonce nonce))))

(handlers/register-handler-fx
 :extensions/ethereum-send-transaction
 (fn [{db :db} [_ _ {:keys [method params on-result] :as arguments}]]
   (let [tx-object (assoc (select-keys arguments [:to :gas :gas-price :value :nonce])
                          :data (when (and method params) (abi-spec/encode method params)))
         transaction (prepare-extension-transaction tx-object (:contacts/contacts db) on-result)]
     (models.wallet/open-modal-wallet-for-transaction db transaction tx-object))))

(handlers/register-handler-fx
 :extensions/ethereum-call
 (fn [_ [_ _ {:keys [to method params outputs on-result]}]]
   (let [tx-object {:to to :data (when method (abi-spec/encode method params))}]
     {:browser/call-rpc [{"jsonrpc" "2.0"
                          "method"  "eth_call"
                          "params"  [tx-object "latest"]}
                         #(let [result-str (when %2
                                             (get (js->clj %2) "result"))
                                result     (cond
                                             (= "0x" result-str) nil
                                             (and outputs result-str)
                                             (abi-spec/decode (string/replace result-str #"0x" "")  outputs)
                                             :else result-str)]
                            (re-frame/dispatch (on-result (merge {:result result} (when %1 {:error %1})))))]})))

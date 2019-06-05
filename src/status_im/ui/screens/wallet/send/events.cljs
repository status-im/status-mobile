(ns status-im.ui.screens.wallet.send.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.wallet.db :as wallet.db]
            [status-im.ethereum.tokens :as tokens]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.core :as ethereum]
            [status-im.signing.core :as signing]))

(defn set-and-validate-amount-db [db amount symbol decimals]
  (let [{:keys [value error]} (wallet.db/parse-amount amount decimals)]
    (-> db
        (assoc-in [:wallet :send-transaction :amount] (money/formatted->internal value symbol decimals))
        (assoc-in [:wallet :send-transaction :amount-text] amount)
        (assoc-in [:wallet :send-transaction :amount-error] error))))

(handlers/register-handler-fx
 :wallet.send/set-and-validate-amount
 (fn [{:keys [db]} [_ amount symbol decimals]]
   {:db (set-and-validate-amount-db db amount symbol decimals)}))

(handlers/register-handler-fx
 :wallet.send/set-symbol
 (fn [{:keys [db]} [_ symbol]]
   {:db (-> db
            (assoc-in [:wallet :send-transaction :symbol] symbol)
            (assoc-in [:wallet :send-transaction :amount] nil)
            (assoc-in [:wallet :send-transaction :amount-text] nil)
            (assoc-in [:wallet :send-transaction :asset-error] nil))}))

(handlers/register-handler-fx
 :wallet.ui/sign-transaction-button-clicked
 (fn [{:keys [db] :as cofx} _]
   (let [{:keys [to symbol amount]} (get-in cofx [:db :wallet :send-transaction])
         {:keys [symbol address]} (tokens/asset-for (:wallet/all-tokens db) (keyword (:chain db)) symbol)
         amount-hex (str "0x" (abi-spec/number-to-hex amount))
         to-norm (ethereum/normalized-address to)]
     (signing/sign cofx {:tx-obj    (if (= symbol :ETH)
                                      {:to   to-norm
                                       :value amount-hex}
                                      {:to   (ethereum/normalized-address address)
                                       :data (abi-spec/encode "transfer(address,uint256)" [to-norm amount-hex])})
                         :on-result [:navigate-back]}))))
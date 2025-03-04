(ns status-im.contexts.wallet.trading.swap.routes
  (:require [utils.money :as money]
            [utils.number :as number]))

(defn approval-data
  [route]
  (let [to-bn (fn [k] (-> route k money/from-hex))]
    {:required?        (:ApprovalRequired route)
     :nonce            (:ApprovalTxNonce route)
     :contract-address (:ApprovalContractAddress route)
     :estimated-time   (to-bn :ApprovalEstimatedTime)
     :amount-required  (to-bn :ApprovalAmountRequired)}))

(defn amount-out
  [route]
  (let [bn-amount      (-> route :AmountOut money/from-hex)
        token-decimals (-> route :ToToken :decimals)]
    {:decimals token-decimals
     :bn       bn-amount
     :whole    (number/convert-to-whole-number bn-amount token-decimals)}))

(defn amount-in
  [route]
  (let [bn-amount      (-> route :AmountIn money/from-hex)
        token-decimals (-> route :FromToken money/bignumber)]
    {:decimals token-decimals
     :bn       bn-amount
     :whole    (number/convert-to-whole-number bn-amount token-decimals)}))

(defn fees
  [route]
  (let [to-bn (fn [k] (-> route k money/from-hex))]
    {:tx-total-fee (to-bn :TxTotalFee)
     :approval-fee (to-bn :ApprovalFee)}))

(defn estimated-time
  [route]
  (:TxEstimatedTime route))

(defn processor-name
  [route]
  (get route :ProcessorName))

(defn find-pending-approval-transaction
  [transactions]
  (println transactions)
  (->> (vals transactions)
       (some (fn [{:keys [status transaction]}]
               (when (and (:approvalTx transaction)
                          (= :pending status))
                 transaction)))))

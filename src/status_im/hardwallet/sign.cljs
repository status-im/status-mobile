(ns status-im.hardwallet.sign
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]))

(fx/defn sign
  {:events [:hardwallet/sign]}
  [{:keys [db] :as cofx}]
  (let [card-connected?                   (get-in db [:hardwallet :card-connected?])
        pairing                           (common/get-pairing db)
        multiaccount-keycard-instance-uid (get-in db [:multiaccount :keycard-instance-uid])
        instance-uid                      (get-in db [:hardwallet :application-info :instance-uid])
        keycard-match?                    (= multiaccount-keycard-instance-uid instance-uid)
        hash                              (get-in db [:hardwallet :hash])
        pin                               (common/vector->string (get-in db [:hardwallet :pin :sign]))]
    (if (and card-connected?
             keycard-match?)
      {:db              (-> db
                            (assoc-in [:hardwallet :card-read-in-progress?] true)
                            (assoc-in [:hardwallet :pin :status] :verifying))
       :hardwallet/sign {:hash    (ethereum/naked-address hash)
                         :pairing pairing
                         :pin     pin}}
      (fx/merge cofx
                {:db (assoc-in db [:signing/sign :keycard-step] :signing)}
                (common/set-on-card-connected :hardwallet/sign)
                (when-not keycard-match?
                  (common/show-wrong-keycard-alert card-connected?))))))

(fx/defn prepare-to-sign
  {:events [:hardwallet/prepare-to-sign]}
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pairing         (common/get-pairing db)]
    (fx/merge cofx
              (if card-connected?
                (common/get-application-info pairing :hardwallet/sign)
                (fn [cofx]
                  (fx/merge cofx
                            (common/set-on-card-connected :hardwallet/prepare-to-sign)
                            (common/show-pair-sheet {})))))))

(fx/defn sign-message-completed
  [_ signature]
  (let [signature' (-> signature
                                        ; add 27 to last byte
                                        ; https://github.com/ethereum/go-ethereum/blob/master/internal/ethapi/api.go#L431
                       (clojure.string/replace-first #"00$", "1b")
                       (clojure.string/replace-first #"01$", "1c")
                       (ethereum/normalized-hex))]
    {:dispatch
     [:signing/sign-message-completed (types/clj->json {:result signature'})]}))

(fx/defn send-transaction-with-signature
  [_ data]
  {:send-transaction-with-signature data})

(fx/defn on-sign-success
  {:events [:hardwallet.callback/on-sign-success]}
  [{:keys [db] :as cofx} signature]
  (log/debug "[hardwallet] sign success: " signature)
  (let [transaction (get-in db [:hardwallet :transaction])
        tx-obj      (select-keys transaction [:from :to :value :gas :gasPrice])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :pin :sign] [])
                       (assoc-in [:hardwallet :pin :status] nil)
                       (assoc-in [:hardwallet :hash] nil)
                       (assoc-in [:hardwallet :transaction] nil))}
              (common/clear-on-card-connected)
              (common/get-application-info (common/get-pairing db) nil)
              (common/hide-pair-sheet)
              (if transaction
                (send-transaction-with-signature {:transaction  (types/clj->json transaction)
                                                  :signature    signature
                                                  :on-completed #(re-frame/dispatch [:signing/transaction-completed % tx-obj])})
                (sign-message-completed signature)))))

(fx/defn on-sign-error
  {:events [:hardwallet.callback/on-sign-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] sign error: " error)
  (let [tag-was-lost? (common/tag-lost? (:error error))]
    (when-not tag-was-lost?
      (if (re-matches common/pin-mismatch-error (:error error))
        (fx/merge cofx
                  {:db (-> db
                           (update-in [:hardwallet :pin] merge {:status      :error
                                                                :sign        []
                                                                :error-label :t/pin-mismatch})
                           (assoc-in [:signing/sign :keycard-step] :pin))}
                  (common/hide-pair-sheet)
                  (common/get-application-info (common/get-pairing db) nil))
        (fx/merge cofx
                  (common/hide-pair-sheet)
                  (common/show-wrong-keycard-alert true))))))

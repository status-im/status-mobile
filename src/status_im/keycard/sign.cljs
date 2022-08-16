(ns status-im.keycard.sign
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]
            [clojure.string :as clojure.string]))

(fx/defn sign
  {:events [:keycard/sign]}
  [{:keys [db] :as cofx} hash on-success]
  (let [card-connected?      (get-in db [:keycard :card-connected?])
        key-uid              (get-in db [:multiaccount :key-uid])
        keycard-key-uid      (get-in db [:keycard :application-info :key-uid])
        keycard-pin-retries  (get-in db [:keycard :application-info :pin-retry-counter])
        keycard-match?       (= key-uid keycard-key-uid)
        hash                 (or hash (get-in db [:keycard :hash]))
        data                 (get-in db [:keycard :data])
        typed?               (get-in db [:keycard :typed?])
        pin                  (common/vector->string (get-in db [:keycard :pin :sign]))
        from                 (or (get-in db [:signing/tx :from :address]) (get-in db [:signing/tx :message :from]) (ethereum/default-address db))
        path                 (reduce
                              (fn [_ {:keys [address path]}]
                                (when (ethereum/address= from address)
                                  (reduced path)))
                              nil
                              (:multiaccount/accounts db))]
    (cond
      (not keycard-match?)
      (common/show-wrong-keycard-alert cofx)

      (not card-connected?)
      (fx/merge cofx
                {:db (assoc-in db [:signing/sign :keycard-step] :signing)}
                (common/set-on-card-connected :keycard/sign))

      (pos? keycard-pin-retries) ; if 0, get-application-info will have already closed the connection sheet and opened the frozen card popup
      {:db              (-> db
                            (assoc-in [:keycard :card-read-in-progress?] true)
                            (assoc-in [:keycard :pin :status] :verifying))
       :keycard/sign {:hash       (ethereum/naked-address hash)
                      :data       data
                      :typed?     typed? ; this parameter is for e2e
                      :on-success on-success
                      :pin        pin
                      :path       path}})))

(defn normalize-signature [signature]
  (-> signature
      (clojure.string/replace-first #"00$", "1b")
      (clojure.string/replace-first #"01$", "1c")
      ethereum/normalized-hex))

(fx/defn sign-message
  {:events [:keycard/sign-message]}
  [cofx params result]
  (let [{:keys [result error]} (types/json->clj result)
        on-success #(re-frame/dispatch [:keycard/on-sign-message-success params
                                        (normalize-signature %)])
        hash (ethereum/naked-address result)]
    (sign cofx hash on-success)))

(fx/defn on-sign-message-success
  {:events [:keycard/on-sign-message-success]}
  [{:keys [db] :as cofx} {:keys [tx-hash message-id chat-id value contract]} signature]
  (fx/merge
   cofx
   {:dispatch
    (if message-id
      [:sign/send-accept-transaction-message message-id tx-hash signature]
      [:sign/send-transaction-message chat-id value contract tx-hash signature])
    :signing/show-transaction-result nil
    :db (-> db
            (assoc-in [:keycard :pin :sign] [])
            (assoc-in [:keycard :pin :status] nil))}
   (common/clear-on-card-connected)
   (common/get-application-info nil)
   (common/hide-connection-sheet)))

(fx/defn sign-typed-data
  {:events [:keycard/sign-typed-data]}
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:keycard :card-connected?])
        hash (get-in db [:keycard :hash])]
    (if card-connected?
      {:db                      (-> db
                                    (assoc-in [:keycard :card-read-in-progress?] true)
                                    (assoc-in [:signing/sign :keycard-step] :signing))
       :keycard/sign-typed-data {:hash (ethereum/naked-address hash)}}
      (fx/merge cofx
                (common/set-on-card-connected :keycard/sign-typed-data)
                {:db (assoc-in db [:signing/sign :keycard-step] :signing)}))))

(fx/defn fetch-currency-token-on-success
  {:events [:keycard/fetch-currency-token-on-success]}
  [{:keys [db]} {:keys [decimals symbol]}]
  {:db (-> db
           (assoc-in [:signing/sign :formatted-data :message :formatted-currency] symbol)
           (update-in [:signing/sign :formatted-data :message]
                      #(assoc % :formatted-amount (.dividedBy ^js (money/bignumber (:amount %))
                                                              (money/bignumber (money/from-decimal decimals))))))})

(fx/defn store-hash-and-sign-typed
  {:events [:keycard/store-hash-and-sign-typed]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [result]} (types/json->clj result)
        message (get-in db [:signing/sign :formatted-data :message])
        currency-contract (:currency message)]
    (when currency-contract
      {::json-rpc/call [{:method "wallet_discoverToken"
                         :params [(ethereum/chain-id db) currency-contract]
                         :on-success #(re-frame/dispatch [:keycard/fetch-currency-token-on-success %])}]})
    (fx/merge cofx
              {:db (assoc-in db [:keycard :hash] result)}
              sign-typed-data)))

(fx/defn prepare-to-sign
  {:events [:keycard/prepare-to-sign]}
  [{:keys [db] :as cofx}]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/prepare-to-sign
    :handler           (common/get-application-info :keycard/sign)}))

(fx/defn sign-message-completed
  [_ signature]
  {:dispatch
   [:signing/sign-message-completed signature]})

(fx/defn send-transaction-with-signature
  [_ data]
  {:send-transaction-with-signature data})

(fx/defn on-sign-success
  {:events [:keycard.callback/on-sign-success]}
  [{:keys [db] :as cofx} signature]
  (log/debug "[keycard] sign success: " signature)
  (let [signature-json (types/clj->json {:result (normalize-signature signature)})
        transaction    (get-in db [:keycard :transaction])
        tx-obj         (select-keys transaction [:from :to :value :gas :gasPrice :command? :chat-id :message-id])
        command?       (:command? transaction)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :hash] nil)
                       (assoc-in [:keycard :transaction] nil))}
              (when-not command?
                (fn [{:keys [db] :as cofx}]
                  (fx/merge
                   cofx
                   {:db (-> db
                            (assoc-in [:keycard :pin :sign] [])
                            (assoc-in [:keycard :pin :status] nil))}
                   (common/clear-on-card-connected)
                   (common/get-application-info nil)
                   (common/hide-connection-sheet))))
              (if transaction
                (send-transaction-with-signature
                 {:transaction  (types/clj->json transaction)
                  :signature    signature
                  :on-completed #(re-frame/dispatch [:signing/transaction-completed % tx-obj])})
                (sign-message-completed signature-json)))))

(fx/defn on-sign-error
  {:events [:keycard.callback/on-sign-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] sign error: " error)
  (let [tag-was-lost? (common/tag-lost? (:error error))
        pin-retries (common/pin-retries (:error error))]
    (when-not tag-was-lost?
      (if (not (nil? pin-retries))
        (fx/merge cofx
                  {:db (-> db
                           (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries)
                           (update-in [:keycard :pin] assoc
                                      :status      :error
                                      :sign        []
                                      :error-label :t/pin-mismatch)
                           (assoc-in [:signing/sign :keycard-step] :pin))}
                  (common/hide-connection-sheet)
                  (when (zero? pin-retries) (common/frozen-keycard-popup)))

        (fx/merge cofx
                  (common/hide-connection-sheet)
                  (common/show-wrong-keycard-alert))))))

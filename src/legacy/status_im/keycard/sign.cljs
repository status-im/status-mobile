(ns legacy.status-im.keycard.sign
  (:require
    [clojure.string :as string]
    [legacy.status-im.keycard.common :as common]
    [legacy.status-im.utils.deprecated-types :as types]
    [legacy.status-im.wallet.utils :as wallet.utils]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.ethereum.chain :as chain]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(rf/defn sign
  {:events [:keycard/sign]}
  [{:keys [db] :as cofx} hash on-success]
  (let [card-connected?     (get-in db [:keycard :card-connected?])
        key-uid             (get-in db [:profile/profile :key-uid])
        keycard-key-uid     (get-in db [:keycard :application-info :key-uid])
        keycard-pin-retries (get-in db [:keycard :application-info :pin-retry-counter])
        keycard-match?      (= key-uid keycard-key-uid)
        hash                (or hash (get-in db [:keycard :hash]))
        data                (get-in db [:keycard :data])
        typed?              (get-in db [:keycard :typed?])
        pin                 (common/vector->string (get-in db [:keycard :pin :sign]))
        from                (or (get-in db [:signing/tx :from :address])
                                (get-in db [:signing/tx :message :from])
                                (wallet.utils/default-address db))
        path                (reduce
                             (fn [_ {:keys [address path]}]
                               (when (address/address= from address)
                                 (reduced path)))
                             nil
                             (:profile/wallet-accounts db))]
    (cond
      (not keycard-match?)
      (common/show-wrong-keycard-alert cofx)

      (not card-connected?)
      (rf/merge cofx
                {:db (assoc-in db [:signing/sign :keycard-step] :signing)}
                (common/set-on-card-connected :keycard/sign))

      (pos? keycard-pin-retries) ; if 0, get-application-info will have already closed the
                                 ; connection sheet and opened the frozen card popup
      {:db           (-> db
                         (assoc-in [:keycard :card-read-in-progress?] true)
                         (assoc-in [:keycard :pin :status] :verifying))
       :keycard/sign {:hash       (address/naked-address hash)
                      :data       data
                      :typed?     typed? ; this parameter is for e2e
                      :on-success on-success
                      :pin        pin
                      :path       path}})))

(defn normalize-signature
  [signature]
  (-> signature
      (string/replace-first #"00$" "1b")
      (string/replace-first #"01$" "1c")
      address/normalized-hex))

(rf/defn sign-message
  {:events [:keycard/sign-message]}
  [cofx params result]
  (let [{:keys [result error]} (types/json->clj result)
        on-success             #(re-frame/dispatch [:keycard/on-sign-message-success params
                                                    (normalize-signature %)])
        hash                   (address/naked-address result)]
    (sign cofx hash on-success)))

(rf/defn on-sign-message-success
  {:events [:keycard/on-sign-message-success]}
  [{:keys [db] :as cofx} {:keys [tx-hash message-id chat-id value contract]} signature]
  (rf/merge
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

(rf/defn sign-typed-data
  {:events [:keycard/sign-typed-data]}
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:keycard :card-connected?])
        hash            (get-in db [:keycard :hash])]
    (if card-connected?
      {:db                      (-> db
                                    (assoc-in [:keycard :card-read-in-progress?] true)
                                    (assoc-in [:signing/sign :keycard-step] :signing))
       :keycard/sign-typed-data {:hash (address/naked-address hash)}}
      (rf/merge cofx
                (common/set-on-card-connected :keycard/sign-typed-data)
                {:db (assoc-in db [:signing/sign :keycard-step] :signing)}))))

(rf/defn fetch-currency-token-on-success
  {:events [:keycard/fetch-currency-token-on-success]}
  [{:keys [db]} {:keys [decimals symbol]}]
  {:db (-> db
           (assoc-in [:signing/sign :formatted-data :message :formatted-currency] symbol)
           (update-in [:signing/sign :formatted-data :message]
                      #(assoc %
                              :formatted-amount
                              (.dividedBy ^js (money/bignumber (:amount %))
                                          (money/bignumber (money/from-decimal decimals))))))})

(rf/defn store-hash-and-sign-typed
  {:events [:keycard/store-hash-and-sign-typed]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [result]}  (types/json->clj result)
        message           (get-in db [:signing/sign :formatted-data :message])
        currency-contract (:currency message)]
    (when currency-contract
      {:json-rpc/call [{:method     "wallet_discoverToken"
                        :params     [(chain/chain-id db) currency-contract]
                        :on-success #(re-frame/dispatch [:keycard/fetch-currency-token-on-success
                                                         %])}]})
    (rf/merge cofx
              {:db (assoc-in db [:keycard :hash] result)}
              sign-typed-data)))

(rf/defn prepare-to-sign
  {:events [:keycard/prepare-to-sign]}
  [{:keys [db] :as cofx}]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/prepare-to-sign
    :handler           (common/get-application-info :keycard/sign)}))

(rf/defn sign-message-completed
  [_ signature]
  {:dispatch
   [:signing/sign-message-completed signature]})

(rf/defn send-transaction-with-signature
  [_ data]
  {:send-transaction-with-signature data})

(rf/defn on-sign-success
  {:events [:keycard.callback/on-sign-success]}
  [{:keys [db] :as cofx} signature]
  (log/debug "[keycard] sign success: " signature)
  (let [signature-json (types/clj->json {:result (normalize-signature signature)})
        transaction    (get-in db [:keycard :transaction])
        tx-obj         (select-keys transaction
                                    [:from :to :value :gas :gasPrice :command? :chat-id :message-id])
        command?       (:command? transaction)]
    (rf/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :hash] nil)
                       (assoc-in [:keycard :transaction] nil))}
              (when-not command?
                (fn [{:keys [db] :as cofx}]
                  (rf/merge
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

(rf/defn on-sign-error
  {:events [:keycard.callback/on-sign-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] sign error: " error)
  (let [tag-was-lost? (common/tag-lost? (:error error))
        pin-retries   (common/pin-retries (:error error))]
    (when-not tag-was-lost?
      (if (not (nil? pin-retries))
        (rf/merge cofx
                  {:db (-> db
                           (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries)
                           (update-in [:keycard :pin]
                                      assoc
                                      :status      :error
                                      :sign        []
                                      :error-label :t/pin-mismatch)
                           (assoc-in [:signing/sign :keycard-step] :pin))}
                  (common/hide-connection-sheet)
                  (when (zero? pin-retries) (common/frozen-keycard-popup)))

        (rf/merge cofx
                  (common/hide-connection-sheet)
                  (common/show-wrong-keycard-alert))))))

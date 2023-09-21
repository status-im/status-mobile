(ns status-im.signing.core
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im.ethereum.core :as ethereum]
    [status-im.ethereum.eip55 :as eip55]
    [status-im.ethereum.tokens :as tokens]
    [utils.i18n :as i18n]
    [status-im.keycard.card :as keycard.card]
    [status-im.keycard.common :as keycard.common]
    [native-module.core :as native-module]
    [status-im.signing.eip1559 :as eip1559]
    [status-im.signing.keycard :as signing.keycard]
    [utils.re-frame :as rf]
    [status-im.utils.hex :as utils.hex]
    [utils.money :as money]
    [status-im.utils.types :as types]
    [status-im.utils.utils :as utils]
    [status-im.wallet.core :as wallet]
    [status-im.wallet.prices :as prices]
    [status-im2.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]
    [utils.security.core :as security]))

(re-frame/reg-fx
 :signing/send-transaction-fx
 (fn [{:keys [tx-obj hashed-password cb]}]
   (native-module/send-transaction (types/clj->json tx-obj)
                                   hashed-password
                                   cb)))

(re-frame/reg-fx
 :signing/show-transaction-error
 (fn [message]
   (utils/show-popup (i18n/label :t/transaction-failed) message)))

(re-frame/reg-fx
 :signing/show-transaction-result
 (fn []
   (utils/show-popup (i18n/label :t/transaction-sent) (i18n/label :t/transaction-description))))

(re-frame/reg-fx
 :signing.fx/sign-message
 (fn [{:keys [params on-completed]}]
   (native-module/sign-message (types/clj->json params)
                               on-completed)))

(re-frame/reg-fx
 :signing.fx/recover-message
 (fn [{:keys [params on-completed]}]
   (native-module/recover-message (types/clj->json params)
                                  on-completed)))

(re-frame/reg-fx
 :signing.fx/sign-typed-data
 (fn [{:keys [v4 data account on-completed hashed-password]}]
   (if v4
     (native-module/sign-typed-data-v4 data account hashed-password on-completed)
     (native-module/sign-typed-data data account hashed-password on-completed))))

(defn get-contact
  [db to]
  (let [to (utils.hex/normalize-hex to)]
    (or
     (get-in db [:contacts/contacts to])
     {:address (ethereum/normalized-hex to)})))

(rf/defn change-password
  {:events [:signing.ui/password-is-changed]}
  [{db :db} password]
  (let [unmasked-pass (security/safe-unmask-data password)]
    {:db (update db
                 :signing/sign assoc
                 :password     password
                 :error        nil
                 :enabled?     (and unmasked-pass (> (count unmasked-pass) 5)))}))

(rf/defn sign-message
  [{{:signing/keys [sign tx] :as db} :db}]
  (let [{{:keys [data typed? from v4]} :message} tx
        {:keys [in-progress? password]}          sign
        from                                     (or from (ethereum/default-address db))
        hashed-password                          (ethereum/sha3 (security/safe-unmask-data password))]
    (when-not in-progress?
      (merge
       {:db (update db :signing/sign assoc :error nil :in-progress? true)}
       (if typed?
         {:signing.fx/sign-typed-data {:v4              v4
                                       :data            data
                                       :account         from
                                       :hashed-password hashed-password
                                       :on-completed    #(re-frame/dispatch
                                                          [:signing/sign-message-completed %])}}
         {:signing.fx/sign-message {:params       {:data     data
                                                   :password hashed-password
                                                   :account  from}
                                    :on-completed #(re-frame/dispatch [:signing/sign-message-completed
                                                                       %])}})))))

(rf/defn send-transaction
  {:events [:signing.ui/sign-is-pressed]}
  [{{:signing/keys [sign tx] :ens/keys [registration] :as db} :db :as cofx}]
  (let [{:keys [in-progress? password]}          sign
        {:keys [tx-obj gas gasPrice maxPriorityFeePerGas
                maxFeePerGas message nonce]}     tx
        hashed-password                          (ethereum/sha3 (security/safe-unmask-data password))
        {:keys [action username custom-domain?]} registration
        {:keys [public-key]}                     (:profile/profile db)
        chain-id                                 (ethereum/chain-id db)]
    (if message
      (sign-message cofx)
      (let [tx-obj-to-send  (merge tx-obj
                                   (when gas
                                     {:gas (str "0x" (native-module/number-to-hex gas))})
                                   (when gasPrice
                                     {:gasPrice (str "0x" (native-module/number-to-hex gasPrice))})
                                   (when nonce
                                     {:nonce (str "0x" (native-module/number-to-hex nonce))})
                                   (when maxPriorityFeePerGas
                                     {:maxPriorityFeePerGas (str "0x"
                                                                 (native-module/number-to-hex
                                                                  (js/parseInt maxPriorityFeePerGas)))})
                                   (when maxFeePerGas
                                     {:maxFeePerGas (str "0x"
                                                         (native-module/number-to-hex
                                                          (js/parseInt maxFeePerGas)))}))
            cb              #(re-frame/dispatch
                              [:signing/transaction-completed %
                               tx-obj-to-send hashed-password])
            watch-ens-tx-fn #(re-frame/dispatch
                              [:transactions/watch-transaction
                               %
                               {:trigger-fn
                                (fn [_ {:keys [hash type]}]
                                  (and (= hash %)
                                       (contains? #{:outbound :failed} type)))
                                :on-trigger
                                (fn [{:keys [type]}]
                                  (case type
                                    :outbound (do (rf/dispatch [:ens/clear-registration %])
                                                  (rf/dispatch [:ens/save-username custom-domain?
                                                                username false]))

                                    :failed   (rf/dispatch [:ens/update-ens-tx-state :failure username
                                                            custom-domain? %])
                                    nil))}])]
        (when-not in-progress?
          (cond-> {:db (update db :signing/sign assoc :error nil :in-progress? true)}
            (nil? action)                                 (assoc :signing/send-transaction-fx
                                                                 {:tx-obj          tx-obj-to-send
                                                                  :hashed-password hashed-password
                                                                  :cb              cb})
            (= action constants/ens-action-type-register) (assoc :json-rpc/call
                                                                 [{:method "ens_register"
                                                                   :params [chain-id tx-obj-to-send
                                                                            hashed-password username
                                                                            public-key]
                                                                   :on-success
                                                                   #(do
                                                                      (cb (types/clj->json
                                                                           {:result %}))
                                                                      (watch-ens-tx-fn %))
                                                                   :on-error #(cb (types/clj->json
                                                                                   {:error %}))}])
            (= action
               constants/ens-action-type-set-pub-key)     (assoc :json-rpc/call
                                                                 [{:method     "ens_setPubKey"
                                                                   :params     [chain-id tx-obj-to-send
                                                                                hashed-password username
                                                                                public-key]
                                                                   :on-success #(do (cb (types/clj->json
                                                                                         {:result %}))
                                                                                    (watch-ens-tx-fn %))
                                                                   :on-error   #(cb (types/clj->json
                                                                                     {:error
                                                                                      %}))}])))))))

(rf/defn prepare-unconfirmed-transaction
  [{:keys [db now]} new-tx-hash
   {:keys [value gasPrice maxFeePerGas maxPriorityFeePerGas gas data to from hash]} symbol amount]
  (let [token       (tokens/symbol->token (:wallet/all-tokens db) symbol)
        from        (eip55/address->checksum from)
        ;;if there is a hash in the tx object that means we resending transaction
        old-tx-hash hash
        gas-price   (money/to-fixed (money/bignumber gasPrice))
        gas-limit   (money/to-fixed (money/bignumber gas))
        tx          {:timestamp now
                     :to        to
                     :from      from
                     :type      :pending
                     :hash      new-tx-hash
                     :data      data
                     :token     token
                     :symbol    symbol
                     :value     (if token
                                  (money/to-fixed (money/unit->token amount (:decimals token)))
                                  (money/to-fixed (money/bignumber value)))
                     :gas-price gas-price
                     :fee-cap   maxFeePerGas
                     :tip-cap   maxPriorityFeePerGas
                     :gas-limit gas-limit}]
    (log/info "[signing] prepare-unconfirmed-transaction" tx)
    {:db (-> db
             ;;remove old transaction, because we replace it with the new one
             (update-in [:wallet :accounts from :transactions] dissoc old-tx-hash)
             (assoc-in [:wallet :accounts from :transactions new-tx-hash] tx))}))

(defn get-method-type
  [data]
  (cond
    (string/starts-with? data constants/method-id-transfer)
    :transfer
    (string/starts-with? data constants/method-id-approve)
    :approve
    (string/starts-with? data constants/method-id-approve-and-call)
    :approve-and-call))

(defn get-transfer-token
  [db to data]
  (let [{:keys [decimals] :as token} (tokens/address->token (:wallet/all-tokens db) to)]
    (when (and token data (string? data))
      (when-let [type (get-method-type data)]
        (let [[address value _] (native-module/decode-parameters
                                 (str "0x" (subs data 10))
                                 (if (= type :approve-and-call)
                                   ["address" "uint256" "bytes"]
                                   ["address" "uint256"]))]
          (when (and address value)
            {:to       address
             :contact  (get-contact db address)
             :contract to
             :approve? (not= type :transfer)
             :value    value
             :amount   (money/to-fixed (money/token->unit value decimals))
             :token    token
             :symbol   (:symbol token)}))))))

(defn parse-tx-obj
  [db {:keys [from to value data cancel?] :as tx}]
  (merge {:from    {:address from}
          :cancel? cancel?
          :hash    (:hash tx)}
         (if (nil? to)
           {:contact {:name (i18n/label :t/new-contract)}}
           (let [eth-value  (when value (money/bignumber value))
                 eth-amount (when eth-value (money/to-fixed (money/wei->ether eth-value)))
                 token      (get-transfer-token db to data)]
             (cond
               (and eth-amount (or (not (.equals ^js (money/bignumber 0) ^js eth-amount)) (nil? data)))
               {:to      to
                :contact (get-contact db to)
                :symbol  :ETH
                :value   value
                :amount  (str eth-amount)
                :token   (tokens/asset-for (:wallet/all-tokens db)
                                           (ethereum/get-current-network db)
                                           :ETH)}
               (not (nil? token))
               token
               :else
               {:to      to
                :contact {:address (ethereum/normalized-hex to)}})))))

(defn prepare-tx
  [db {{:keys [data gas gasPrice maxFeePerGas maxPriorityFeePerGas] :as tx-obj} :tx-obj :as tx}]
  (merge
   tx
   (parse-tx-obj db tx-obj)
   {:data                 data
    :gas                  (when gas (money/bignumber gas))
    :gasPrice             (when gasPrice (money/bignumber gasPrice))
    :maxFeePerGas         (when maxFeePerGas
                            (money/bignumber maxFeePerGas))
    :maxPriorityFeePerGas (when maxPriorityFeePerGas
                            (money/bignumber maxPriorityFeePerGas))}))

(rf/defn show-sign
  [{:keys [db] :as cofx}]
  (let [{:signing/keys [queue]} db
        {{:keys [gas gasPrice maxFeePerGas] :as tx-obj} :tx-obj
         {:keys [data typed? pinless?] :as message}     :message
         :as                                            tx}
        (last queue)
        keycard-multiaccount? (boolean (get-in db [:profile/profile :keycard-pairing]))
        wallet-set-up-passed? (get-in db [:profile/profile :wallet-set-up-passed?])]
    (if message
      (rf/merge
       cofx
       {:db                 (assoc db
                                   :signing/queue (drop-last queue)
                                   :signing/tx    tx
                                   :signing/sign  {:type           (cond pinless?              :pinless
                                                                         keycard-multiaccount? :keycard
                                                                         :else                 :password)
                                                   :formatted-data (if typed?
                                                                     (types/js->pretty-json
                                                                      (types/json->js data))
                                                                     (ethereum/hex->text data))
                                                   :keycard-step   (when pinless? :connect)})
        :show-signing-sheet nil}
       #(when-not wallet-set-up-passed?
          {:dispatch-later [{:dispatch [:show-popover {:view :signing-phrase}] :ms 200}]})
       (when pinless?
         (keycard.card/start-nfc {:on-success #(re-frame/dispatch [:keycard.callback/start-nfc-success])
                                  :on-failure #(re-frame/dispatch
                                                [:keycard.callback/start-nfc-failure])})
         (signing.keycard/hash-message
          {:data         data
           :typed?       true
           :on-completed #(re-frame/dispatch [:keycard/store-hash-and-sign-typed %])})))
      (rf/merge
       cofx
       {:db                 (assoc db
                                   :signing/queue (drop-last queue)
                                   :signing/tx    (prepare-tx db tx))
        :show-signing-sheet nil
        :dismiss-keyboard   nil}
       #(when-not wallet-set-up-passed?
          {:dispatch-later [{:dispatch [:show-popover {:view :signing-phrase}] :ms 200}]})
       (prices/update-prices)
       #(when-not gas
          {:db                           (assoc-in (:db %) [:signing/edit-fee :gas-loading?] true)
           :signing/update-estimated-gas {:obj           (-> tx-obj
                                                             (dissoc :gasPrice)
                                                             (update :maxFeePerGas
                                                                     (fn [fee]
                                                                       (some-> fee
                                                                               money/bignumber
                                                                               (money/mul 2)
                                                                               money/to-hex))))
                                          :success-event :signing/update-estimated-gas-success
                                          :error-event   :signing/update-estimated-gas-error}})
       (fn [cofx]
         {:db (assoc-in (:db cofx) [:signing/edit-fee :gas-price-loading?] true)
          :signing/update-gas-price
          {:success-callback #(re-frame/dispatch
                               [:wallet.send/update-gas-price-success :signing/tx % tx-obj])
           :error-callback   #(re-frame/dispatch [:signing/update-gas-price-error %])
           :network-id       (get-in (ethereum/current-network db)
                                     [:config :NetworkId])}})))))

(rf/defn check-queue
  [{:keys [db] :as cofx}]
  (let [{:signing/keys [tx queue]} db]
    (when (and (not tx) (seq queue))
      (show-sign cofx))))

(rf/defn send-transaction-message
  {:events [:sign/send-transaction-message]}
  [cofx chat-id value contract transaction-hash signature]
  {:json-rpc/call [{:method "wakuext_sendTransaction"
                    ;; We make sure `value` is serialized as string, and not as an integer or
                    ;; big-int
                    :params [chat-id (str value) contract transaction-hash
                             (or (:result (types/json->clj signature))
                                 (ethereum/normalized-hex signature))]
                    :js-response true
                    :on-success
                    #(re-frame/dispatch [:transport/message-sent %])}]})

(rf/defn send-accept-request-transaction-message
  {:events [:sign/send-accept-transaction-message]}
  [cofx message-id transaction-hash signature]
  {:json-rpc/call [{:method "wakuext_acceptRequestTransaction"
                    :params [transaction-hash message-id
                             (or (:result (types/json->clj signature))
                                 (ethereum/normalized-hex signature))]
                    :js-response true
                    :on-success
                    #(re-frame/dispatch [:transport/message-sent %])}]})

(rf/defn transaction-result
  [{:keys [db] :as cofx} result tx-obj]
  (let [{:keys [on-result symbol amount from]} (get db :signing/tx)]
    (rf/merge cofx
              {:db                              (dissoc db :signing/tx :signing/sign)
               :signing/show-transaction-result nil}
              (prepare-unconfirmed-transaction result tx-obj symbol amount)
              (check-queue)
              (wallet/watch-tx (get from :address) result)
              #(when on-result
                 {:dispatch (conj on-result result)}))))

(rf/defn command-transaction-result
  [{:keys [db] :as cofx} transaction-hash hashed-password
   {:keys [message-id chat-id from] :as tx-obj}]
  (let [{:keys [on-result symbol amount contract value]} (get db :signing/tx)
        data                                             (str (get-in db [:profile/profile :public-key])
                                                              (subs transaction-hash 2))]
    (rf/merge
     cofx
     {:db (dissoc db :signing/tx :signing/sign)}
     (wallet/watch-tx (get from :address) transaction-hash)
     (if (keycard.common/keycard-multiaccount? db)
       (signing.keycard/hash-message
        {:data data
         :on-completed
         (fn [hash]
           (re-frame/dispatch
            [:keycard/sign-message
             {:tx-hash    transaction-hash
              :message-id message-id
              :chat-id    chat-id
              :value      value
              :contract   contract
              :data       data}
             hash]))})
       (fn [_]
         {:signing.fx/sign-message
          {:params {:data     data
                    :password hashed-password
                    :account  from}
           :on-completed
           (fn [res]
             (re-frame/dispatch
              (if message-id
                [:sign/send-accept-transaction-message message-id transaction-hash res]
                [:sign/send-transaction-message
                 chat-id value contract transaction-hash res])))}
          :signing/show-transaction-result nil}))
     (prepare-unconfirmed-transaction transaction-hash tx-obj symbol amount)
     (check-queue)
     #(when on-result
        {:dispatch (conj on-result transaction-hash)}))))

(rf/defn transaction-error
  [{:keys [db]} {:keys [code message]}]
  (let [on-error (get-in db [:signing/tx :on-error])]
    (if (= code constants/send-transaction-err-decrypt)
      ;;wrong password
      {:db (assoc-in db [:signing/sign :error] (i18n/label :t/wrong-password))}
      (merge {:db                             (dissoc db :signing/tx :signing/sign)
              :signing/show-transaction-error message}
             (when on-error
               {:dispatch (conj on-error message)})))))

(rf/defn dissoc-signing-db-entries-and-check-queue
  {:events [:signing/dissoc-entries-and-check-queue]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (dissoc db :signing/tx :signing/sign)}
            check-queue))

(rf/defn sign-message-completed
  {:events [:signing/sign-message-completed]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [result error]} (types/json->clj result)
        on-result              (get-in db [:signing/tx :on-result])]
    (if error
      {:db (update db
                   :signing/sign
                   assoc
                   :error (if (= 5 (:code error))
                            (i18n/label :t/wrong-password)
                            (:message error))
                   :in-progress? false)}
      (rf/merge cofx
                (when-not (= (-> db :signing/sign :type) :pinless)
                  (dissoc-signing-db-entries-and-check-queue))
                #(when (= (-> db :signing/sign :type) :pinless)
                   {:dispatch-later [{:ms       3000
                                      :dispatch [:signing/dissoc-entries-and-check-queue]}]})
                #(when on-result
                   {:dispatch (conj on-result result)})))))

(rf/defn transaction-completed
  {:events       [:signing/transaction-completed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [cofx response tx-obj hashed-password]
  (log/info "transaction-completed" "tx-obj" tx-obj "response" response)
  (let [cofx-in-progress-false (assoc-in cofx [:db :signing/sign :in-progress?] false)
        {:keys [result error]} (types/json->clj response)]
    (if error
      (transaction-error cofx-in-progress-false error)
      (if (:command? tx-obj)
        (command-transaction-result cofx-in-progress-false result hashed-password tx-obj)
        (transaction-result cofx-in-progress-false result tx-obj)))))

(rf/defn discard
  "Discrad transaction signing"
  {:events [:signing.ui/cancel-is-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [on-error]} (get-in db [:signing/tx])]
    (rf/merge cofx
              {:db                 (-> db
                                       (assoc-in [:keycard :pin :status] nil)
                                       (dissoc :signing/tx :signing/sign))
               :hide-signing-sheet nil}
              (check-queue)
              (keycard.common/hide-connection-sheet)
              (keycard.common/clear-pin)
              #(when on-error
                 {:dispatch (conj on-error "transaction was cancelled by user")}))))

(defn normalize-tx-obj
  [db tx]
  (update-in tx [:tx-obj :from] #(eip55/address->checksum (or % (ethereum/default-address db)))))

(rf/defn sign
  "Signing transaction or message, shows signing sheet
   tx
   {:tx-obj - transaction object to send https://github.com/ethereum/wiki/wiki/JavaScript-API#parameters-25
    :message {:address  :data  :typed? } - message data to sign
    :on-result - re-frame event vector
    :on-error - re-frame event vector}"
  {:events [:signing.ui/sign]}
  [{:keys [db] :as cofx} tx]
  (rf/merge cofx
            {:db (update db :signing/queue conj (normalize-tx-obj db tx))}
            (check-queue)))

(rf/defn sign-transaction-button-clicked-from-chat
  {:events [:wallet.ui/sign-transaction-button-clicked-from-chat]}
  [{:keys [db] :as cofx} {:keys [to amount from token]}]
  (let [{:keys [symbol address]} token
        amount-hex               (str "0x" (native-module/number-to-hex amount))
        to-norm                  (ethereum/normalized-hex (if (string? to) to (:address to)))
        from-address             (:address from)
        identity                 (:current-chat-id db)
        db                       (dissoc db :wallet/prepare-transaction :signing/edit-fee)]
    (if to-norm
      (rf/merge
       cofx
       {:db db}
       (sign {:tx-obj (if (= symbol :ETH)
                        {:to       to-norm
                         :from     from-address
                         :chat-id  identity
                         :command? true
                         :value    amount-hex}
                        {:to       (ethereum/normalized-hex address)
                         :from     from-address
                         :chat-id  identity
                         :command? true
                         :data     (native-module/encode-transfer to-norm amount-hex)})}))
      {:db db
       :json-rpc/call
       [{:method      "wakuext_requestAddressForTransaction"
         :params      [(:current-chat-id db)
                       from-address
                       amount
                       (when-not (= symbol :ETH)
                         address)]
         :js-response true
         :on-success  #(re-frame/dispatch [:transport/message-sent %])}]})))

(rf/defn sign-transaction-button-clicked-from-request
  {:events [:wallet.ui/sign-transaction-button-clicked-from-request]}
  [{:keys [db] :as cofx} {:keys [amount from token]}]
  (let [{:keys [request-parameters chat-id]} (:wallet/prepare-transaction db)
        {:keys [symbol address]}             token
        amount-hex                           (str "0x" (native-module/number-to-hex amount))
        to-norm                              (:address request-parameters)
        from-address                         (:address from)]
    (rf/merge cofx
              {:db (dissoc db :wallet/prepare-transaction :signing/edit-fee)}
              (fn [cofx]
                (sign
                 cofx
                 {:tx-obj (if (= symbol :ETH)
                            {:to         to-norm
                             :from       from-address
                             :message-id (:id request-parameters)
                             :chat-id    chat-id
                             :command?   true
                             :value      amount-hex}
                            {:to         (ethereum/normalized-hex address)
                             :from       from-address
                             :command?   true
                             :message-id (:id request-parameters)
                             :chat-id    chat-id
                             :data       (native-module/encode-transfer to-norm amount-hex)})})))))

(rf/defn sign-transaction-button-clicked
  {:events [:wallet.ui/sign-transaction-button-clicked]}
  [{:keys [db] :as cofx} {:keys [to amount from token gas gasPrice maxFeePerGas maxPriorityFeePerGas]}]
  (let [{:keys [symbol address]} token
        amount-hex               (str "0x" (native-module/number-to-hex amount))
        to-norm                  (ethereum/normalized-hex (if (string? to) to (:address to)))
        from-address             (:address from)]
    (rf/merge cofx
              {:db (dissoc db :wallet/prepare-transaction :signing/edit-fee)}
              (sign
               {:tx-obj (merge (if (eip1559/sync-enabled?)
                                 {:from                 from-address
                                  :gas                  gas
                                  ;; per eip1559
                                  :maxFeePerGas         maxFeePerGas
                                  :maxPriorityFeePerGas maxPriorityFeePerGas}
                                 {:from     from-address
                                  ;;gas and gasPrice from qr (eip681)
                                  :gas      gas
                                  :gasPrice gasPrice})
                               (if (= symbol :ETH)
                                 {:to    to-norm
                                  :value amount-hex}
                                 {:to   (ethereum/normalized-hex address)
                                  :data (native-module/encode-transfer to-norm amount-hex)}))}))))

(re-frame/reg-fx
 :signing/get-transaction-by-hash-fx
 (fn [[tx-hash handler]]
   (json-rpc/call
    {:method     "eth_getTransactionByHash"
     :params     [tx-hash]
     :on-success handler})))

(rf/defn cancel-transaction-pressed
  {:events [:signing.ui/cancel-transaction-pressed]}
  [_ hash]
  {:signing/get-transaction-by-hash-fx [hash #(re-frame/dispatch [:signing/cancel-transaction %])]})

(rf/defn increase-gas-pressed
  {:events [:signing.ui/increase-gas-pressed]}
  [_ hash]
  {:signing/get-transaction-by-hash-fx [hash #(re-frame/dispatch [:signing/increase-gas %])]})

(rf/defn cancel-transaction
  {:events [:signing/cancel-transaction]}
  [cofx {:keys [from nonce hash]}]
  (when (and from nonce hash)
    (sign cofx
          {:tx-obj {:from    from
                    :to      from
                    :nonce   nonce
                    :value   "0x0"
                    :cancel? true
                    :hash    hash}})))

(rf/defn increase-gas
  {:events [:signing/increase-gas]}
  [cofx {:keys [from nonce] :as tx}]
  (when (and from nonce)
    (sign cofx
          {:tx-obj (-> tx
                       (select-keys [:from :to :value :input :gas :nonce :hash])
                       (set/rename-keys {:input :data}))})))

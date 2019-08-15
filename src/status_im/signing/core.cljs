(ns status-im.signing.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            ;;don't remove, used in fx macro
            [status-im.utils.handlers :as handlers]))

(re-frame/reg-fx
 :signing/send-transaction-fx
 (fn [{:keys [tx-obj hashed-password cb]}]
   (status/send-transaction (types/clj->json tx-obj)
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
   (status/sign-message (types/clj->json params)
                        on-completed)))

(re-frame/reg-fx
 :signing.fx/sign-typed-data
 (fn [{:keys [data account on-completed hashed-password]}]
   (status/sign-typed-data data account hashed-password on-completed)))

(defn get-contact [db to]
  (let [to (utils.hex/normalize-hex to)]
    (or
     (get-in db [:contacts/contacts to])
     {:address (ethereum/normalized-address to)})))

(fx/defn change-password
  {:events [:signing.ui/password-is-changed]}
  [{db :db} password]
  (let [unmasked-pass (security/safe-unmask-data password)]
    {:db (update db :signing/sign assoc
                 :password password
                 :error    nil
                 :enabled? (and unmasked-pass (> (count unmasked-pass) 5)))}))

(fx/defn sign-message
  [{{:signing/keys [sign tx] :as db} :db}]
  (let [{{:keys [data typed?]} :message} tx
        {:keys [in-progress? password]} sign
        from (ethereum/default-address db)
        hashed-password (ethereum/sha3 (security/safe-unmask-data password))]
    (when-not in-progress?
      (merge
       {:db (update db :signing/sign assoc :error nil :in-progress? true)}
       (if typed?
         {:signing.fx/sign-typed-data {:data         data
                                       :account      from
                                       :hashed-password hashed-password
                                       :on-completed #(re-frame/dispatch [:signing/sign-message-completed %])}}
         {:signing.fx/sign-message {:params       {:data     data
                                                   :password hashed-password
                                                   :account  from}
                                    :on-completed #(re-frame/dispatch [:signing/sign-message-completed %])}})))))

(fx/defn send-transaction
  {:events [:signing.ui/sign-is-pressed]}
  [{{:signing/keys [sign tx] :as db} :db :as cofx}]
  (let [{:keys [in-progress? password]} sign
        {:keys [tx-obj gas gasPrice message]} tx]
    (if message
      (sign-message cofx)
      (let [tx-obj-to-send (merge tx-obj
                                  (when gas
                                    {:gas (str "0x" (abi-spec/number-to-hex gas))})
                                  (when gasPrice
                                    {:gasPrice (str "0x" (abi-spec/number-to-hex gasPrice))}))]
        (when-not in-progress?
          {:db                          (update db :signing/sign assoc :error nil :in-progress? true)
           :signing/send-transaction-fx {:tx-obj   tx-obj-to-send
                                         :hashed-password (ethereum/sha3 (security/safe-unmask-data password))
                                         :cb       #(re-frame/dispatch [:signing/transaction-completed % tx-obj-to-send])}})))))

(fx/defn prepare-unconfirmed-transaction
  [{:keys [db now]} hash {:keys [value gasPrice gas data to from]} symbol]
  (let [all-tokens (:wallet/all-tokens db)
        chain      (:chain db)
        token      (tokens/symbol->token all-tokens (keyword chain) symbol)]
    {:db (assoc-in db [:wallet :transactions hash]
                   {:timestamp (str now)
                    :to        to
                    :from      from
                    :type      :pending
                    :hash      hash
                    :data      data
                    :token     token
                    :symbol    symbol
                    :value     (money/to-fixed (money/bignumber value))
                    :gas-price (money/to-fixed (money/bignumber gasPrice))
                    :gas-limit (money/to-fixed (money/bignumber gas))})}))

(defn get-method-type [data]
  (cond
    (string/starts-with? data constants/method-id-transfer)
    :transfer
    (string/starts-with? data constants/method-id-approve)
    :approve
    (string/starts-with? data constants/method-id-approve-and-call)
    :approve-and-call))

(defn get-transfer-token [db to data]
  (let [{:keys [symbol decimals] :as token} (tokens/address->token (:wallet/all-tokens db) (ethereum/chain-keyword db) to)]
    (when (and token data (string? data))
      (when-let [type (get-method-type data)]
        (let [[address value _] (abi-spec/decode
                                 (str "0x" (subs data 10))
                                 (if (= type :approve-and-call) ["address" "uint256" "bytes"] ["address" "uint256"]))]
          (when (and address value)
            {:to       address
             :contact  (get-contact db address)
             :contract to
             :approve? (not= type :transfer)
             :amount   (money/to-fixed (money/token->unit value decimals))
             :token    token
             :symbol   symbol}))))))

(defn parse-tx-obj [db {:keys [from to value data]}]
  (merge {:from {:address from}}
         (if (nil? to)
           {:contact {:name (i18n/label :t/new-contract)}}
           (let [eth-value  (when value (money/bignumber value))
                 eth-amount (when eth-value (money/to-number (money/wei->ether eth-value)))
                 token      (get-transfer-token db to data)]
             (cond
               (and eth-amount (or (not (zero? eth-amount)) (nil? data)))
               {:to      to
                :contact (get-contact db to)
                :symbol  :ETH
                :amount  (str eth-amount)
                :token   (tokens/asset-for (:wallet/all-tokens db) (ethereum/chain-keyword db) :ETH)}
               (not (nil? token))
               token
               :else
               {:to      to
                :contact {:address (ethereum/normalized-address to)}})))))

(defn prepare-tx [db {{:keys [data gas gasPrice] :as tx-obj} :tx-obj :as tx}]
  (merge
   tx
   (parse-tx-obj db tx-obj)
   {:data     data
    :gas      (when gas (money/bignumber gas))
    :gasPrice (when gasPrice (money/bignumber gasPrice))}))

(fx/defn show-sign [{:keys [db] :as cofx}]
  (let [{:signing/keys [queue]} db
        {{:keys [gas gasPrice] :as tx-obj} :tx-obj {:keys [data typed?] :as message} :message :as tx} (last queue)
        keycard-multiaccount? (boolean (get-in db [:multiaccount :keycard-key-uid]))
        wallet-set-up-passed? (get-in db [:multiaccount :wallet-set-up-passed?])
        updated-db (if wallet-set-up-passed? db (assoc db :popover/popover {:view :signing-phrase}))]
    (if message
      {:db (assoc updated-db
                  :signing/in-progress? true
                  :signing/queue (drop-last queue)
                  :signing/tx tx
                  :signing/sign {:type           (if keycard-multiaccount? :keycard :password)
                                 :formatted-data (if typed? (types/json->clj data) (ethereum/hex-to-utf8 data))})}
      (fx/merge cofx
                {:db
                 (assoc updated-db
                        :signing/in-progress? true
                        :signing/queue (drop-last queue)
                        :signing/tx (prepare-tx updated-db tx))
                 :dismiss-keyboard
                 nil}
                #(when-not gas
                   {:signing/update-estimated-gas {:obj           tx-obj
                                                   :success-event :signing/update-estimated-gas-success}})
                #(when-not gasPrice
                   {:signing/update-gas-price {:success-event :signing/update-gas-price-success}})))))

(fx/defn check-queue [{:keys [db] :as cofx}]
  (let [{:signing/keys [in-progress? queue]} db]
    (when (and (not in-progress?) (seq queue))
      (show-sign cofx))))

(fx/defn transaction-result
  [{:keys [db] :as cofx} result tx-obj]
  (let [{:keys [on-result symbol]} (get db :signing/tx)]
    (fx/merge cofx
              {:db                              (dissoc db :signing/tx :signing/in-progress? :signing/sign)
               :signing/show-transaction-result nil}
              (prepare-unconfirmed-transaction result tx-obj symbol)
              (check-queue)
              #(when on-result
                 {:dispatch (conj on-result result)}))))

(fx/defn transaction-error
  [{:keys [db]} {:keys [code message]}]
  (let [on-error (get-in db [:signing/tx :on-error])]
    (if (= code constants/send-transaction-err-decrypt)
      ;;wrong password
      {:db (assoc-in db [:signing/sign :error] (i18n/label :t/wrong-password))}
      (merge {:db                             (dissoc db :signing/tx :signing/in-progress? :signing/sign)
              :signing/show-transaction-error message}
             (when on-error
               {:dispatch (conj on-error message)})))))

(fx/defn sign-message-completed
  {:events [:signing/sign-message-completed]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [result error]} (types/json->clj result)
        on-result (get-in db [:signing/tx :on-result])]
    (if error
      {:db (update db :signing/sign assoc :error (i18n/label :t/wrong-password) :in-progress? false)}
      (fx/merge cofx
                {:db (dissoc db :signing/tx :signing/in-progress? :signing/sign)}
                (check-queue)
                #(if on-result
                   {:dispatch (conj on-result result)})))))

(fx/defn transaction-completed
  {:events       [:signing/transaction-completed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [cofx response tx-obj]
  (let [cofx-in-progress-false (assoc-in cofx [:db :signing/sign :in-progress?] false)
        {:keys [result error]} (types/json->clj response)]
    (if error
      (transaction-error cofx-in-progress-false error)
      (transaction-result cofx-in-progress-false result tx-obj))))

(fx/defn discard
  "Discrad transaction signing"
  {:events [:signing.ui/cancel-is-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [on-error]} (get-in db [:signing/tx])]
    (fx/merge cofx
              {:db (dissoc db :signing/tx :signing/in-progress? :signing/sign)}
              (check-queue)
              #(when on-error
                 {:dispatch (conj on-error "transaction was cancelled by user")}))))

(defn normalize-tx-obj [db tx]
  (update-in tx [:tx-obj :from] #(eip55/address->checksum (or % (ethereum/default-address db)))))

(fx/defn sign [{:keys [db] :as cofx} tx]
  "Signing transaction or message, shows signing sheet
   tx
   {:tx-obj - transaction object to send https://github.com/ethereum/wiki/wiki/JavaScript-API#parameters-25
    :message {:address  :data  :typed? } - message data to sign
    :on-result - re-frame event vector
    :on-error - re-frame event vector}"
  (fx/merge cofx
            {:db (update db :signing/queue conj (normalize-tx-obj db tx))}
            (check-queue)))

(fx/defn eth-transaction-call
  "Prepares tx-obj for contract call and show signing sheet"
  [cofx {:keys [contract method params on-result on-error]}]
  (sign cofx {:tx-obj    {:to   contract
                          :data (abi-spec/encode method params)}
              :on-result on-result
              :on-error  on-error}))

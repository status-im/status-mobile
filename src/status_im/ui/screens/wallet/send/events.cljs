(ns status-im.ui.screens.wallet.send.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.models :as chat.models]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.i18n :as i18n]
            [status-im.models.transactions :as wallet.transactions]
            [status-im.models.wallet :as models.wallet]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.ethereum.eip55 :as eip55]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]))

(def wrong-password-error-code 5)

;;;; FX

(defn get-tx-params [{:keys [from to value gas gasPrice] :as params} symbol coin]
  (if (= :ETH symbol)
    params
    (erc20/transfer-tx (:address coin) from to value gas gasPrice)))

(defn send-transaction! [params symbol coin on-completed password]
  (let [tx-params (get-tx-params params symbol coin)]
    (status/send-transaction (types/clj->json tx-params)
                             password
                             on-completed)))

(handlers/register-handler-fx
 :wallet/add-unconfirmed-transaction
 (fn [{:keys [db now]} [_ transaction result]]
   {:db (assoc-in db [:wallet :transactions result]
                  (models.wallet/prepare-unconfirmed-transaction db now transaction result))}))

;;TODO(goranjovic) - fully refactor
(defn on-transaction-completed [transaction flow {:keys [public-key]} {:keys [decimals] :as coin} {:keys [result error]} in-progress?]
  (let [{:keys [id method to symbol amount on-result]} transaction
        amount-text (str (money/internal->formatted amount symbol decimals))]
    (if error
      ;; ERROR
      (do (utils/show-popup (i18n/label :t/error)
                            (if (= (:code error) wrong-password-error-code)
                              (i18n/label :t/wrong-password)
                              (:message error)))
          (reset! in-progress? false))
      (do
        (re-frame/dispatch [:wallet/add-unconfirmed-transaction transaction result])
        (if on-result
          (re-frame/dispatch (conj on-result id result method))
          (re-frame/dispatch [:send-transaction-message public-key flow {:address to
                                                                         :asset   (name symbol)
                                                                         :amount  amount-text
                                                                         :tx-hash result}]))))))

(defn send-transaction-wrapper [{:keys [transaction password flow all-tokens in-progress? chain contact account]}]
  (let [symbol (:symbol transaction)
        coin   (tokens/asset-for all-tokens (keyword chain) symbol)]
    (reset! in-progress? true)
    (send-transaction! (models.wallet/prepare-send-transaction (:address account) transaction)
                       symbol
                       coin
                       #(on-transaction-completed transaction flow contact coin (types/json->clj %) in-progress?)
                       password)))

(re-frame/reg-fx
 ::sign-message
 (fn [{:keys [params on-completed]}]
   (status/sign-message (types/clj->json params)
                        on-completed)))

(re-frame/reg-fx
 :wallet/show-transaction-error
 (fn [message]
   ;; (andrey) we need this timeout because modal window conflicts with alert
   (utils/set-timeout #(utils/show-popup (i18n/label :t/transaction-failed) message) 1000)))

;;;; Handlers
;; HANDLE QR CODE

(defn qr-data->send-tx-data [{:keys [address value symbol gas gasPrice public-key from-chat?]}]
  {:pre [(not (nil? address))]}
  (cond-> {:to address :public-key public-key}
    value (assoc :amount value)
    symbol (assoc :symbol symbol)
    gas (assoc :gas (money/bignumber gas))
    from-chat? (assoc :from-chat? from-chat?)
    gasPrice (assoc :gas-price (money/bignumber gasPrice))))

(defn extract-qr-code-details [chain all-tokens qr-uri]
  {:pre [(keyword? chain) (string? qr-uri)]}
  ;; i don't like fetching all tokens here
  (let [qr-uri (string/trim qr-uri)
        chain-id (ethereum/chain-keyword->chain-id chain)]
    (or (let [m (eip681/parse-uri qr-uri)]
          (merge m (eip681/extract-request-details m all-tokens)))
        (when (ethereum/address? qr-uri)
          {:address qr-uri :chain-id chain-id}))))

(defn qr-data->transaction-data [qr-data contacts]
  {:pre [(map? qr-data)]}
  (let [{:keys [to name] :as tx-details} (qr-data->send-tx-data qr-data)
        contact-name (:name (contact.db/find-contact-by-address contacts to))]
    (cond-> tx-details
      contact-name (assoc :to-name name))))

;; CHOOSEN RECIPIENT
(defn eth-name->address [web3 chain recipient callback]
  (if (ens/is-valid-eth-name? recipient)
    (ens/get-addr web3
                  (get ens/ens-registries chain)
                  recipient
                  #(callback %))
    (callback recipient)))

(defn chosen-recipient [web3 chain {:keys [to to-ens]} success-callback error-callback]
  {:pre [(keyword? chain) (string? to)]}
  (eth-name->address web3 chain to
                     (fn [to]
                       (if (ethereum/address? to)
                         (if (and (not to-ens) (not (eip55/valid-address-checksum? to)))
                           (error-callback :t/wallet-invalid-address-checksum)
                           (success-callback to))
                         (error-callback :t/invalid-address)))))

(handlers/register-handler-fx
 :wallet/transaction-to-success
 (fn [{:keys [db]} [_ recipient-address]]
   {:db (assoc-in db [:wallet :send-transaction :to] recipient-address)}))

;; SEND TRANSACTION
(handlers/register-handler-fx
 :wallet/send-transaction
 (fn [{{:keys [chain] :as db} :db} _]
   (let [{:keys [password symbol in-progress?] :as transaction} (get-in db [:wallet :send-transaction])
         all-tokens (:wallet/all-tokens db)
         from       (get-in db [:account/account :address])]
     (when-not in-progress?
       {:db                (-> db
                               (assoc-in [:wallet :send-transaction :wrong-password?] false)
                               (assoc-in [:wallet :send-transaction :in-progress?] true))
        ::send-transaction [(models.wallet/prepare-send-transaction from transaction)
                            all-tokens
                            symbol
                            chain
                            #(re-frame/dispatch [::transaction-completed (types/json->clj %)])
                            password]}))))

;; SIGN MESSAGE
(handlers/register-handler-fx
 :wallet/sign-message
 (fn [{db :db} _]
   (let [{:keys [data from password]} (get-in db [:wallet :send-transaction])]
     {:db            (assoc-in db [:wallet :send-transaction :in-progress?] true)
      ::sign-message {:params       {:data     data
                                     :password (security/safe-unmask-data password)
                                     :account  from}
                      :on-completed #(re-frame/dispatch [::transaction-completed (types/json->clj %)])}})))

;; SEND TRANSACTION (SIGN MESSAGE) CALLBACK
(handlers/register-handler-fx
 ::transaction-completed
 (fn [{:keys [db now] :as cofx} [_ {:keys [result error]}]]
   (let [{:keys [id method public-key to symbol amount-text on-result]} (get-in db [:wallet :send-transaction])
         db' (assoc-in db [:wallet :send-transaction :in-progress?] false)
         transaction (get-in db [:wallet :send-transaction])]
     (if error
        ;; ERROR
       (models.wallet/handle-transaction-error (assoc cofx :db db') error)
        ;; RESULT
       (merge
        {:db (cond-> (assoc-in db' [:wallet :send-transaction] {})

               (not= method constants/web3-personal-sign)
               (assoc-in [:wallet :transactions result]
                         (models.wallet/prepare-unconfirmed-transaction db now transaction result)))}

        (if on-result
          {:dispatch (conj on-result id result method)}
          {:dispatch [:send-transaction-message public-key :dapp {:address to
                                                                  :asset   (name symbol)
                                                                  :amount  amount-text
                                                                  :tx-hash result}]}))))))

(handlers/register-handler-fx
 :wallet.dapp/transaction-on-result
 (fn [{db :db} [_ message-id id result method]]
   (let [webview (:webview-bridge db)]
     (models.wallet/dapp-complete-transaction (int id) result method message-id webview))))

(handlers/register-handler-fx
 :wallet.dapp/transaction-on-error
 (fn [{db :db} [_ message-id message]]
   (models.wallet/web3-error-callback {} db message-id message)))

;; DAPP TRANSACTIONS QUEUE
;; NOTE(andrey) We need this queue because dapp can send several transactions in a row, this is bad behaviour
;; but we need to support it
(handlers/register-handler-fx
 :check-dapps-transactions-queue
 (fn [{:keys [db]} _]
   (let [{:keys [send-transaction transactions-queue]} (:wallet db)
         {:keys [payload message-id] :as queued-transaction} (last transactions-queue)
         {:keys [method params id]} payload
         db' (update-in db [:wallet :transactions-queue] drop-last)]
     (when (and (not (contains? #{:wallet-transaction-sent
                                  :wallet-transaction-sent-modal}
                                (:view-id db)))
                (not (:id send-transaction)) queued-transaction)
       (cond

         ;;SEND TRANSACTION
         (= method constants/web3-send-transaction)
         (let [transaction (models.wallet/prepare-dapp-transaction queued-transaction (:contacts/contacts db))]
           (models.wallet/open-modal-wallet-for-transaction db' transaction))

         ;;SIGN MESSAGE
         (= method constants/web3-personal-sign)
         (let [[address data] (models.wallet/normalize-sign-message-params params)]
           (if (and address data)
             (let [db'' (assoc-in db' [:wallet :send-transaction]
                                  {:id               (str (or id message-id))
                                   :from             address
                                   :data             data
                                   :on-result        [:wallet.dapp/transaction-on-result message-id]
                                   :on-error         [:wallet.dapp/transaction-on-error message-id]
                                   :method           method})]
               (navigation/navigate-to-cofx {:db db''} :wallet-sign-message-modal nil))
             {:db db'})))))))

(handlers/register-handler-fx
 :send-transaction-message
 (concat [(re-frame/inject-cofx :random-id-generator)]
         navigation/navigation-interceptors)
 (fn [{:keys [db] :as cofx} [_ chat-id flow params]]
   ;;NOTE(goranjovic): we want to send the payment message only when we have a whisper id
   ;; for the recipient, we always redirect to `:wallet-transaction-sent` even when we don't
   (let [send-command? (and chat-id (get-in db [:id->command ["send" #{:personal-chats}]]))]
     (fx/merge cofx
               #(when (and chat-id send-command?)
                  (commands-sending/send % chat-id send-command? params))
               (navigation/navigate-to-clean :wallet-transaction-sent {:flow    flow
                                                                       :chat-id chat-id})))))
(handlers/register-handler-fx
 :wallet/discard-transaction-navigate-back
 (fn [cofx _]
   (fx/merge cofx
             (navigation/navigate-back)
             (models.wallet/discard-transaction))))

(handlers/register-handler-fx
 :wallet/cancel-entering-password
 (fn [{:keys [db]} _]
   {:db (update-in db [:wallet :send-transaction] assoc
                   :show-password-input? false
                   :wrong-password? false
                   :password nil)}))

(handlers/register-handler-fx
 :wallet.send/set-password
 (fn [{:keys [db]} [_ masked-password]]
   {:db (assoc-in db [:wallet :send-transaction :password] masked-password)}))

(handlers/register-handler-fx
 :close-transaction-sent-screen
 (fn [cofx [_ chat-id flow]]
   (fx/merge cofx
             {:dispatch-later [{:ms 400 :dispatch [:check-dapps-transactions-queue]}]}
             #(case flow
                :chat (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id {}])
                :dapp (re-frame/dispatch [:navigate-back])
                (re-frame/dispatch [:navigate-to :wallet {}])))))


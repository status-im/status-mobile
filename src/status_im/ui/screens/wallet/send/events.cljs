(ns status-im.ui.screens.wallet.send.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.db :as db]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.money :as money]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.constants :as constants]
            [status-im.transport.utils :as transport.utils]))

;;;; FX

(re-frame/reg-fx
  ::accept-transaction
  (fn [{:keys [password id on-completed]}]
    (status/approve-sign-requests (list id) password on-completed)))

(defn- send-ethers [{:keys [web3 from to value gas gas-price]}]
  (.sendTransaction (.-eth web3)
                    (clj->js {:from from :to to :value value :gas gas :gasPrice gas-price})
                    #()))

(defn- send-tokens [{:keys [web3 from to value gas gas-price symbol network]}]
  (let [contract (:address (tokens/symbol->token (ethereum/network->chain-keyword network) symbol))]
    (erc20/transfer web3 contract from to value {:gas gas :gasPrice gas-price} #())))

(re-frame/reg-fx
  ::send-transaction
  (fn [{:keys [symbol] :as params}]
    (case symbol
      :ETH (send-ethers params)
      (send-tokens params))))

(re-frame/reg-fx
  ::show-transaction-moved
  (fn [modal?]
    (utils/show-popup
      (i18n/label :t/transaction-moved-title)
      (i18n/label :t/transaction-moved-text)
      (when modal?
        #(re-frame/dispatch [:navigate-back])))))

(re-frame/reg-fx
  ::show-transaction-error
  (fn [message]
    ;; (andrey) we need this timeout because modal window conflicts with alert
    (utils/set-timeout #(utils/show-popup (i18n/label :t/transaction-failed) message) 1000)))

(re-frame/reg-fx
  :discard-transaction
  (fn [id]
    (status/discard-sign-request id)))

;;Helper functions

(defn transaction-valid? [{{:keys [to data]} :args}]
  (or (and to (utils.hex/valid-hex? to)) (and data (not= data "0x"))))

(defn dispatch-transaction-completed [result & [modal?]]
  (re-frame/dispatch [::transaction-completed {:id (name (key result)) :response (second result)} modal?]))
;;;; Handlers

(handlers/register-handler-fx
  :wallet.send/set-and-validate-amount
  (fn [{:keys [db]} [_ amount]]
    (let [{:keys [value error]} (wallet.db/parse-amount amount)]
      {:db (-> db
               (assoc-in [:wallet :send-transaction :amount] (money/ether->wei value))
               (assoc-in [:wallet :send-transaction :amount-error] error))})))

(handlers/register-handler-fx
  :wallet.send/set-symbol
  (fn [{:keys [db]} [_ symbol]]
    {:db (-> (assoc-in db [:wallet :send-transaction :symbol] symbol)
             (assoc-in [:wallet :send-transaction :gas] (ethereum/estimate-gas symbol)))}))

(handlers/register-handler-fx
  :wallet.send/toggle-advanced
  (fn [{:keys [db]} [_ advanced?]]
    {:db (assoc-in db [:wallet :send-transaction :advanced?] advanced?)}))

(def ^:private clear-send-properties {:id              nil
                                      :signing?        false
                                      :wrong-password? false
                                      :waiting-signal? false
                                      :from-chat?      false})

(defn on-transactions-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (dispatch-transaction-completed result))))

(handlers/register-handler-fx
  :sign-later-from-chat
  (fn [_ _]
    {::show-transaction-moved  true}))

;;TRANSACTION QUEUED signal from status-go
(handlers/register-handler-fx
  :sign-request-queued
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ {:keys [id message_id method args]}]]
    (cond

      (= method constants/web3-send-transaction)
      ;;NOTE(goranjovic): the transactions started from chat using /send command
      ;; are only in ether, so this parameter defaults to ETH
      (let [{:keys [from to value symbol data gas gasPrice] :or {symbol :ETH}} args
            ;;TODO (andrey) revisit this map later (this map from old transactions, idk if we need all these fields)
            transaction {:id         id
                         :from       from
                         :to         to
                         :to-name    (when (nil? to)
                                       (i18n/label :t/new-contract))
                         :symbol     symbol
                         :value      (money/bignumber (or value 0))
                         :data       data
                         :gas        (when (seq gas)
                                       (money/bignumber (money/to-decimal gas)))
                         :gas-price  (when (seq gasPrice)
                                       (money/bignumber (money/to-decimal gasPrice)))
                         :timestamp  now
                         :message-id message_id}
            sending-from-bot-or-dapp? (not (get-in db [:wallet :send-transaction :waiting-signal?]))
            new-db (assoc-in db [:wallet :transactions-unsigned id] transaction)
            sending-db {:id         id
                        :method     method
                        :from-chat? sending-from-bot-or-dapp?}]
        (if sending-from-bot-or-dapp?
          ;;SENDING FROM BOT (CHAT) OR DAPP
          {:db         (assoc-in new-db [:wallet :send-transaction] sending-db) ; we need to completely reset sending state here
           :dispatch-n [[:update-wallet]
                        [:navigate-to-modal :wallet-send-transaction-modal]
                        (when-not (seq gas)
                          [:wallet/update-estimated-gas transaction])
                        (when-not (seq gasPrice)
                          [:wallet/update-gas-price])]}
          ;;WALLET SEND SCREEN WAITING SIGNAL
          (let [{:keys [later? password]} (get-in db [:wallet :send-transaction])
                new-db' (update-in new-db [:wallet :send-transaction] merge sending-db)] ; just update sending state as we are in wallet flow
            (if later?
              ;;SIGN LATER
              {:db                      (assoc-in new-db' [:wallet :send-transaction :waiting-signal?] false)
               :dispatch                [:navigate-back]
               ::show-transaction-moved false}
              ;;SIGN NOW
              {:db                  new-db'
               ::accept-transaction {:id           id
                                     :password     password
                                     :on-completed on-transactions-completed}}))))

      (= method constants/web3-personal-sign)

      (let [{:keys [data]} args
            data' (transport.utils/to-utf8 data)]
        (when data'
          {:db (-> db
                   (assoc-in [:wallet :transactions-unsigned id] {:data data' :id id})
                   (assoc-in [:wallet :send-transaction] {:id id :method method}))
           :dispatch [:navigate-to-modal :wallet-sign-message-modal]})))))

(defn this-transaction-signing? [id signing-id view-id modal]
  (and (= signing-id id)
       (or (= view-id :wallet-send-transaction)
           (= modal :wallet-send-transaction-modal)
           (= modal :wallet-sign-message-modal))))

;;TRANSACTION FAILED signal from status-go
(handlers/register-handler-fx
  :sign-request-failed
  (fn [{{:keys [view-id modal] :as db} :db} [_ {:keys [id method error_code error_message]}]]
    (let [send-transaction (get-in db [:wallet :send-transaction])]
      (case error_code

        ;;WRONG PASSWORD
        constants/send-transaction-password-error-code
        {:db (assoc-in db [:wallet :send-transaction :wrong-password?] true)}

        ;;NO ERROR, DISCARDED, TIMEOUT or DEFAULT ERROR
        (if (this-transaction-signing? id (:id send-transaction) view-id modal)
          (cond-> {:db                      (-> db
                                                (update-in [:wallet :transactions-unsigned] dissoc id)
                                                (update-in [:wallet :send-transaction] merge clear-send-properties))
                   :dispatch                [:navigate-back]}
            (= method constants/web3-send-transaction)
            (assoc ::show-transaction-error error_message))
          {:db (update-in db [:wallet :transactions-unsigned] dissoc id)})))))

(defn prepare-unconfirmed-transaction [db now hash id]
  (let [transaction (get-in db [:wallet :transactions-unsigned id])]
    (-> transaction
        (assoc :confirmations "0"
               :timestamp (str now)
               :type :outbound
               :hash hash)
        (update :gas-price str)
        (update :value str)
        (update :gas str)
        (dissoc :message-id :id))))

(handlers/register-handler-fx
  ::transaction-completed
  (fn [{db :db now :now} [_ {:keys [id response]} modal?]]
    (let [{:keys [hash error]} response
          {:keys [method]} (get-in db [:wallet :send-transaction])
          db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
      (if (and error (string? error) (not (string/blank? error))) ;; ignore error here, error will be handled in :transaction-failed
        {:db db'}
        (merge
          {:db (cond-> db'
                       (= method constants/web3-send-transaction)
                       (assoc-in [:wallet :transactions hash] (prepare-unconfirmed-transaction db now hash id))
                       true
                       (update-in [:wallet :transactions-unsigned] dissoc id)
                       true
                       (update-in [:wallet :send-transaction] merge clear-send-properties))}
          (if modal?
            (cond-> {:dispatch [:navigate-back]}
                    (= method constants/web3-send-transaction)
                    (assoc :dispatch-later [{:ms 400 :dispatch [:navigate-to-modal :wallet-transaction-sent-modal]}]))
            {:dispatch [:navigate-to :wallet-transaction-sent]}))))))

(defn on-transactions-modal-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (dispatch-transaction-completed result true))))

(handlers/register-handler-fx
  :wallet/sign-transaction
  (fn [{{:keys [web3] :as db} :db} [_ later?]]
    (let [db' (assoc-in db [:wallet :send-transaction :wrong-password?] false)
          network (:network db)
          {:keys [amount id password to symbol gas gas-price]} (get-in db [:wallet :send-transaction])]
      (if id
        {::accept-transaction {:id           id
                               :password     password
                               :on-completed on-transactions-completed}
         :db                  (assoc-in db' [:wallet :send-transaction :in-progress?] true)}
        {:db                (update-in db' [:wallet :send-transaction] assoc
                                       :waiting-signal? true
                                       :later? later?
                                       :in-progress? true)
         ::send-transaction {:web3      web3
                             :from      (get-in db [:account/account :address])
                             :to        to
                             :value     amount
                             :gas       gas
                             :gas-price gas-price
                             :symbol    symbol
                             :network   network}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction-modal
  (fn [{db :db} _]
    (let [{:keys [id password]} (get-in db [:wallet :send-transaction])]
      {:db                  (assoc-in db [:wallet :send-transaction :in-progress?] true)
       ::accept-transaction {:id           id
                             :password     password
                             :on-completed on-transactions-modal-completed}})))

(defn discard-transaction
  [{:keys [db]}]
  (let [{:keys [id]} (get-in db [:wallet :send-transaction])]
    (merge {:db (update-in db [:wallet :send-transaction] merge clear-send-properties)}
           (when id
             {:discard-transaction id}))))

(handlers/register-handler-fx
  :wallet/discard-transaction
  (fn [cofx _]
    (discard-transaction cofx)))

(handlers/register-handler-fx
  :wallet/discard-transaction-navigate-back
  (fn [cofx _]
    (-> cofx
        discard-transaction
        (assoc :dispatch [:navigate-back]))))

(handlers/register-handler-fx
  :wallet/cancel-signing-modal
  (fn [{:keys [db]} _]
    {:db (update-in db [:wallet :send-transaction] assoc
                    :signing? false
                    :wrong-password? false)}))

(handlers/register-handler-fx
  :wallet.send/set-password
  (fn [{:keys [db]} [_ password]]
    {:db (assoc-in db [:wallet :send-transaction :password] password)}))

(handlers/register-handler-fx
  :wallet.send/set-signing?
  (fn [{:keys [db]} [_ signing?]]
    {:db (assoc-in db [:wallet :send-transaction :signing?] signing?)}))

(handlers/register-handler-fx
  :wallet.send/edit-gas
  (fn [{:keys [db]} [_ gas]]
    {:db (assoc-in db [:wallet :edit :gas] (money/bignumber gas))}))

(handlers/register-handler-fx
  :wallet.send/edit-gas-price
  (fn [{:keys [db]} [_ gas-price]]
    {:db (assoc-in db [:wallet :edit :gas-price] (money/bignumber gas-price))}))

(handlers/register-handler-fx
  :wallet.send/set-gas-details
  (fn [{:keys [db]} [_ gas gas-price]]
    {:db (-> db
             (assoc-in [:wallet :send-transaction :gas] gas)
             (assoc-in [:wallet :send-transaction :gas-price] gas-price))}))

(handlers/register-handler-fx
  :wallet.send/clear-gas
  (fn [{:keys [db]}]
    {:db (update db :wallet dissoc :edit)}))

(handlers/register-handler-fx
  :wallet.send/reset-gas-default
  (fn [{:keys [db]}]
    {:dispatch [:wallet/update-gas-price true]
     :db       (update-in db [:wallet :edit]
                          assoc
                          :gas (ethereum/estimate-gas (get-in db [:wallet :send-transaction :symbol])))}))

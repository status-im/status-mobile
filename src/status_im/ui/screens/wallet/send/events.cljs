(ns status-im.ui.screens.wallet.send.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.models.wallet :as models.wallet]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.constants :as constants]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.models.transactions :as wallet.transactions]))

;;;; FX

(defn- send-ethers [params on-completed masked-password]
  (status/send-transaction (types/clj->json params)
                           (security/unmask masked-password)
                           on-completed))

(defn- send-tokens [symbol chain {:keys [from to value gas gasPrice]} on-completed masked-password]
  (let [contract (:address (tokens/symbol->token (keyword chain) symbol))]
    (erc20/transfer contract from to value gas gasPrice masked-password on-completed)))

(re-frame/reg-fx
 ::send-transaction
 (fn [[params symbol chain on-completed masked-password]]
   (case symbol
     :ETH (send-ethers params on-completed masked-password)
     (send-tokens symbol chain params on-completed masked-password))))

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

;; SEND TRANSACTION
(handlers/register-handler-fx
 :wallet/send-transaction
 (fn [{{:keys [chain] :as db} :db} _]
   (let [{:keys [password symbol in-progress?] :as transaction} (get-in db [:wallet :send-transaction])
         from (get-in db [:account/account :address])]
     (when-not in-progress?
       {:db                (-> db
                               (assoc-in [:wallet :send-transaction :wrong-password?] false)
                               (assoc-in [:wallet :send-transaction :in-progress?] true))
        ::send-transaction [(models.wallet/prepare-send-transaction from transaction)
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
                                     :password (security/unmask password)
                                     :account  from}
                      :on-completed #(re-frame/dispatch [::transaction-completed (types/json->clj %)])}})))

;; SEND TRANSACTION (SIGN MESSAGE) CALLBACK
(handlers/register-handler-fx
 ::transaction-completed
 (fn [{:keys [db now]} [_ {:keys [result error]}]]
   (let [{:keys [id method whisper-identity to symbol amount-text dapp-transaction]} (get-in db [:wallet :send-transaction])
         db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
     (if error
       ;; ERROR
       (models.wallet/handle-transaction-error db' error)
       ;; RESULT
       (merge
        {:db (cond-> (assoc-in db' [:wallet :send-transaction] {})

               (not= method constants/web3-personal-sign)
               (assoc-in [:wallet :transactions result]
                         (models.wallet/prepare-unconfirmed-transaction db now result)))}

        (if dapp-transaction
          (let [{:keys [message-id]} dapp-transaction
                webview (:webview-bridge db)]
            (models.wallet/dapp-complete-transaction (int id) result method message-id webview))
          {:dispatch [:send-transaction-message whisper-identity {:address to
                                                                  :asset   (name symbol)
                                                                  :amount  amount-text
                                                                  :tx-hash result}]}))))))

;; DISCARD TRANSACTION
(handlers/register-handler-fx
 :wallet/discard-transaction
 (fn [cofx _]
   (models.wallet/discard-transaction cofx)))

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
     (when (and (not (:id send-transaction)) queued-transaction)
       (cond

         ;;SEND TRANSACTION
         (= method constants/web3-send-transaction)
         (let [{:keys [gas gasPrice] :as transaction} (models.wallet/prepare-dapp-transaction
                                                       queued-transaction (:contacts/contacts db))
               {:keys [wallet-set-up-passed?]} (:account/account db)]
           {:db         (assoc-in db' [:wallet :send-transaction] transaction)
            :dispatch-n [[:update-wallet]
                         (when-not gas
                           [:wallet/update-estimated-gas (first params)])
                         (when-not gasPrice
                           [:wallet/update-gas-price])
                         [:navigate-to-modal (if wallet-set-up-passed?
                                               :wallet-send-transaction-modal
                                               :wallet-onboarding-setup-modal)]]})

         ;;SIGN MESSAGE
         (= method constants/web3-personal-sign)
         (let [[address data] (models.wallet/normalize-sign-message-params params)]
           (if (and address data)
             {:db       (assoc-in db' [:wallet :send-transaction] {:id               (str (or id message-id))
                                                                   :from             address
                                                                   :data             data
                                                                   :dapp-transaction queued-transaction
                                                                   :method           method})
              :dispatch [:navigate-to-modal :wallet-sign-message-modal]}
             {:db db'})))))))

(handlers/register-handler-fx
 :send-transaction-message
 (concat models.message/send-interceptors
         navigation/navigation-interceptors)
 (fn [{:keys [db] :as cofx} [chat-id params]]
   ;;NOTE(goranjovic): we want to send the payment message only when we have a whisper id
   ;; for the recipient, we always redirect to `:wallet-transaction-sent` even when we don't
   (if-let [send-command (and chat-id (get-in db [:id->command ["send" #{:personal-chats}]]))]
     (handlers-macro/merge-fx cofx
                              (commands-sending/send chat-id send-command params)
                              (navigation/replace-view :wallet-transaction-sent))
     (handlers-macro/merge-fx cofx
                              (navigation/replace-view :wallet-transaction-sent)))))

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
 :wallet/discard-transaction-navigate-back
 (fn [cofx _]
   (-> cofx
       models.wallet/discard-transaction
       (assoc :dispatch [:navigate-back]))))

(defn update-gas-price
  ([db edit? success-event]
   {:update-gas-price {:web3          (:web3 db)
                       :success-event (or success-event :wallet/update-gas-price-success)
                       :edit?         edit?}})
  ([db edit?] (update-gas-price db edit? :wallet/update-gas-price-success))
  ([db] (update-gas-price db false :wallet/update-gas-price-success)))

(defn recalculate-gas [{:keys [db] :as fx} symbol]
  (-> fx
      (assoc-in [:db :wallet :send-transaction :gas] (ethereum/estimate-gas symbol))
      (merge (update-gas-price db))))

(handlers/register-handler-fx
 :wallet/update-gas-price
 (fn [{:keys [db]} [_ edit?]]
   (update-gas-price db edit?)))

(handlers/register-handler-fx
 :wallet.send/set-symbol
 (fn [{:keys [db]} [_ symbol]]
   (let [old-symbol (get-in db [:wallet :send-transaction :symbol])]
     (cond-> {:db (-> db
                      (assoc-in [:wallet :send-transaction :symbol] symbol)
                      (assoc-in [:wallet :send-transaction :amount] nil)
                      (assoc-in [:wallet :send-transaction :amount-text] nil)
                      (assoc-in [:wallet :send-transaction :asset-error] nil))}
       (not= old-symbol symbol) (recalculate-gas symbol)))))

(handlers/register-handler-fx
 :wallet.send/toggle-advanced
 (fn [{:keys [db]} [_ advanced?]]
   {:db (assoc-in db [:wallet :send-transaction :advanced?] advanced?)}))

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
 :wallet.send/edit-value
 (fn [cofx [_ key value]]
   (models.wallet/edit-value key value cofx)))

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
 (fn [{:keys [db] :as cofx}]
   (let [gas-estimate (money/to-fixed
                       (ethereum/estimate-gas
                        (-> db :wallet :send-transaction :symbol)))]
     (assoc (models.wallet/edit-value
             :gas
             gas-estimate
             cofx)
            :dispatch [:wallet/update-gas-price true]))))

(handlers/register-handler-fx
 :close-transaction-sent-screen
 (fn [{:keys [db]} [_ chat-id]]
   {:dispatch       [:navigate-back]
    :dispatch-later [{:ms 400 :dispatch [:check-dapps-transactions-queue]}]}))

(handlers/register-handler-fx
 :sync-wallet-transactions
 (fn [cofx _]
   (wallet.transactions/sync cofx)))

(handlers/register-handler-fx
 :start-wallet-transactions-sync
 (fn [cofx _]
   (when-not (get-in cofx [:db :wallet :transactions-sync-started?])
     (handlers-macro/merge-fx cofx
                              (wallet.transactions/load-missing-chat-transactions)
                              (wallet.transactions/sync)
                              (wallet.transactions/set-sync-started)))))

(ns status-im.ui.screens.wallet.send.events
  (:require [re-frame.core :as re-frame]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.db :as wallet.db]))

;;;; FX

(defn- send-ethers [params on-completed masked-password]
  (status/send-transaction (types/clj->json params)
                           (security/safe-unmask-data masked-password)
                           on-completed))

(defn- send-tokens [all-tokens symbol chain {:keys [from to value gas gasPrice]} on-completed masked-password]
  (let [contract (:address (tokens/symbol->token all-tokens (keyword chain) symbol))]
    (erc20/transfer contract from to value gas gasPrice masked-password on-completed)))

(re-frame/reg-fx
 ::send-transaction
 (fn [[params all-tokens symbol chain on-completed masked-password]]
   (case symbol
     :ETH (send-ethers params on-completed masked-password)
     (send-tokens all-tokens symbol chain params on-completed masked-password))))

(re-frame/reg-fx
 ::sign-message
 (fn [{:keys [params on-completed]}]
   (status/sign-message (types/clj->json params)
                        on-completed)))

(re-frame/reg-fx
 ::sign-typed-data
 (fn [{:keys [data on-completed password]}]
   (status/sign-typed-data data (security/safe-unmask-data password) on-completed)))

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
         all-tokens (:wallet/all-tokens db)
         from       (get-in db [:account/account :address])]
     (when-not in-progress?
       {:db                (-> db
                               (assoc-in [:wallet :send-transaction :wrong-password?] false)
                               (assoc-in [:wallet :send-transaction :in-progress?] true))
        ::send-transaction [(wallet/prepare-send-transaction from transaction)
                            all-tokens
                            symbol
                            chain
                            #(re-frame/dispatch [:wallet.callback/transaction-completed (types/json->clj %)])
                            password]}))))

;; SIGN MESSAGE
(handlers/register-handler-fx
 :wallet/sign-message
 (fn [_ [_ typed? screen-params password-error-cb]]
   (let [{:keys [data from password]} screen-params]
     (if typed?
       {::sign-typed-data {:data     data
                           :password password
                           :account  from
                           :on-completed #(re-frame/dispatch [::sign-message-completed
                                                              screen-params
                                                              (types/json->clj %)
                                                              password-error-cb])}}
       {::sign-message {:params       {:data     data
                                       :password (security/safe-unmask-data password)
                                       :account  from}
                        :on-completed #(re-frame/dispatch [::sign-message-completed
                                                           screen-params
                                                           (types/json->clj %)
                                                           password-error-cb])}}))))

(fx/defn send-transaction-message
  "NOTE(goranjovic): we want to send the payment message only when we have a
   whisper id for the recipient, we always redirect to `:wallet-transaction-sent`
   even when we don't"
  [{:keys [db] :as cofx} chat-id params]
  (let [send-command? (and chat-id
                           (get-in db [:id->command ["send" #{:personal-chats}]]))]
    (when send-command?
      (commands-sending/send cofx chat-id send-command? params))))

;; SEND TRANSACTION CALLBACK
(handlers/register-handler-fx
 :wallet.callback/transaction-completed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{:keys [db now] :as cofx} [_ {:keys [result error]}]]
   (let [{:keys [id method public-key to symbol amount-text on-result on-error
                 send-transaction-message?]}
         (get-in db [:wallet :send-transaction])
         db' (assoc-in db [:wallet :send-transaction :in-progress?] false)
         modal-screen-was-used? (get-in db [:navigation/screen-params :wallet-send-modal-stack :modal?])]
     (if error
       ;; ERROR
       (wallet/handle-transaction-error (assoc cofx :db db') error)
       ;; RESULT
       (fx/merge cofx
                 (merge
                  {:db (cond-> (assoc-in db' [:wallet :send-transaction] {})

                         (not (constants/web3-sign-message? method))
                         (assoc-in [:wallet :transactions result]
                                   (wallet/prepare-unconfirmed-transaction db now result)))}
                  (when  on-result
                    {:dispatch (conj on-result id result method)}))
                 #(when (or (not on-result)
                            send-transaction-message?)
                    (send-transaction-message
                     %
                     public-key
                     {:address to
                      :asset   (name symbol)
                      :amount  amount-text
                      :tx-hash result}))
                 #(when-not on-result
                    (navigation/navigate-to-clean
                     %
                     (if modal-screen-was-used?
                       :wallet-transaction-sent-modal
                       :wallet-transaction-sent)
                     {})))))))

(re-frame/reg-fx
 :show-sign-message-error
 (fn [[password-error-cb]]
   (password-error-cb)))

;; SIGN MESSAGE CALLBACK
(handlers/register-handler-fx
 ::sign-message-completed
 (fn [{:keys [db now] :as cofx} [_ {:keys [on-result id method]} {:keys [result error]} password-error-cb]]
   (let [db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
     (if error
       ;; ERROR
       {:show-sign-message-error [password-error-cb]}
       ;; RESULT
       (if on-result
         {:dispatch (conj on-result id result method)})))))

(handlers/register-handler-fx
 ::hash-message-completed
 (fn [{:keys [db] :as cofx} [_ {:keys [result error]}]]
   (let [view-id (:view-id db)
         db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
     (if error
       ;; ERROR
       (wallet/handle-transaction-error (assoc cofx :db db') error)
       ;; RESULT
       (fx/merge cofx
                 {:db (-> db
                          (assoc-in [:hardwallet :pin :enter-step] :sign)
                          (assoc-in [:hardwallet :hash] result))}
                 (navigation/navigate-to-cofx
                  (if (contains?
                       #{:wallet-sign-message-modal :wallet-send-transaction-modal :wallet-send-modal-stack}
                       view-id)
                    :enter-pin-modal
                    :enter-pin-sign)
                  nil))))))

;; DISCARD TRANSACTION
(handlers/register-handler-fx
 :wallet/discard-transaction
 (fn [cofx _]
   (wallet/discard-transaction cofx)))

(handlers/register-handler-fx
 :wallet.dapp/transaction-on-result
 (fn [{db :db} [_ message-id id result method]]
   (let [webview (:webview-bridge db)
         keycard? (boolean (get-in db [:account/account :keycard-instance-uid]))]
     (wallet/dapp-complete-transaction (int id) result method message-id webview keycard?))))

(handlers/register-handler-fx
 :wallet.dapp/transaction-on-error
 (fn [{db :db} [_ message-id message]]
   (wallet/web3-error-callback {} db message-id message)))

;; DAPP TRANSACTIONS QUEUE
;; NOTE(andrey) We need this queue because dapp can send several transactions in a row, this is bad behaviour
;; but we need to support it
(handlers/register-handler-fx
 :check-dapps-transactions-queue
 (fn [{:keys [db]} _]
   (let [{:keys [send-transaction transactions-queue]} (:wallet db)
         {:keys [payload message-id] :as queued-transaction} (last transactions-queue)
         {:keys [method params id]} payload
         keycard? (boolean (get-in db [:account/account :keycard-instance-uid]))
         db' (update-in db [:wallet :transactions-queue] drop-last)]
     (when (and (not (contains? #{:wallet-transaction-sent
                                  :wallet-transaction-sent-modal}
                                (:view-id db)))
                (not (:id send-transaction)) queued-transaction)
       (cond

         ;;SEND TRANSACTION
         (= method constants/web3-send-transaction)
         (let [transaction (wallet/prepare-dapp-transaction queued-transaction (:contacts/contacts db))]
           (wallet/open-modal-wallet-for-transaction db' transaction (first params)))

         ;;SIGN MESSAGE
         (constants/web3-sign-message? method)
         (let [typed? (not= constants/web3-personal-sign method)
               [address data] (wallet/normalize-sign-message-params params)]
           (if (and address data)
             (let [signing-phrase (-> (get-in db [:account/account :signing-phrase])
                                      (clojure.string/replace-all #" " "     "))
                   screen-params {:id             (str (or id message-id))
                                  :from           address
                                  :data           data
                                  :typed?         typed?
                                  :decoded-data   (if typed? (types/json->clj data) (transport.utils/to-utf8 data))
                                  :on-result      [:wallet.dapp/transaction-on-result message-id]
                                  :on-error       [:wallet.dapp/transaction-on-error message-id]
                                  :method         method
                                  :signing-phrase signing-phrase
                                  :keycard?       keycard?}]
               (navigation/navigate-to-cofx {:db db'} :wallet-sign-message-modal screen-params))
             {:db db'})))))))

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
   (fx/merge cofx
             (navigation/navigate-back)
             (wallet/discard-transaction))))

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
   (wallet/edit-value key value cofx)))

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
   (let [gas-default (if-some [original-gas (-> db :wallet :send-transaction :original-gas)]
                       (money/to-fixed original-gas)
                       (money/to-fixed
                        (ethereum/estimate-gas
                         (-> db :wallet :send-transaction :symbol))))]
     (assoc (wallet/edit-value
             :gas
             gas-default
             cofx)
            :dispatch [:wallet/update-gas-price true]))))

(handlers/register-handler-fx
 :close-transaction-sent-screen
 (fn [cofx [_ chat-id]]
   (fx/merge cofx
             {:dispatch-later [{:ms 400 :dispatch [:check-dapps-transactions-queue]}]}
             (navigation/navigate-back))))

(re-frame/reg-fx
 ::hash-transaction
 (fn [{:keys [transaction on-completed]}]
   (status/hash-transaction (types/clj->json transaction) on-completed)))

(re-frame/reg-fx
 ::hash-message
 (fn [{:keys [message on-completed]}]
   (status/hash-message message on-completed)))

(re-frame/reg-fx
 ::hash-typed-data
 (fn [{:keys [data on-completed]}]
   (status/hash-typed-data data on-completed)))

(defn- prepare-keycard-transaction
  [transaction from symbol chain all-tokens]
  (if (= :ETH symbol)
    (wallet/prepare-send-transaction from transaction)
    (let [contract (:address (tokens/symbol->token all-tokens (keyword chain) symbol))
          {:keys [gas gasPrice to from value]} (wallet/prepare-send-transaction from transaction)]
      (merge (ethereum/call-params contract "transfer(address,uint256)" to value)
             {:from     from
              :gas      gas
              :gasPrice gasPrice}))))

(defn send-keycard-transaction
  [{{:keys [chain] :as db} :db}]
  (let [{:keys [symbol] :as transaction} (get-in db [:wallet :send-transaction])
        all-tokens (:wallet/all-tokens db)
        from (get-in db [:account/account :address])]
    {::hash-transaction {:transaction  (prepare-keycard-transaction transaction from symbol chain all-tokens)
                         :all-tokens   all-tokens
                         :symbol       symbol
                         :chain        chain
                         :on-completed #(re-frame/dispatch [:wallet.callback/hash-transaction-completed %])}}))

(handlers/register-handler-fx
 :wallet.callback/hash-transaction-completed
 (fn [{:keys [db] :as cofx} [_ result]]
   (let [{:keys [transaction hash]} (:result (types/json->clj result))]
     (fx/merge cofx
               {:db (-> db
                        (assoc-in [:hardwallet :pin :enter-step] :sign)
                        (assoc-in [:hardwallet :transaction] transaction)
                        (assoc-in [:hardwallet :hash] hash))}
               (navigation/navigate-to-clean
                (if (contains?
                     #{:wallet-sign-message-modal :wallet-send-transaction-modal :wallet-send-modal-stack}
                     (:view-id db))
                  :enter-pin-modal
                  :enter-pin-sign)
                nil)))))

(handlers/register-handler-fx
 :wallet.ui/sign-transaction-button-clicked
 (fn [{:keys [db] :as cofx} _]
   (let [keycard-account? (boolean (get-in db [:account/account :keycard-instance-uid]))]
     (if keycard-account?
       (send-keycard-transaction cofx)
       {:db (assoc-in db [:wallet :send-transaction :show-password-input?] true)}))))

(fx/defn keycard-hash-message
  [_ data typed?]
  (if typed?
    {::hash-typed-data {:data         data
                        :on-completed #(re-frame/dispatch [::hash-message-completed (types/json->clj %)])}}
    {::hash-message {:message      (ethereum/naked-address data)
                     :on-completed #(re-frame/dispatch [::hash-message-completed (types/json->clj %)])}}))

(handlers/register-handler-fx
 :wallet.ui/sign-message-button-clicked
 (fn [{:keys [db] :as cofx} [_ typed? screen-params password-error-cb]]
   (let [{:keys [data from password]} screen-params
         keycard-account? (boolean (get-in db [:account/account :keycard-instance-uid]))
         modal? (= (:view-id db) :wallet-sign-message-modal)]
     (if keycard-account?
       (fx/merge cofx
                 {:db (assoc-in db [:navigation/screen-params :wallet-send-modal-stack :modal?] modal?)}
                 (keycard-hash-message data typed?))
       (if typed?
         {::sign-typed-data {:data     data
                             :password password
                             :account  from
                             :on-completed #(re-frame/dispatch [::sign-message-completed
                                                                screen-params
                                                                (types/json->clj %)
                                                                password-error-cb])}}
         {::sign-message {:params       {:data     data
                                         :password (security/safe-unmask-data password)
                                         :account  from}
                          :on-completed #(re-frame/dispatch [::sign-message-completed
                                                             screen-params
                                                             (types/json->clj %)
                                                             password-error-cb])}})))))

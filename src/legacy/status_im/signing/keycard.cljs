(ns legacy.status-im.signing.keycard
  (:require
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::hash-transaction
 (fn [{:keys [transaction on-completed]}]
   (native-module/hash-transaction (types/clj->json transaction) on-completed)))

(re-frame/reg-fx
 ::hash-message
 (fn [{:keys [message on-completed]}]
   (native-module/hash-message message on-completed)))

(re-frame/reg-fx
 ::hash-typed-data
 (fn [{:keys [v4 data on-completed]}]
   (if v4
     (native-module/hash-typed-data-v4 data on-completed)
     (native-module/hash-typed-data data on-completed))))

(defn prepare-transaction
  [{:keys [gas gasPrice data nonce tx-obj] :as params}]
  (let [{:keys [from to value chat-id message-id command? maxPriorityFeePerGas maxFeePerGas]} tx-obj
        maxPriorityFeePerGas (or maxPriorityFeePerGas (get params :maxPriorityFeePerGas))
        maxFeePerGas         (or maxFeePerGas (get params :maxFeePerGas))]
    (cond-> {:from       from
             :to         to
             :value      value
             :chat-id    chat-id
             :message-id message-id
             :command?   command?}
      maxPriorityFeePerGas
      (assoc :maxPriorityFeePerGas
             (str "0x"
                  (native-module/number-to-hex
                   (js/parseInt maxPriorityFeePerGas))))
      maxFeePerGas
      (assoc :maxFeePerGas
             (str "0x"
                  (native-module/number-to-hex
                   (js/parseInt maxFeePerGas))))
      gas
      (assoc :gas (str "0x" (native-module/number-to-hex gas)))
      gasPrice
      (assoc :gasPrice (str "0x" (native-module/number-to-hex gasPrice)))
      data
      (assoc :data data)
      nonce
      (assoc :nonce nonce))))

(rf/defn hash-message
  [_ {:keys [v4 data typed? on-completed]}]
  (if typed?
    {::hash-typed-data
     {:data data
      :v4 v4
      :on-completed
      (or on-completed
          #(re-frame/dispatch
            [:signing.keycard.callback/hash-message-completed
             data typed? %]))}}
    {::hash-message
     {:message data
      :on-completed
      (or on-completed
          #(re-frame/dispatch
            [:signing.keycard.callback/hash-message-completed
             data typed? %]))}}))

(rf/defn hash-message-completed
  {:events [:signing.keycard.callback/hash-message-completed]}
  [{:keys [db]} data typed? result]
  (let [{:keys [result error]} (types/json->clj result)]
    (if error
      {:dispatch                 [:signing.ui/cancel-is-pressed]
       :effects.utils/show-popup {:title   (i18n/label :t/sign-request-failed)
                                  :content (:message error)}}
      {:db (update db
                   :keycard assoc
                   :hash    result
                   :typed?  typed?
                   :data    data)})))

(rf/defn hash-transaction
  [{:keys [db]}]
  (let [tx (prepare-transaction (:signing/tx db))]
    (log/debug "hash-transaction" tx)
    {::hash-transaction
     {:transaction  tx
      :on-completed #(re-frame/dispatch
                      [:signing.keycard.callback/hash-transaction-completed tx %])}}))

(rf/defn hash-transaction-completed
  {:events [:signing.keycard.callback/hash-transaction-completed]}
  [{:keys [db]} original-tx result]
  (let [{:keys [transaction hash]} (:result (types/json->clj result))]
    {:db (-> db
             (assoc-in [:keycard :transaction]
                       (merge original-tx transaction))
             (assoc-in [:keycard :hash] hash))}))

(rf/defn sign-with-keycard
  {:events [:signing.ui/sign-with-keycard-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [message maxPriorityFeePerGas maxFeePerGas]} (get db :signing/tx)]
    (rf/merge
     cofx
     {:db (-> db
              (assoc-in [:keycard :pin :enter-step] :sign)
              (assoc-in [:signing/sign :type] :keycard)
              (assoc-in [:signing/sign :keycard-step] :pin))}
     #(if message
        (hash-message % message)
        (hash-transaction %)))))

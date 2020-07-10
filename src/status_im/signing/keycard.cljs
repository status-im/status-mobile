(ns status-im.signing.keycard
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

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

(defn prepare-transaction
  [{:keys [gas gasPrice data nonce tx-obj]}]
  (let [{:keys [from to value chat-id message-id command?]} tx-obj]
    (cond-> {:from       from
             :to         to
             :value      value
             :gas        (str "0x" (abi-spec/number-to-hex gas))
             :gasPrice   (str "0x" (abi-spec/number-to-hex gasPrice))
             :chat-id    chat-id
             :message-id message-id
             :command?   command?}
      data
      (assoc :data data)
      nonce
      (assoc :nonce nonce))))

(fx/defn hash-message
  [_ {:keys [data typed? on-completed]}]
  (if typed?
    {::hash-typed-data
     {:data         data
      :on-completed
      (or on-completed
          #(re-frame/dispatch
            [:signing.keycard.callback/hash-message-completed
             data typed? %]))}}
    {::hash-message
     {:message      data
      :on-completed
      (or on-completed
          #(re-frame/dispatch
            [:signing.keycard.callback/hash-message-completed
             data typed? %]))}}))

(fx/defn hash-message-completed
  {:events [:signing.keycard.callback/hash-message-completed]}
  [{:keys [db]} data typed? result]
  (let [{:keys [result error]} (types/json->clj result)]
    {:db (update db :keycard assoc
                 :hash result
                 :typed? typed?
                 :data data)}))

(fx/defn hash-transaction
  [{:keys [db]}]
  (let [tx (prepare-transaction (:signing/tx db))]
    (log/debug "hash-transaction" tx)
    {::hash-transaction
     {:transaction  tx
      :on-completed #(re-frame/dispatch
                      [:signing.keycard.callback/hash-transaction-completed tx %])}}))

(fx/defn hash-transaction-completed
  {:events [:signing.keycard.callback/hash-transaction-completed]}
  [{:keys [db]} original-tx result]
  (let [{:keys [transaction hash]} (:result (types/json->clj result))]
    {:db (-> db
             (assoc-in [:keycard :transaction]
                       (merge original-tx transaction))
             (assoc-in [:keycard :hash] hash))}))

(fx/defn sign-with-keycard
  {:events [:signing.ui/sign-with-keycard-pressed]}
  [{:keys [db] :as cofx}]
  (let [message (get-in db [:signing/tx :message])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:keycard :pin :enter-step] :sign)
              (assoc-in [:signing/sign :type] :keycard)
              (assoc-in [:signing/sign :keycard-step] :pin))}
     #(if message
        (hash-message % message)
        (hash-transaction %)))))

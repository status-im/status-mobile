(ns ^{:doc "Definition of the StatusMessage protocol"}
 status-im.transport.message.core
  (:require [status-im.chat.models.message :as models.message]
            [status-im.chat.models :as models.chat]
            [status-im.contact.core :as models.contact]
            [status-im.pairing.core :as models.pairing]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.data-store.contacts :as data-store.contacts]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]))

(fx/defn handle-chats [cofx chats]
  (models.chat/ensure-chats cofx chats))

(fx/defn handle-contacts [cofx contacts]
  (models.contact/ensure-contacts cofx contacts))

(fx/defn handle-message [cofx message]
  (models.message/receive-one cofx message))

(fx/defn process-response [cofx ^js response-js]
  (let [^js chats (.-chats response-js)
        ^js contacts (.-contacts response-js)
        ^js installations (.-installations response-js)
        ^js messages (.-messages response-js)]
    (cond
      (seq installations)
      (let [installations-clj (types/js->clj installations)]
        (js-delete response-js "installations")
        (fx/merge cofx
                  {:utils/dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (models.pairing/handle-installations installations-clj)))

      (seq contacts)
      (let [contacts-clj (types/js->clj contacts)]
        (js-delete response-js "contacts")
        (fx/merge cofx
                  {:utils/dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (handle-contacts (map data-store.contacts/<-rpc contacts-clj))))

      (seq chats)
      (let [chats-clj (types/js->clj chats)]
        (js-delete response-js "chats")
        (fx/merge cofx
                  {:utils/dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (handle-chats (map #(-> %
                                          (data-store.chats/<-rpc)
                                          (dissoc :unviewed-messages-count))
                                     chats-clj))))

      (seq messages)
      (let [message (.pop messages)]
        (fx/merge cofx
                  {:utils/dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (handle-message (-> message (types/js->clj) (data-store.messages/<-rpc))))))))

(handlers/register-handler-fx
 ::process
 (fn [cofx [_ response-js]]
   (process-response cofx response-js)))

(fx/defn remove-hash
  [{:keys [db] :as cofx} envelope-hash]
  {:db (update db :transport/message-envelopes dissoc envelope-hash)})

(fx/defn check-confirmations
  [{:keys [db] :as cofx} status chat-id message-id]
  (when-let [{:keys [pending-confirmations not-sent]}
             (get-in db [:transport/message-ids->confirmations message-id])]
    (if (zero? (dec pending-confirmations))
      (fx/merge cofx
                {:db (update db
                             :transport/message-ids->confirmations
                             dissoc message-id)}
                (models.message/update-message-status chat-id
                                                      message-id
                                                      (if not-sent
                                                        :not-sent
                                                        status))
                (remove-hash message-id))
      (let [confirmations {:pending-confirmations (dec pending-confirmations)
                           :not-sent  (or not-sent
                                          (= :not-sent status))}]
        {:db (assoc-in db
                       [:transport/message-ids->confirmations message-id]
                       confirmations)}))))

(fx/defn update-envelope-status
  [{:keys [db] :as cofx} message-id status]
  (if-let [{:keys [chat-id]}
           (get-in db [:transport/message-envelopes message-id])]
    (when-let [{:keys [from]} (get-in db [:messages chat-id message-id])]
      (check-confirmations cofx status chat-id message-id))
    ;; We don't have a message-envelope for this, might be that the confirmation
    ;; came too early
    {:db (update-in db [:transport/message-confirmations message-id] conj status)}))

(fx/defn update-envelopes-status
  [{:keys [db] :as cofx} message-id status]
  (apply fx/merge cofx (map #(update-envelope-status % status) message-id)))

(fx/defn set-message-envelope-hash
  "message-type is used for tracking"
  [{:keys [db] :as cofx} chat-id message-id message-type messages-count]
  ;; Check first if the confirmation has already arrived
  (let [statuses (get-in db [:transport/message-confirmations message-id])
        check-confirmations-fx (map
                                #(check-confirmations % chat-id message-id)
                                statuses)

        add-envelope-data (fn [{:keys [db]}]
                            {:db (-> db
                                     (update :transport/message-confirmations dissoc message-id)
                                     (assoc-in [:transport/message-envelopes message-id]
                                               {:chat-id      chat-id
                                                :message-type message-type})
                                     (update-in [:transport/message-ids->confirmations message-id]
                                                #(or % {:pending-confirmations messages-count})))})]
    (apply fx/merge cofx (conj check-confirmations-fx add-envelope-data))))

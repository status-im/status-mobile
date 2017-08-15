(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.utils.clocks :as clocks]
            [status-im.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models.unviewed-messages :as unviewed-messages-model]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as msg-store]))

(re-frame/reg-cofx
 :pop-up-chat?
 (fn [cofx]
   (assoc cofx :pop-up-chat? (fn [chat-id]
                               (or (not (chat-store/exists? chat-id))
                                   (chat-store/is-active? chat-id))))))

(re-frame/reg-cofx
 :get-last-clock-value
 (fn [cofx]
   (assoc cofx :get-last-clock-value msg-store/get-last-clock-value)))

(re-frame/reg-cofx
 :current-timestamp
 (fn [cofx]
   ;; TODO (janherich) why is actual timestmap generation in random namespace ?
   (assoc cofx :current-timestamp (random/timestamp))))

(defn- get-current-identity
  [{:accounts/keys [accounts current-account-id]}]
  (get-in accounts [current-account-id :public-key]))

(defn- wallet-message
  [{:keys [content-type] :as message}]
  (log/debug "Wallet msg")
  (let [wallet-ct (if (= content-type const/content-type-command)
                    const/content-type-wallet-command
                    const/content-type-wallet-request)]
    (-> message
        (assoc :clock-value 0
               :chat-id const/wallet-chat-id
               :content-type wallet-ct)
        (dissoc :group-id))))

(defn add-message
  [{:keys [db get-stored-message get-last-stored-message pop-up-chat?
           get-last-clock-value current-timestamp random-id]}
   {:keys [from group-id chat-id content-type
           message-id timestamp clock-value]
    :as   message
    :or   {clock-value 0}}]
  (let [chat-identifier  (or group-id chat-id from)
        current-identity (get-current-identity db)]
    ;; proceed with adding message if message is not already stored in realm,
    ;; it's not from current user (outgoing message) and it's for relevant chat
    ;; (either current active chat or new chat not existing yet)
    (if (and (not (get-stored-message chat-identifier))
             (not= from current-identity)
             (pop-up-chat? chat-identifier))
      (let [group-chat?      (not (nil? group-id))
            enriched-message (assoc (chat-utils/check-author-direction
                                     (get-last-stored-message chat-identifier)
                                     message)
                                    :chat-id chat-identifier
                                    :timestamp (or timestamp current-timestamp)
                                    :clock-value (clocks/receive
                                                  clock-value
                                                  (get-last-clock-value chat-identifier)))]
        (cond-> {:db           (-> db
                                   (chat-utils/add-message-to-db chat-identifier chat-identifier enriched-message
                                                                 (:new? enriched-message))
                                   (unviewed-messages-model/add-unviewed-message chat-identifier message-id)
                                   (assoc-in [:chats chat-identifier :last-message] message))
                 :dispatch-n   [[:upsert-chat! {:chat-id    chat-identifier
                                                :group-chat group-chat?}]
                                [:request-command-message-data enriched-message :short-preview]]
                 :save-message (dissoc enriched-message :new?)}

          (get-in enriched-message [:content :command])
          (update :dispatch-n conj [:request-command-preview enriched-message])

          (= (:content-type enriched-message) const/content-type-command-request)
          (update :dispatch-n conj [:add-request chat-identifier enriched-message])
          ;; TODO(janherich) refactor this ugly special treatment of wallet send commands for logged in user
          (and (= (get-in message [:content :params :bot-db :public :recipient :whisper-identity])
                  current-identity)
               (= content-type const/content-type-command)
               (not= chat-identifier const/wallet-chat-id)
               (= "send" (get-in message [:content :command])))
          (update :dispatch-n conj [:received-message (wallet-message (assoc message :message-id random-id))])))
      {:db db})))

(def ^:private receive-interceptors
  [(re-frame/inject-cofx :get-stored-message) (re-frame/inject-cofx :get-last-stored-message)
   (re-frame/inject-cofx :pop-up-chat?) (re-frame/inject-cofx :get-last-clock-value)
   (re-frame/inject-cofx :current-timestamp) (re-frame/inject-cofx :random-id)
   re-frame/trim-v])

(handlers/register-handler-fx
  :received-protocol-message!
  receive-interceptors
  (fn [cofx [{:keys [from to payload]}]]
    (add-message cofx (merge payload
                             {:from    from
                              :to      to
                              :chat-id from}))))

(handlers/register-handler-fx
  :received-message
  receive-interceptors
  (fn [cofx [message]]
    (add-message cofx message)))

(handlers/register-handler-fx
  :received-message-when-commands-loaded
  receive-interceptors
  (fn [{:keys [db] :as cofx} [chat-id message]]
    (if (and (:status-node-started? db)
             (get-in db [:contacts/contacts chat-id :commands-loaded?]))
      (add-message cofx message)
      {:dispatch-later [{:ms 400 :dispatch [:received-message-when-commands-loaded chat-id message]}]})))

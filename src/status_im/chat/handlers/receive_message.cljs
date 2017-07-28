(ns status-im.chat.handlers.receive-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [re-frame.core :refer [enrich after debug dispatch path]]
            [status-im.data-store.messages :as messages]
            [status-im.chat.utils :as cu]
            [status-im.utils.random :as random]
            [status-im.constants :refer [wallet-chat-id
                                         content-type-command
                                         content-type-command-request]
             :as c]
            [cljs.reader :refer [read-string]]
            [status-im.data-store.chats :as chats]
            [status-im.utils.scheduler :as s]
            [taoensso.timbre :as log]))

(defn store-message [{chat-id :chat-id :as message}]
  (messages/save chat-id (dissoc message :new?)))

(defn get-current-identity
  [{:keys [current-account-id accounts]}]
  (:public-key (accounts current-account-id)))

(declare add-message-to-wallet)

(defn add-message
  [db {:keys [from group-id chat-id
              message-id timestamp clock-value show?]
       :as   message
       :or   {clock-value 0}}]
  (let [same-message     (messages/get-by-id message-id)
        current-identity (get-current-identity db)
        chat-id'         (or group-id chat-id from)
        exists?          (chats/exists? chat-id')
        active?          (chats/is-active? chat-id')
        chat-clock-value (messages/get-last-clock-value chat-id')
        clock-value      (if (zero? clock-value)
                           (inc chat-clock-value)
                           clock-value)]
    (when (and (not same-message)
               (not= from current-identity)
               (or (not exists?) active?))
      (let [group-chat?      (not (nil? group-id))
            previous-message (messages/get-last-message chat-id')
            message'         (assoc (cu/check-author-direction previous-message message)
                               :chat-id chat-id'
                               :timestamp (or timestamp (random/timestamp))
                               :clock-value clock-value)]
        (store-message message')
        (dispatch [:upsert-chat! {:chat-id    chat-id'
                                  :group-chat group-chat?}])
        (when (get-in message [:content :command])
          (dispatch [:request-command-preview  message]))
        (dispatch [::add-message chat-id' message'])
        (dispatch [::set-last-message message'])
        (when (= (:content-type message') content-type-command-request)
          (dispatch [:add-request chat-id' message']))
        (dispatch [:add-unviewed-message chat-id' message-id])
        (when-not show?
          (dispatch [:send-clock-value-request! message])))
      (if (and
            (= (:content-type message) content-type-command)
            (not= chat-id' wallet-chat-id)
            (= "send" (get-in message [:content :command])))
        (add-message-to-wallet db message)))))

(defn add-message-to-wallet [db {:keys [content-type] :as message}]
  (let [ct       (if (= content-type c/content-type-command)
                   c/content-type-wallet-command
                   c/content-type-wallet-request)
        message' (assoc message :clock-value 0
                                :message-id (random/id)
                                :chat-id wallet-chat-id
                                :content-type ct)]
    (add-message db message')))

(register-handler :received-protocol-message!
  (u/side-effect!
    (fn [_ [_ {:keys [from to payload]}]]
      (dispatch [:received-message (merge payload
                                          {:from    from
                                           :to      to
                                           :chat-id from})]))))

(register-handler :received-message
  (after #(dispatch [:update-suggestions]))
  (u/side-effect!
    (fn [db [_ message]]
      (add-message db message))))

(register-handler ::add-message
  (fn [db [_ add-to-chat-id {:keys [chat-id new?] :as message}]]
    (cu/add-message-to-db db add-to-chat-id chat-id message new?)))

(register-handler ::set-last-message
  (fn [{:keys [chats] :as db} [_ {:keys [chat-id] :as message}]]
    (dispatch [:request-command-data message :short-preview])
    (assoc-in db [:chats chat-id :last-message] message)))

(defn commands-loaded? [db chat-id]
  (get-in db [:contacts/contacts chat-id :commands-loaded?]))

(def timeout 400)

(register-handler :received-message-when-commands-loaded
  (u/side-effect!
    (fn [{:keys [status-node-started?] :as db} [_ chat-id message]]
      (if (and status-node-started? (commands-loaded? db chat-id))
        (dispatch [:received-message message])
        (s/execute-later
          #(dispatch [:received-message-when-commands-loaded chat-id message])
          timeout)))))


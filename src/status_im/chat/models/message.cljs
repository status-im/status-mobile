(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message-list :as message-list]
            [status-im.chat.models.message-content :as message-content]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.core :as ethereum]
            [status-im.mailserver.core :as mailserver]
            [status-im.native-module.core :as status]
            [status-im.transport.message.group-chat :as message.group-chat]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.ui.components.react :as react]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.datetime :as time]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn- prepare-message
  [{:keys [content content-type] :as message} chat-id current-chat?]
  (cond-> message
    current-chat?
    (assoc :seen true)

    (and (= constants/content-type-text content-type)
         (message-content/should-collapse?
          (:text content)
          (:line-count content)))
    (assoc :should-collapse? true)))

(defn system-message? [{:keys [message-type]}]
  (= constants/message-type-private-group-system-message message-type))

(defn build-desktop-notification
  [{:keys [db] :as cofx} {:keys [chat-id timestamp content from] :as message}]
  (let [{:keys [group-chat] :as chat} (get-in db [:chats chat-id])
        contact-name (get-in db [:contacts/contacts from :name]
                             (:name (contact.db/public-key->new-contact from)))
        chat-name        (if group-chat
                           (chat.db/group-chat-name chat)
                           contact-name)
        ;; contact name and chat-name are the same in 1-1 chats
        shown-chat-name   (when group-chat chat-name)
        timestamp'        (when-not (< (time/seconds-ago (time/to-date timestamp)) 15)
                            (str " @ " (time/to-short-str timestamp)))
        body-first-line   (when (or shown-chat-name timestamp')
                            (str shown-chat-name timestamp' ":\n"))]
    {:title       contact-name
     :body        (str body-first-line (:text content))
     :prioritary? (not (chat-model/multi-user-chat? cofx chat-id))}))

(fx/defn add-message
  [{:keys [db] :as cofx}
   {{:keys [chat-id message-id timestamp from] :as message} :message
    :keys [current-chat?]}]
  (let [current-public-key (multiaccounts.model/current-public-key cofx)
        prepared-message (prepare-message message chat-id current-chat?)]
    (when (and platform/desktop?
               (not= from current-public-key)
               (get-in db [:multiaccount :desktop-notifications?])
               (< (time/seconds-ago (time/to-date timestamp)) constants/one-earth-day))
      (let [{:keys [title body prioritary?]} (build-desktop-notification cofx message)]
        (.displayNotification react/desktop-notification title body prioritary?)))
    (fx/merge cofx
              {:db            (cond->
                               (-> db
                                   ;; We should not be always adding to the list, as it does not make sense
                                   ;; if the chat has not been initialized, but run into
                                   ;; some troubles disabling it, so next time
                                   (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                                   (update-in [:chats chat-id :message-list] message-list/add prepared-message))
                                (and (not current-chat?)
                                     (not= from current-public-key))
                                (update-in [:chats chat-id :loaded-unviewed-messages-ids]
                                           (fnil conj #{}) message-id))}
              (when (and platform/desktop?
                         (not (system-message? prepared-message)))

                (chat-model/update-dock-badge-label)))))

(fx/defn add-received-message
  [{:keys [db] :as cofx}
   {:keys [from message-id chat-id content] :as message}]
  (let [{:keys [current-chat-id view-id]} db
        current-chat?                  (and (or (= :chat view-id)
                                                (= :chat-modal view-id))
                                            (= current-chat-id chat-id))]
    (fx/merge cofx
              (add-message {:message      message
                            :current-chat? current-chat?}))))

(defn- add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id from]}]
  (let [{:keys [deleted-at-clock-value messages]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (>= deleted-at-clock-value clock-value)))))

(defn extract-chat-id [cofx {:keys [chat-id from message-type]}]
  "Validate and return a valid chat-id"
  (cond
    (and (= constants/message-type-private-group message-type)
         (and (get-in cofx [:db :chats chat-id :contacts from])
              (get-in cofx [:db :chats chat-id :members-joined (multiaccounts.model/current-public-key cofx)]))) chat-id
    (and (= constants/message-type-public-group message-type)
         (get-in cofx [:db :chats chat-id :public?])) chat-id
    (and (= constants/message-type-one-to-one message-type)
         (= (multiaccounts.model/current-public-key cofx) from)) chat-id
    (= constants/message-type-one-to-one message-type) from))

(fx/defn update-unviewed-count
  [{:keys [now db] :as cofx} {:keys [chat-id
                                     from
                                     message-id] :as message}]
  (let [{:keys [current-chat-id view-id]} db
        chat-view?         (or (= :chat view-id)
                               (= :chat-modal view-id))
        current-count (get-in db [:chats chat-id :unviewed-messages-count])]
    (cond
      (= from (multiaccounts.model/current-public-key cofx))
      ;; nothing to do
      nil

      (and chat-view? (= current-chat-id chat-id))
      (fx/merge cofx
                (data-store.messages/mark-messages-seen current-chat-id [message-id]))

      :else
      {:db (update-in db [:chats chat-id]
                      assoc
                      :unviewed-messages-count (inc current-count))})))

(fx/defn receive-one
  [cofx message]
  (when-let [chat-id (extract-chat-id cofx message)]
    (let [message-with-chat-id (assoc message :chat-id chat-id)]
      (when (add-to-chat? cofx message-with-chat-id)
        (fx/merge cofx
                  (add-received-message message-with-chat-id)
                  (update-unviewed-count message-with-chat-id)
                  (chat-model/join-time-messages-checked chat-id)
                  (when platform/desktop?
                    (chat-model/update-dock-badge-label)))))))

;;;; Send message

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp :name])

(fx/defn update-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (fx/merge cofx
            {:db (assoc-in db
                           [:chats chat-id :messages message-id :outgoing-status]
                           status)}
            (data-store.messages/update-outgoing-status message-id status)))

(fx/defn resend-message
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {::json-rpc/call [{:method "shhext_reSendChatMessage"
                               :params [message-id]
                               :on-success #(log/debug "re-sent message successfully")
                               :on-error #(log/error "failed to re-send message" %)}]}
            (update-message-status chat-id message-id :sending)))

(fx/defn rebuild-message-list
  [{:keys [db]} chat-id]
  {:db (assoc-in db [:chats chat-id :message-list]
                 (message-list/add-many nil (vals (get-in db [:chats chat-id :messages]))))})

(fx/defn delete-message
  "Deletes chat message, rebuild message-list"
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {:db            (update-in db [:chats chat-id :messages] dissoc message-id)}
            (data-store.messages/delete-message message-id)
            (rebuild-message-list chat-id)))

(fx/defn handle-saved-system-messages
  {:events [:messages/system-messages-saved]}
  [cofx messages]
  (apply fx/merge cofx (map #(add-message {:message       %
                                           :current-chat? true})
                            messages)))

(fx/defn add-system-messages [cofx messages]
  (data-store.messages/save-system-messages cofx messages))

(fx/defn send-message
  [{:keys [db now] :as cofx} {:keys [chat-id] :as message}]
  (let [{:keys [chats]}  db
        message-data                        (-> message
                                                (tribute-to-talk/add-transaction-hash db))]
    (protocol/send-chat-message cofx message-data)))

(fx/defn toggle-expand-message
  [{:keys [db]} chat-id message-id]
  {:db (update-in db [:chats chat-id :messages message-id :expanded?] not)})

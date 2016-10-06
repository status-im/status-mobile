(ns status-im.protocol.handlers
  (:require [status-im.utils.handlers :as u]
            [re-frame.core :refer [dispatch after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.data-store.chats :as chats]
            [status-im.protocol.core :as protocol]
            [status-im.constants :refer [text-content-type
                                         blocks-per-hour]]
            [status-im.i18n :refer [label]]
            [status-im.utils.random :as random]
            [taoensso.timbre :as log :refer-macros [debug]]))

(register-handler :initialize-protocol
  (fn [db [_ current-account-id]]
    (let [{:keys [public-key status updates-public-key
                  updates-private-key]}
          (get-in db [:accounts current-account-id])]
      (let [groups (chats/get-active-group-chats)
            w3     (protocol/init-whisper!
                     {:rpc-url                     "http://localhost:8545"
                      :identity                    public-key
                      :groups                      groups
                      :callback                    #(dispatch [:incoming-message %1 %2])
                      :ack-not-received-s-interval 125
                      :default-ttl                 120
                      :send-online-s-interval      180
                      :ttl                         {}
                      :max-attempts-number         3
                      :delivery-loop-ms-interval   500
                      :profile-keypair             {:public  updates-public-key
                                                    :private updates-private-key}
                      :hashtags                    (u/get-hashtags status)
                      :pending-messages            (pending-messages/get-all)
                      :contacts                    (keep (fn [{:keys [whisper-identity
                                                                      public-key
                                                                      private-key]}]
                                                           (when (and public-key private-key)
                                                             {:identity whisper-identity
                                                              :keypair  {:public  public-key
                                                                         :private private-key}}))
                                                         (contacts/get-all))})]
        (assoc db :web3 w3)))))

(register-handler :update-sync-state
  (u/side-effect!
    (fn [{:keys [sync-state]} [_ error sync]]
      (let [{:keys [highestBlock currentBlock]} (js->clj sync :keywordize-keys true)
            syncing?  (> (- highestBlock currentBlock) blocks-per-hour)
            new-state (cond
                        error :offline
                        syncing? (if (= sync-state :done)
                                   :pending
                                   :in-progress)
                        :else (if (or (= sync-state :done)
                                      (= sync-state :pending))
                                :done
                                :synced))]
        (when (not= sync-state new-state)
          (dispatch [:set :sync-state new-state]))))))

(register-handler :initialize-sync-listener
  (fn [{:keys [web3 sync-listener] :as db} _]
    (when sync-listener
      (.stopWatching sync-listener))
    (->> (.isSyncing (.-eth web3)
                     (fn [error sync]
                       (dispatch [:update-sync-state error sync])))
         (assoc db :sync-listener))))

(register-handler :incoming-message
  (u/side-effect!
    (fn [_ [_ type {:keys [payload] :as message}]]
      (debug :incoming-message type)
      (case type
        :message (dispatch [:received-protocol-message! message])
        :group-message (dispatch [:received-protocol-message! message])
        :ack (if (#{:message :group-message} (:type payload))
               (dispatch [:message-delivered message])
               (dispatch [:pending-message-remove message]))
        :seen (dispatch [:message-seen message])
        :group-invitation (dispatch [:group-chat-invite-received message])
        :add-group-identity (dispatch [:participant-invited-to-group message])
        :leave-group (dispatch [:participant-left-group message])
        :contact-request (dispatch [:contact-request-received message])
        :discovery (dispatch [:discovery-response-received message])
        :profile (dispatch [:contact-update-received message])
        :online (dispatch [:contact-online-received message])
        :pending (dispatch [:pending-message-upsert message])
        :sent (let [{:keys [to id group-id]} message
                    message' {:from    to
                              :payload {:message-id id
                                        :group-id   group-id}}]
                (dispatch [:message-sent message']))
        (debug "Unknown message type" type)))))

(defn system-message [message-id content]
  {:from         "system"
   :message-id   message-id
   :content      content
   :content-type text-content-type})

(defn joined-chat-message [chat-id from message-id]
  (let [contact-name (:name (contacts/get-by-id from))]
    (messages/save chat-id {:from         "system"
                            :message-id   (str message-id "_" from)
                            :content      (str (or contact-name from) " " (label :t/received-invitation))
                            :content-type text-content-type})))

(defn participant-invited-to-group-message [chat-id current-identity identity from message-id]
  (let [inviter-name (:name (contacts/get-by-id from))
        invitee-name (if (= identity current-identity)
                       (label :t/You)
                       (:name (contacts/get-by-id identity)))]
    (messages/save chat-id {:from         "system"
                            :message-id   message-id
                            :content      (str (or inviter-name from) " " (label :t/invited) " " (or invitee-name identity))
                            :content-type text-content-type})))

(defn participant-removed-from-group-message [chat-id identity from message-id]
  (let [remover-name (:name (contacts/get-by-id from))
        removed-name (:name (contacts/get-by-id identity))]
    (->> (str (or remover-name from) " " (label :t/removed) " " (or removed-name identity))
         (system-message message-id)
         (messages/save chat-id))))

(defn you-removed-from-group-message [chat-id from message-id]
  (let [remover-name (:name (contacts/get-by-id from))]
    (->> (str (or remover-name from) " " (label :t/removed-from-chat))
         (system-message message-id)
         (messages/save chat-id))))

(defn participant-left-group-message [chat-id from message-id]
  (let [left-name (:name (contacts/get-by-id from))]
    (->> (str (or left-name from) " " (label :t/left))
         (system-message message-id)
         (messages/save chat-id))))

(register-handler :group-chat-invite-acked
  (u/side-effect!
    (fn [_ [action from group-id ack-message-id]]
      (log/debug action from group-id ack-message-id)
      #_(joined-chat-message group-id from ack-message-id))))

(register-handler :participant-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id identity message-id]]
      (log/debug action message-id from group-id identity)
      (chats/remove-contacts group-id [identity])
      (participant-removed-from-group-message group-id identity from message-id))))

(register-handler :you-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id message-id]]
      (log/debug action message-id from group-id)
      (you-removed-from-group-message group-id from message-id)
      (chats/set-active group-id false))))

(register-handler :participant-left-group
  (u/side-effect!
    (fn [{:keys [current-public-key]}
         [_ {:keys                                   [from]
             {:keys [group-id message-id timestamp]} :payload}]]
      (when (and (not= current-public-key from)
                 (chats/is-active? group-id)
                 (> timestamp (chats/get-property group-id :timestamp)))
        (participant-left-group-message group-id from message-id)
        (dispatch [::remove-identity-from-chat group-id from])
        (dispatch [::remove-identity-from-chat! group-id from])))))

(register-handler ::remove-identity-from-chat
  (fn [db [_ chat-id id]]
    (update-in db [:chats chat-id :contacts]
               #(remove (fn [{:keys [identity]}]
                          (= identity id)) %))))

(register-handler ::remove-identity-from-chat!
  (u/side-effect!
    (fn [_ [_ group-id identity]]
      (chats/remove-contacts group-id [identity]))))

(register-handler :participant-invited-to-group
  (u/side-effect!
    (fn [{:keys [current-public-key]}
         [_ {:keys                                   [from]
             {:keys [group-id identity message-id]} :payload}]]
      (participant-invited-to-group-message group-id current-public-key identity from message-id)
      (when-not (= current-public-key identity)
        (dispatch [:add-contact-to-group! group-id identity])))))

(register-handler :add-contact-to-group!
  (u/side-effect!
    (fn [_ [_ group-id identity]]
      (when-not (chats/has-contact? group-id identity)
        (dispatch [::add-contact group-id identity])
        (dispatch [::store-contact! group-id identity])))))

(register-handler ::add-contact
  (fn [db [_ group-id identity]]
    (update-in db [:chats group-id :contacts] conj {:identity identity})))

(register-handler ::store-contact!
  (u/side-effect!
    (fn [_ [_ group-id identity]]
      (chats/add-contacts group-id [identity]))))

(defn save-message-status! [status]
  (fn [_ [_
          {:keys                                        [from]
           {:keys [message-id ack-of-message group-id]} :payload}]]
    (let [message-id' (or ack-of-message message-id)]
      (when-let [{:keys [message-status] :as message} (messages/get-by-id message-id')]
        (when-not (= (keyword message-status) :seen)
          (let [group?  (boolean group-id)
                message (if (and group? (not= status :sent))
                          (update-in message
                                     [:user-statuses from]
                                     (fn [{old-status :status}]
                                       {:id               (random/id)
                                        :whisper-identity from
                                        :status           (if (= (keyword old-status) :seen)
                                                            old-status
                                                            status)}))
                          (assoc message :message-status status))]
            (messages/update message)))))))


(defn update-message-status [status]
  (fn [db
       [_ {:keys                                        [from]
           {:keys [message-id ack-of-message group-id]} :payload}]]
    (if (chats/is-active? (or group-id from))
      (let [message-id'    (or ack-of-message message-id)
            group?         (boolean group-id)
            status-path    (if (and group? (not= status :sent))
                             [:message-user-statuses message-id' from]
                             [:message-statuses message-id'])
            {current-status :status} (get-in db status-path)]
        (if-not (= :seen current-status)
          (assoc-in db status-path {:whisper-identity from
                                    :status           status})
          db))
      db)))

(defn remove-pending-message
  [_ [_ message]]
  (dispatch [:pending-message-remove message]))

(register-handler :message-delivered
  [(after (save-message-status! :delivered))
   (after remove-pending-message)]
  (update-message-status :delivered))

(register-handler :message-failed
  (after (save-message-status! :failed))
  (update-message-status :failed))

(register-handler :message-sent
  (after (save-message-status! :sent))
  (update-message-status :sent))

(register-handler :message-seen
  [(after (save-message-status! :seen))]
  (update-message-status :seen))

(register-handler :pending-message-upsert
  (after
    (fn [_ [_ {:keys [type id] :as pending-message}]]
      (pending-messages/save pending-message)
      (when (#{:message :group-message} type)
        (messages/update {:message-id      id
                          :delivery-status :pending}))))
  (fn [db [_ {:keys [type id to group-id]}]]
    (if (#{:message :group-message} type)
      (let [chat-id        (or group-id to)
            current-status (get-in db [:message-status chat-id id])]
        (if-not (= :seen current-status)
          (assoc-in db [:message-status chat-id id] :pending)
          db))
      db)))

(register-handler :pending-message-remove
  (u/side-effect!
    (fn [_ [_ message]]
      (pending-messages/delete message))))

(register-handler :contact-request-received
  (u/side-effect!
    (fn [_ [_ {:keys [from payload]}]]
      (when from
        (let [{{:keys [name profile-image address]} :contact
               {:keys [public private]}             :keypair} payload

              contact {:whisper-identity from
                       :public-key       public
                       :private-key      private
                       :address          address
                       :photo-path       profile-image
                       :name             name
                       :pending          true}]
          (dispatch [:watch-contact contact])
          (dispatch [:add-contacts [contact]]))))))

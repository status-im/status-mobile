(ns status-im.protocol.handlers
  (:require [status-im.utils.handlers :as u]
            [re-frame.core :refer [dispatch after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.data-store.processed-messages :as processed-messages]
            [status-im.data-store.chats :as chats]
            [status-im.protocol.core :as protocol]
            [status-im.constants :refer [text-content-type
                                         blocks-per-hour]]
            [status-im.i18n :refer [label]]
            [status-im.utils.random :as random]
            [status-im.protocol.message-cache :as cache]
            [status-im.utils.datetime :as dt]
            [taoensso.timbre :as log :refer-macros [debug]]
            [status-im.constants :as c]
            [status-im.components.status :as status]
            [clojure.string :refer [join]]))

(register-handler :initialize-protocol
  (fn [db [_ current-account-id]]
    (let [{:keys [public-key status updates-public-key
                  updates-private-key]}
          (get-in db [:accounts current-account-id])]
      (if public-key
        (let [groups (chats/get-active-group-chats)
              w3     (protocol/init-whisper!
                       {:rpc-url                     c/ethereum-rpc-url
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
                                                           (contacts/get-all))
                        :post-error-callback         #(dispatch [::post-error %])})]
          (assoc db :web3 w3))
        db))))

(register-handler :update-sync-state
  (u/side-effect!
    (fn [{:keys [sync-state sync-data]} [_ error sync]]
      (let [{:keys [highestBlock currentBlock] :as state}
            (js->clj sync :keywordize-keys true)
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
        (when (and (not= sync-data state) (= :in-progress new-state))
          (dispatch [:set :sync-data state]))
        (when (not= sync-state new-state)
          (dispatch [:set :sync-state new-state]))))))

(register-handler :initialize-sync-listener
  (fn [{:keys [web3 sync-listener] :as db} _]
    (if-not sync-listener
      (let [sync-listener (.isSyncing
                            (.-eth web3)
                            (fn [error sync]
                              (dispatch [:update-sync-state error sync])))]
        (assoc db :sync-listener sync-listener))
      db)))

(register-handler :incoming-message
  (u/side-effect!
    (fn [_ [_ type {:keys [payload ttl id] :as message}]]
      (let [message-id (or id (:message-id payload))]
        (when-not (cache/exists? message-id type)
          (let [ttl-s             (* 1000 (or ttl 120))
                processed-message {:id         (random/id)
                                   :message-id message-id
                                   :type       type
                                   :ttl        (+ (dt/now-ms) ttl-s)}]
            (cache/add! processed-message)
            (processed-messages/save processed-message))
          (case type
            :message (dispatch [:received-protocol-message! message])
            :group-message (dispatch [:received-protocol-message! message])
            :ack (if (#{:message :group-message} (:type payload))
                   (dispatch [:message-delivered message])
                   (dispatch [:pending-message-remove message]))
            :seen (dispatch [:message-seen message])
            :clock-value-request (dispatch [:message-clock-value-request message])
            :clock-value (dispatch [:message-clock-value message])
            :group-invitation (dispatch [:group-chat-invite-received message])
            :update-group (dispatch [:update-group-message message])
            :add-group-identity (dispatch [:participant-invited-to-group message])
            :remove-group-identity (dispatch [:participant-removed-from-group message])
            :leave-group (dispatch [:participant-left-group message])
            :contact-request (dispatch [:contact-request-received message])
            :discover (dispatch [:status-received message])
            :discoveries-request (dispatch [:discoveries-request-received message])
            :discoveries-response (dispatch [:discoveries-response-received message])
            :profile (dispatch [:contact-update-received message])
            :update-keys (dispatch [:update-keys-received message])
            :online (dispatch [:contact-online-received message])
            :pending (dispatch [:pending-message-upsert message])
            :sent (let [{:keys [to id group-id]} message
                        message' {:from    to
                                  :payload {:message-id id
                                            :group-id   group-id}}]
                    (dispatch [:message-sent message']))
            (debug "Unknown message type" type)))))))

(defn system-message
  ([message-id timestamp content]
   {:from         "system"
    :message-id   message-id
    :timestamp    timestamp
    :content      content
    :content-type text-content-type}))

(defn joined-chat-message [chat-id from message-id]
  (let [contact-name (:name (contacts/get-by-id from))]
    (messages/save chat-id {:from         "system"
                            :message-id   (str message-id "_" from)
                            :content      (str (or contact-name from) " " (label :t/received-invitation))
                            :content-type text-content-type})))

(defn participant-invited-to-group-message [chat-id current-identity identity from message-id timestamp]
  (let [inviter-name (:name (contacts/get-by-id from))
        invitee-name (if (= identity current-identity)
                       (label :t/You)
                       (:name (contacts/get-by-id identity)))]
    {:from         "system"
     :group-id     chat-id
     :timestamp    timestamp
     :message-id   message-id
     :content      (str (or inviter-name from) " " (label :t/invited) " " (or invitee-name identity))
     :content-type text-content-type}))

(defn participant-removed-from-group-message
  [identity from {:keys [message-id timestamp]}]
  (let [remover-name (:name (contacts/get-by-id from))
        removed-name (:name (contacts/get-by-id identity))]
    (->> [(or remover-name from) (label :t/removed) (or removed-name identity)]
         (join " ")
         (system-message message-id timestamp))))

(defn you-removed-from-group-message
  [from {:keys [message-id timestamp]}]
  (let [remover-name (:name (contacts/get-by-id from))]
    (->> [(or remover-name from) (label :t/removed-from-chat)]
         (join " ")
         (system-message message-id timestamp))))

(defn participant-left-group-message
  [chat-id from {:keys [message-id timestamp]}]
  (let [left-name (:name (contacts/get-by-id from))]
    (->> (str (or left-name from) " " (label :t/left))
         (system-message message-id timestamp)
         (messages/save chat-id))))

(register-handler :group-chat-invite-acked
  (u/side-effect!
    (fn [_ [action from group-id ack-message-id]]
      (log/debug action from group-id ack-message-id)
      #_(joined-chat-message group-id from ack-message-id))))

(register-handler :participant-removed-from-group
  (u/side-effect!
    (fn [{:keys [current-public-key chats]}
         [_ {:keys                                              [from]
             {:keys [group-id identity message-id] :as payload} :payload
             :as                                                message}]]
      (when-not (messages/get-by-id message-id)
        (let [admin (get-in chats [group-id :group-admin])]
          (when (= admin from)
            (if (= current-public-key identity)
              (dispatch [::you-removed-from-group message])
              (let [message
                    (assoc
                      (participant-removed-from-group-message identity from payload)
                      :group-id group-id)]
                (chats/remove-contacts group-id [identity])
                (dispatch [:received-message message])))))))))

(register-handler ::you-removed-from-group
  (u/side-effect!
    (fn [{:keys [web3]}
         [_ {:keys                                    [from]
             {:keys [group-id timestamp] :as payload} :payload}]]
      (when (chats/new-update? timestamp group-id)
        (let [message  (you-removed-from-group-message from payload)
              message' (assoc message :group-id group-id)]
          (dispatch [:received-message message']))
        (protocol/stop-watching-group! {:web3     web3
                                        :group-id group-id})
        (dispatch [:update-chat! {:chat-id         group-id
                                  :removed-from-at timestamp
                                  :is-active       false}])))))

(register-handler :participant-left-group
  (u/side-effect!
    (fn [{:keys [current-public-key]}
         [_ {:keys                                    [from]
             {:keys [group-id timestamp] :as payload} :payload}]]
      (when (and (not= current-public-key from)
                 (chats/is-active? group-id)
                 (> timestamp (chats/get-property group-id :timestamp)))
        (participant-left-group-message group-id from payload)
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
    (fn [{:keys [current-public-key chats]}
         [_ {:keys                                            [from]
             {:keys [group-id identity message-id timestamp]} :payload}]]
      (let [admin (get-in chats [group-id :group-admin])]
        (when (= from admin)
          (dispatch
            [:received-message
             (participant-invited-to-group-message group-id current-public-key identity from message-id timestamp)])
          (when-not (= current-public-key identity)
            (dispatch [:add-contact-to-group! group-id identity])))))))

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

(defn save-message-clock-value!
  [{:keys [message-extras]}
   [_ {{:keys [message-id clock-value]} :payload}]]
  (when-let [{old-clock-value :clock-value
              :as             message} (merge (messages/get-by-id message-id)
                                              (get message-extras message-id))]
    (if (>= clock-value old-clock-value)
      (messages/update (assoc message :clock-value clock-value :show? true)))))

(defn update-message-status [status]
  (fn [db
       [_ {:keys                                        [from]
           {:keys [message-id ack-of-message group-id]} :payload}]]
    (if (chats/is-active? (or group-id from))
      (let [message-id' (or ack-of-message message-id)
            group?      (boolean group-id)
            status-path (if (and group? (not= status :sent))
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

(register-handler :message-clock-value-request
  (u/side-effect!
    (fn [_ [_ {:keys [from] {:keys [message-id]} :payload}]]
      (let [{:keys [chat-id]} (messages/get-by-id message-id)
            message-overhead (chats/get-message-overhead chat-id)
            last-clock-value (messages/get-last-clock-value chat-id)]
        (if (pos? message-overhead)
          (let [last-outgoing (->> (messages/get-last-outgoing chat-id message-overhead)
                                   (reverse)
                                   (map-indexed vector))]
            (chats/reset-message-overhead chat-id)
            (doseq [[i message] last-outgoing]
              (dispatch [:update-clock-value! from i message (+ last-clock-value 100)])))
          (dispatch [:send-clock-value! from message-id]))))))

(register-handler :message-clock-value
  (after save-message-clock-value!)
  (fn [{:keys [message-extras] :as db}
       [_ {{:keys [message-id clock-value]} :payload}]]
    (if-let [{old-clock-value :clock-value} (merge (messages/get-by-id message-id)
                                                   (get message-extras message-id))]
      (if (> clock-value old-clock-value)
        (assoc-in db [:message-extras message-id] {:clock-value clock-value
                                                   :show?       true})
        db)
      db)))

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
    (fn [{:keys [contacts]} [_ {:keys [from payload]}]]
      (when from
        (let [{{:keys [name profile-image address status]} :contact
               {:keys [public private]}                    :keypair} payload
              existing-contact (get contacts from)
              contact          {:whisper-identity from
                                :public-key       public
                                :private-key      private
                                :address          address
                                :status           status
                                :photo-path       profile-image
                                :name             name}
              chat             {:name         name
                                :chat-id      from
                                :contact-info (prn-str contact)}]
          (if-not existing-contact
            (let [contact (assoc contact :pending? true)]
              (dispatch [:add-contacts [contact]])
              (dispatch [:add-chat from chat]))
            (when-not (:pending? existing-contact)
              (dispatch [:update-contact! contact])
              (dispatch [:watch-contact contact]))))))))

(register-handler ::post-error
  (u/side-effect!
    (fn [_ [_ error]]
      (.log js/console error)
      (let [message (.-message error)
            ios-error? (re-find (re-pattern "Could not connect to the server.") message)
            android-error? (re-find (re-pattern "Failed to connect") message)]
        (when (or ios-error? android-error?)
          (when android-error? (status/init-jail))
          (status/restart-rpc)
          (dispatch [:load-commands!]))))))

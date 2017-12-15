(ns status-im.protocol.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.data-store.processed-messages :as processed-messages]
            [status-im.data-store.chats :as chats]
            [status-im.protocol.core :as protocol]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.random :as random]
            [status-im.protocol.message-cache :as cache]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log :refer-macros [debug]]
            [status-im.native-module.core :as status]
            [clojure.string :as string]
            [status-im.utils.web3-provider :as web3-provider]))

;;;; COFX

(re-frame/reg-cofx
  ::get-web3
  (fn [coeffects _]
    (assoc coeffects :web3 (web3-provider/make-web3))))

(re-frame/reg-cofx
  ::get-chat-groups
  (fn [coeffects _]
    (assoc coeffects :groups (chats/get-active-group-chats))))

(re-frame/reg-cofx
  ::get-pending-messages
  (fn [coeffects _]
    (assoc coeffects :pending-messages (pending-messages/get-all))))

(re-frame/reg-cofx
  ::get-all-contacts
  (fn [coeffects _]
    (assoc coeffects :contacts (contacts/get-all))))

(re-frame/reg-cofx
  ::message-get-by-id
  (fn [coeffects _]
    (let [[{{:keys [message-id]} :payload}] (:event coeffects)]
      (assoc coeffects :message-by-id (messages/get-by-id message-id)))))

(re-frame/reg-cofx
  ::chats-new-update?
  (fn [coeffects _]
    (let [[{{:keys [group-id timestamp]} :payload}] (:event coeffects)]
      (assoc coeffects :new-update? (chats/new-update? timestamp group-id)))))

(re-frame/reg-cofx
  ::chats-is-active-and-timestamp
  (fn [coeffects _]
    (let [[{{:keys [group-id timestamp]} :payload}] (:event coeffects)]
      (assoc coeffects :chats-is-active-and-timestamp
                       (and (chats/is-active? group-id)
                            (> timestamp (chats/get-property group-id :timestamp)))))))

(re-frame/reg-cofx
  ::chats-is-active?
  (fn [coeffects _]
    (let [[{{:keys [group-id]} :payload from :from}] (:event coeffects)]
      (assoc coeffects :chats-is-active?
                       (chats/is-active? (or group-id from))))))

(re-frame/reg-cofx
  ::has-contact?
  (fn [coeffects _]
    (let [[{{:keys [group-id identity]} :payload}] (:event coeffects)]
      (assoc coeffects :has-contact? (chats/has-contact? group-id identity)))))


;;;; FX

(re-frame/reg-fx
  :stop-whisper
  (fn [] (protocol/stop-whisper!)))

(re-frame/reg-fx
  ::init-whisper
  (fn [{:keys [web3 public-key groups updates-public-key updates-private-key status contacts pending-messages]}]
    (protocol/init-whisper!
      {:web3                        web3
       :identity                    public-key
       :groups                      groups
       :callback                    #(re-frame/dispatch [:incoming-message %1 %2])
       :ack-not-received-s-interval 125
       ;; XXX(oskarth): For offline inbox MVP QA only, normally 120
       :default-ttl                 60
       :send-online-s-interval      180
       :ttl-config                  {:public-group-message 2400}
       ;; XXX(oskarth): For offline inbox MVP QA only, normally 3
       :max-attempts-number         1
       :delivery-loop-ms-interval   500
       :profile-keypair             {:public  updates-public-key
                                     :private updates-private-key}
       :hashtags                    (handlers/get-hashtags status)
       :pending-messages            pending-messages
       :contacts                    (keep (fn [{:keys [whisper-identity
                                                       public-key
                                                       private-key]}]
                                            (when (and public-key private-key)
                                              {:identity whisper-identity
                                               :keypair  {:public  public-key
                                                          :private private-key}}))
                                          contacts)
       :post-error-callback         #(re-frame/dispatch [::post-error %])})))

(re-frame/reg-fx
  ::web3-get-syncing
  (fn [web3]
    (when web3
      (.getSyncing
        (.-eth web3)
        (fn [error sync]
          (re-frame/dispatch [:update-sync-state error sync]))))))

(re-frame/reg-fx
  ::save-processed-messages
  (fn [processed-message]
    (processed-messages/save processed-message)))

(defn system-message
  ([message-id timestamp content]
   {:from         "system"
    :message-id   message-id
    :timestamp    timestamp
    :content      content
    :content-type constants/text-content-type}))

(re-frame/reg-fx
  ::participant-removed-from-group-message
  (fn [{:keys [identity from message-id timestamp group-id]}]
    (let [remover-name (:name (contacts/get-by-id from))
          removed-name (:name (contacts/get-by-id identity))
          message (->> [(or remover-name from) (i18n/label :t/removed) (or removed-name identity)]
                       (string/join " ")
                       (system-message message-id timestamp))
          message' (assoc message :group-id group-id)]
      (re-frame/dispatch [:received-message message']))))

(re-frame/reg-fx
  ::chats-add-contact
  (fn [[group-id identity]]
    (chats/add-contacts group-id [identity])))

(re-frame/reg-fx
  ::chats-remove-contact
  (fn [[group-id identity]]
    (chats/remove-contacts group-id [identity])))

(defn you-removed-from-group-message
  [from {:keys [message-id timestamp]}]
  (let [remover-name (:name (contacts/get-by-id from))]
    (->> [(or remover-name from) (i18n/label :t/removed-from-chat)]
         (string/join " ")
         (system-message message-id timestamp))))

(re-frame/reg-fx
  ::you-removed-from-group-message
  (fn [{:keys [from message-id timestamp group-id]}]
    (let [remover-name (:name (contacts/get-by-id from))
          message  (->> [(or remover-name from) (i18n/label :t/removed-from-chat)]
                        (string/join " ")
                        (system-message message-id timestamp))
          message' (assoc message :group-id group-id)]
      (re-frame/dispatch [:received-message message']))))

(re-frame/reg-fx
  ::stop-watching-group!
  (fn [params]
    (protocol/stop-watching-group! params)))

(re-frame/reg-fx
  ::participant-left-group-message
  (fn [{:keys [chat-id from message-id timestamp]}]
    (let [left-name (:name (contacts/get-by-id from))]
      (->> (str (or left-name from) " " (i18n/label :t/left))
           (system-message message-id timestamp)
           (messages/save chat-id)))))

(re-frame/reg-fx
  ::participant-invited-to-group-message
  (fn [{:keys [chat-id current-identity identity from message-id timestamp]}]
    (let [inviter-name (:name (contacts/get-by-id from))
          invitee-name (if (= identity current-identity)
                         (i18n/label :t/You)
                         (:name (contacts/get-by-id identity)))]
      (re-frame/dispatch
        [:received-message
         {:from         "system"
          :group-id     chat-id
          :timestamp    timestamp
          :message-id   message-id
          :content      (str (or inviter-name from) " " (i18n/label :t/invited) " " (or invitee-name identity))
          :content-type constants/text-content-type}]))))

(re-frame/reg-fx
  ::save-message-status!
  (fn [{:keys [message-id ack-of-message group-id from status]}]
    (let [message-id' (or ack-of-message message-id)]
      (when-let [{:keys [message-status] :as message} (messages/get-by-id message-id')]
        (when-not (= (keyword message-status) :seen)
          (let [group?   (boolean group-id)
                message' (-> (if (and group? (not= status :sent))
                               (update-in message
                                          [:user-statuses from]
                                          (fn [{old-status :status}]
                                            {:id               (random/id)
                                             :whisper-identity from
                                             :status           (if (= (keyword old-status) :seen)
                                                                 old-status
                                                                 status)}))
                               (assoc message :message-status status))
                             ;; we need to dissoc preview because it has been saved before
                             (dissoc :preview))]
            (messages/update-message message')))))))

(re-frame/reg-fx
  ::pending-messages-delete
  (fn [message]
    (pending-messages/delete message)))

(re-frame/reg-fx
  ::pending-messages-save
  (fn [{:keys [type id pending-message]}]
    (pending-messages/save pending-message)
    (when (#{:message :group-message} type)
      (messages/update-message {:message-id id
                                :delivery-status    :pending}))))

(re-frame/reg-fx
  ::status-init-jail
  (fn []
    (status/init-jail)))

(re-frame/reg-fx
  ::load-processed-messages!
  (fn []
    (let [now      (datetime/now-ms)
          messages (processed-messages/get-filtered (str "ttl > " now))]
      (cache/init! messages)
      (processed-messages/delete (str "ttl <=" now)))))


;;;; Handlers


;;; INITIALIZE PROTOCOL
(handlers/register-handler-fx
  :initialize-protocol
  [re-frame/trim-v
   (re-frame/inject-cofx ::get-web3)
   (re-frame/inject-cofx ::get-chat-groups)
   (re-frame/inject-cofx ::get-pending-messages)
   (re-frame/inject-cofx ::get-all-contacts)]
  (fn [{:keys [db web3 groups contacts pending-messages]} [current-account-id ethereum-rpc-url]]
    (let [{:keys [public-key status updates-public-key
                  updates-private-key]}
          (get-in db [:accounts/accounts current-account-id])]
      (when public-key
        {::init-whisper {:web3 web3 :public-key public-key :groups groups :pending-messages pending-messages
                         :updates-public-key updates-public-key :updates-private-key updates-private-key
                         :status status :contacts contacts}
         :db (assoc db :web3 web3
                       :rpc-url (or ethereum-rpc-url constants/ethereum-rpc-url))}))))

(handlers/register-handler-fx
  :load-processed-messages
  (fn [_ _]
    {::load-processed-messages! nil}))

;;; NODE SYNC STATE

(handlers/register-handler-db
  :update-sync-state
  (fn [{:keys [sync-state sync-data] :as db} [_ error sync]]
    (let [{:keys [highestBlock currentBlock] :as state}
          (js->clj sync :keywordize-keys true)
          syncing?  (> (- highestBlock currentBlock) constants/blocks-per-hour)
          new-state (cond
                      error :offline
                      syncing? (if (= sync-state :done)
                                 :pending
                                 :in-progress)
                      :else (if (or (= sync-state :done)
                                    (= sync-state :pending))
                              :done
                              :synced))]
      (cond-> db
        (when (and (not= sync-data state) (= :in-progress new-state)))
        (assoc :sync-data state)
        (when (not= sync-state new-state))
        (assoc :sync-state new-state)))))

(handlers/register-handler-fx
  :check-sync
  (fn [{{:keys [web3] :as db} :db} _]
    {::web3-get-syncing web3
     :dispatch-later [{:ms 10000 :dispatch [:check-sync]}]}))

(handlers/register-handler-fx
  :initialize-sync-listener
  (fn [{{:keys [sync-listening-started] :as db} :db} _]
    (when-not sync-listening-started
      {:db (assoc db :sync-listening-started true)
       :dispatch [:check-sync]})))

;;; MESSAGES

(handlers/register-handler-fx
  :incoming-message
  (fn [_ [_ type {:keys [payload ttl id] :as message}]]
    (let [message-id (or id (:message-id payload))]
      (when-not (cache/exists? message-id type)
        (let [ttl-s             (* 1000 (or ttl 120))
              processed-message {:id         (random/id)
                                 :message-id message-id
                                 :type       type
                                 :ttl        (+ (datetime/now-ms) ttl-s)}
              route-event (case type
                            :message               [:received-protocol-message! message]
                            :group-message         [:received-protocol-message! message]
                            :public-group-message  [:received-protocol-message! message]
                            :ack                   (if (#{:message :group-message} (:type payload))
                                                     [:update-message-status message :delivered]
                                                     [:pending-message-remove message])
                            :seen                  [:update-message-status message :seen]
                            :group-invitation      [:group-chat-invite-received message]
                            :update-group          [:update-group-message message]
                            :add-group-identity    [:participant-invited-to-group message]
                            :remove-group-identity [:participant-removed-from-group message]
                            :leave-group           [:participant-left-group message]
                            :contact-request       [:contact-request-received message]
                            :discover              [:status-received message]
                            :discoveries-request   [:discoveries-request-received message]
                            :discoveries-response  [:discoveries-response-received message]
                            :profile               [:contact-update-received message]
                            :update-keys           [:update-keys-received message]
                            :online                [:contact-online-received message]
                            :pending               [:pending-message-upsert message]
                            :sent                  (let [{:keys [to id group-id]} message
                                                         message' {:from    to
                                                                   :payload {:message-id id
                                                                             :group-id   group-id}}]
                                                     [:update-message-status message' :sent])
                            nil)]
          (when (nil? route-event) (debug "Unknown message type" type))
          (cache/add! processed-message)
          (merge
            {::save-processed-messages processed-message}
            (when route-event {:dispatch route-event})))))))

(defn update-message-status [db {:keys [message-id ack-of-message group-id from status]}]
  (let [message-id' (or ack-of-message message-id)
        group?      (boolean group-id)
        status-path (if (and group? (not= status :sent))
                      [:message-data :user-statuses message-id' from]
                      [:message-data :statuses message-id'])
        {current-status :status} (get-in db status-path)]
    (if-not (= :seen current-status)
      (assoc-in db status-path {:whisper-identity from
                                :status           status})
      db)))

(handlers/register-handler-fx
  :update-message-status
  [re-frame/trim-v
   (re-frame/inject-cofx ::chats-is-active?)]
  (fn [{db :db chats-is-active? :chats-is-active?}
       [{:keys                                        [from]
         {:keys [message-id ack-of-message group-id]} :payload
         :as message}
        status]]
    (let [data {:status status
                :message-id message-id :ack-of-message ack-of-message
                :group-id group-id :from from}]
      (merge
        {::save-message-status! data}
        (when (= status :delivered)
         {:dispatch  [:pending-message-remove message]})
        (when chats-is-active?
          {:db (update-message-status db data)})))))

(handlers/register-handler-fx
  :contact-request-received
  (fn [{{:contacts/keys [contacts]} :db}
       [_ {:keys [from payload]}]]
    (when from
      (let [{{:keys [name profile-image address status fcm-token]} :contact
             {:keys [public private]}                              :keypair} payload
            existing-contact (get contacts from)
            contact          {:whisper-identity from
                              :public-key       public
                              :private-key      private
                              :address          address
                              :status           status
                              :photo-path       profile-image
                              :name             name
                              :fcm-token        fcm-token}
            chat             {:name         name
                              :chat-id      from
                              :contact-info (prn-str contact)}]
        (if-not existing-contact
          (let [contact (assoc contact :pending? true)]
            {:dispatch-n [[:add-contacts [contact]]
                          [:add-chat from chat]]})
          (when-not (:pending? existing-contact)
            {:dispatch-n [[:update-contact! contact]
                          [:update-chat! chat]
                          [:watch-contact contact]]}))))))

(handlers/register-handler-fx
  :pending-message-upsert
  (fn [{db :db} [_ {:keys [type id to group-id] :as pending-message}]]
    (let [chat-id        (or group-id to)
          current-status (get-in db [:message-status chat-id id])]
      (merge
        {::pending-messages-save {:type type :id id :pending-message pending-message}}
        (when (and (#{:message :group-message} type) (not= :seen current-status))
          {:db (assoc-in db [:message-status chat-id id] :pending)})))))

(handlers/register-handler-fx
  :pending-message-remove
  (fn [_ [_ message]]
    {::pending-messages-delete message}))



;;GROUP

(handlers/register-handler-fx
  :participant-invited-to-group
  [re-frame/trim-v
   (re-frame/inject-cofx ::has-contact?)]
  (fn [{{:keys [current-public-key chats] :as db} :db has-contact? :has-contact?}
       [{:keys                                            [from]
         {:keys [group-id identity message-id timestamp]} :payload}]]
    (let [admin (get-in chats [group-id :group-admin])]
      (when (= from admin)
        (merge
          {::participant-invited-to-group-message {:group-id group-id :current-public-key current-public-key
                                                   :identity identity :from from :message-id message-id
                                                   :timestamp timestamp}}
          (when-not (and (= current-public-key identity) has-contact?)
            {:db (update-in db [:chats group-id :contacts] conj {:identity identity})
             ::chats-add-contact [group-id [identity]]}))))))

(handlers/register-handler-fx
  ::you-removed-from-group
  [re-frame/trim-v
   (re-frame/inject-cofx ::chats-new-update?)]
  (fn [{{:keys [web3]} :db new-update? :new-update?}
       [{:keys                                               [from]
         {:keys [group-id timestamp message-id] :as payload} :payload}]]
    (when new-update?
      {::you-removed-from-group-message {:from from :message-id message-id :timestamp timestamp
                                         :group-id group-id}
       ::stop-watching-group! {:web3     web3
                               :group-id group-id}
       :dispatch [:update-chat! {:chat-id         group-id
                                 :removed-from-at timestamp
                                 :is-active       false}]})))

(handlers/register-handler-fx
  :participant-removed-from-group
  [re-frame/trim-v
   (re-frame/inject-cofx ::message-get-by-id)]
  (fn [{{:keys [current-public-key chats]} :db message-by-id :message-by-id}
       [{:keys                                              [from]
         {:keys [group-id identity message-id timestamp]}   :payload
         :as                                                message}]]
    (when-not message-by-id
      (let [admin (get-in chats [group-id :group-admin])]
        (when (= admin from)
          (if (= current-public-key identity)
            {:dispatch [::you-removed-from-group message]}
            {::participant-removed-from-group-message {:identity identity :from from :message-id message-id
                                                       :timestamp timestamp :group-id group-id}
             ::chats-remove-contact [group-id identity]}))))))

(handlers/register-handler-fx
  :participant-left-group
  [re-frame/trim-v
   (re-frame/inject-cofx ::chats-is-active-and-timestamp)]
  (fn [{{:keys [current-public-key] :as db} :db chats-is-active-and-timestamp :chats-is-active-and-timestamp}
       [{:keys                                    [from]
         {:keys [group-id timestamp message-id]} :payload}]]
    (when (and (not= current-public-key from)
               chats-is-active-and-timestamp)
      {::participant-left-group-message {:group-id group-id :from from :message-id message-id
                                         :timestamp timestamp}
       ::chats-remove-contact [group-id from]
       :db (update-in db [:chats group-id :contacts]
                      #(remove (fn [{:keys [identity]}]
                                 (= identity from)) %))})))

;;ERROR

(handlers/register-handler-fx
  ::post-error
  (fn [_ [_ error]]
    (let [android-error? (re-find (re-pattern "Failed to connect") (.-message error))]
      (when android-error?
        {::status-init-jail nil}))))
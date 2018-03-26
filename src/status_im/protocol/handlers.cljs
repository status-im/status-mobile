(ns status-im.protocol.handlers
  (:require [re-frame.core :as re-frame]
            [cljs.core.async :as async]
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
            [status-im.utils.async :as async-utils]
            [status-im.protocol.message-cache :as cache]
            [status-im.protocol.listeners :as listeners]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.models :as chat]
            [status-im.protocol.web3.inbox :as inbox]
            [status-im.protocol.web3.keys :as web3.keys]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log]
            [status-im.native-module.core :as status]
            [clojure.string :as string]
            [status-im.utils.web3-provider :as web3-provider]
            [status-im.utils.ethereum.core :as utils]
            [status-im.utils.config :as config]))

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
  ::message-get-by-id
  (fn [coeffects _]
    (let [[{{:keys [message-id]} :payload}] (:event coeffects)]
      (assoc coeffects :message-by-id (messages/get-by-id message-id)))))


;;;; FX

(def ^:private protocol-realm-queue (async-utils/task-queue 2000))

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
      :default-ttl                 120
      :send-online-s-interval      180
      :ttl-config                  {:public-group-message 2400}
      :max-attempts-number         3
      :delivery-loop-ms-interval   500
      :profile-keypair             {:public  updates-public-key
                                    :private updates-private-key}
      :hashtags                    (mapv name (handlers/get-hashtags status))
      :pending-messages            pending-messages
      :contacts                    (keep (fn [{:keys [whisper-identity
                                                      public-key
                                                      private-key]}]
                                           (when (and public-key private-key)
                                             {:identity whisper-identity
                                              :keypair  {:public  public-key
                                                         :private private-key}}))
                                         contacts)
      :post-error-callback         #(re-frame/dispatch [::post-error %])
      :pow-target                  config/pow-target
      :pow-time                    config/pow-time})))

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
    (async/go (async/>! protocol-realm-queue #(processed-messages/save processed-message)))))

(defn system-message [message-id timestamp content]
  {:from         "system"
   :message-id   message-id
   :timestamp    timestamp
   :content      content
   :content-type constants/text-content-type})

(re-frame/reg-fx
  ::participant-removed-from-group-message
  (fn [{:keys [identity from message-id timestamp group-id contacts]}]
    (let [remover-name (get-in contacts [from :name])
          removed-name (get-in contacts [identity :name])
          message (->> [(or remover-name from) (i18n/label :t/removed) (or removed-name identity)]
                       (string/join " ")
                       (system-message message-id timestamp))
          message' (assoc message :group-id group-id)]
      (re-frame/dispatch [:chat-received-message/add message']))))

(re-frame/reg-fx
  ::chats-add-contact
  (fn [[group-id identity]]
    (chats/add-contacts group-id [identity])))

(re-frame/reg-fx
  ::chats-remove-contact
  (fn [[group-id identity]]
    (chats/remove-contacts group-id [identity])))

(re-frame/reg-fx
  ::you-removed-from-group-message
  (fn [{:keys [from message-id timestamp group-id contacts]}]
    (let [remover-name (get-in contacts [from :name])
          message  (->> [(or remover-name from) (i18n/label :t/removed-from-chat)]
                        (string/join " ")
                        (system-message message-id timestamp))
          message' (assoc message :group-id group-id)]
      (re-frame/dispatch [:chat-received-message/add message']))))

(re-frame/reg-fx
  ::stop-watching-group!
  (fn [params]
    (protocol/stop-watching-group! params)))

(re-frame/reg-fx
  ::participant-left-group-message
  (fn [{:keys [chat-id from message-id timestamp contacts]}]
    (let [left-name (get-in contacts [from :name])
          message-text (str (or left-name from) " " (i18n/label :t/left))]
      (-> (system-message message-id timestamp message-text)
          (assoc :chat-id chat-id)
          (messages/save)))))

(re-frame/reg-fx
  ::participant-invited-to-group-message
  (fn [{:keys [group-id current-identity identity from message-id timestamp contacts]}]
    (let [inviter-name (get-in contacts [from :name])
          invitee-name (if (= identity current-identity)
                         (i18n/label :t/You)
                         (get-in contacts [identity :name]))]
      (re-frame/dispatch
       [:chat-received-message/add
        {:from "system"
         :group-id group-id
         :timestamp timestamp
         :message-id message-id
         :content (str (or inviter-name from) " " (i18n/label :t/invited) " " (or invitee-name identity))
         :content-type constants/text-content-type}]))))

(re-frame/reg-fx
  ::pending-messages-delete
  (fn [message-id]
    (async/go (async/>! protocol-realm-queue #(pending-messages/delete message-id)))))

(re-frame/reg-fx
  ::pending-messages-save
  (fn [pending-message]
    (async/go (async/>! protocol-realm-queue #(pending-messages/save pending-message)))))

(re-frame/reg-fx
  ::status-init-jail
  (fn []
    (status/init-jail)))

(re-frame/reg-fx
  ::load-processed-messages!
  (fn []
    (let [now      (datetime/timestamp)
          messages (processed-messages/get-filtered (str "ttl > " now))]
      (cache/init! messages)
      (processed-messages/delete (str "ttl <=" now)))))

(re-frame/reg-fx
 ::add-peer
 (fn [{:keys [wnode web3]}]
   (inbox/add-peer wnode
                   #(re-frame/dispatch [::add-peer-success web3 %])
                   #(re-frame/dispatch [::add-peer-error %]))))

(re-frame/reg-fx
 ::fetch-peers
 (fn [{:keys [wnode web3 retries]}]
   ;; Run immediately on first run, add delay before retry
   (let [delay (cond
                 (zero? retries) 0
                 (< retries 3)   300
                 (< retries 10)  1000
                 :else           5000)]
     (if (> retries 100)
       (log/error "Number of retries for fetching peers exceed" wnode)
       (js/setTimeout
        (fn [] (inbox/fetch-peers #(re-frame/dispatch [::fetch-peers-success web3 % retries])
                                 #(re-frame/dispatch [::fetch-peers-error %])))
        delay)))))

(re-frame/reg-fx
 ::mark-trusted-peer
 (fn [{:keys [wnode web3 peers]}]
   (inbox/mark-trusted-peer web3
                            wnode
                            peers
                            #(re-frame/dispatch [::mark-trusted-peer-success web3 %])
                            #(re-frame/dispatch [::mark-trusted-peer-error %]))))

(re-frame/reg-fx
 ::get-sym-key
 (fn [{:keys [web3 password]}]
   (web3.keys/get-sym-key web3
                     password
                     #(re-frame/dispatch [::get-sym-key-success web3 %])
                     #(re-frame/dispatch [::get-sym-key-error %]))))

(re-frame/reg-fx
 ::request-messages
 (fn [{:keys [wnode topic sym-key-id web3]}]
   (inbox/request-messages web3
                           wnode
                           topic
                           sym-key-id
                           #(re-frame/dispatch [::request-messages-success %])
                           #(re-frame/dispatch [::request-messages-error %]))))

(re-frame/reg-fx
  ::handle-whisper-message
  listeners/handle-whisper-message)

;;;; Handlers


(defn get-wnode [db]
  (let [wnode-id (get db :inbox/wnode)]
    (get-in db [:inbox/wnodes wnode-id :address])))

(defn connectivity-check [peers {:keys [db] :as cofx}]
  (let [wnode                 (get-wnode db)
        peers-count           (count peers)
        mailserver-connected? (inbox/registered-peer? peers wnode)]
    {:db (cond-> db
           mailserver-connected?       (dissoc :mailserver-status)
           (not mailserver-connected?) (assoc :mailserver-status :disconnected)
           :always                     (assoc :peers-count peers-count))}))

(re-frame/reg-fx
  :connectivity/fetch-peers
  (fn [{:keys [wnode web3 retries]}]
    (inbox/fetch-peers #(re-frame/dispatch [:connectivity-check-success %])
                       #(log/error :connectivity/fetch-peers %))))

(handlers/register-handler-fx
  :connectivity-check
  (fn [{:keys [db]} _]
    (let [web3  (:web3 db)
          wnode (get-wnode db)]
      {:connectivity/fetch-peers {:wnode wnode
                                  :web3  web3}})))

(handlers/register-handler-fx
  :connectivity-check-success
  (fn [{:keys [db] :as cofx} [_ peers]]
    (handlers/merge-fx cofx
                       {:dispatch-later [{:ms 30000 :dispatch [:connectivity-check]}]}
                       (connectivity-check peers))))

;; NOTE(dmitryn): events chain
;; add-peer -> fetch-peers -> mark-trusted-peer -> get-sym-key -> request-messages
(handlers/register-handler-fx
  :initialize-offline-inbox
  (fn [{:keys [db]} [_ web3]]
    (log/info "offline inbox: initialize")
    (let [wnode (get-wnode db)]
      {::add-peer {:wnode wnode
                   :web3  web3}})))

(handlers/register-handler-fx
  ::add-peer-success
  (fn [{:keys [db]} [_ web3 response]]
    (let [wnode (get-wnode db)]
      (log/info "offline inbox: add-peer response" wnode response)
      {::fetch-peers {:wnode wnode
                      :web3  web3
                      :retries 0}})))

(handlers/register-handler-fx
  ::fetch-peers-success
  (fn [{:keys [db] :as cofx} [_ web3 peers retries]]
    (let [wnode (get-wnode db)]
      (log/info "offline inbox: fetch-peers response" peers)
      (if (inbox/registered-peer? peers wnode)
        (handlers/merge-fx cofx
                           {::mark-trusted-peer {:wnode wnode
                                                 :web3  web3
                                                 :peers peers}
                            :dispatch-later [{:ms 30000 :dispatch [:connectivity-check]}]}
                           (connectivity-check peers))
        (do
          (log/info "Peer" wnode "is not registered. Retrying fetch peers.")
          (handlers/merge-fx cofx
                             {::fetch-peers {:wnode   wnode
                                             :web3    web3
                                             :retries (inc retries)}}
                             (connectivity-check peers)))))))

(handlers/register-handler-fx
  ::mark-trusted-peer-success
  (fn [{:keys [db]} [_ web3 response]]
    (let [wnode (get-wnode db)
          password (:inbox/password db)]
      (log/info "offline inbox: mark-trusted-peer response" wnode response)
      {::get-sym-key {:password password
                      :web3     web3}})))



(handlers/register-handler-fx
  ::get-sym-key-success
  (fn [{:keys [db]} [_ web3 sym-key-id]]
    (log/info "offline inbox: get-sym-key response" sym-key-id)
    (let [wnode (get-wnode db)
          topic    (:inbox/topic db)]
      {::request-messages {:wnode      wnode
                           :topic      topic
                           :sym-key-id sym-key-id
                           :web3       web3}})))

(handlers/register-handler-fx
  ::request-messages-success
  (fn [_ [_ response]]
    (log/info "offline inbox: request-messages response" response)))

(handlers/register-handler-fx
  ::add-peer-error
  (fn [_ [_ error]]
    (log/error "offline inbox: add-peer error" error)))

(handlers/register-handler-fx
  ::fetch-peers-error
  (fn [_ [_ error]]
    (log/error "offline inbox: fetch-peers error" error)))

(handlers/register-handler-fx
  ::mark-trusted-peer-error
  (fn [_ [_ error]]
    (log/error "offline inbox: mark-trusted-peer error" error)))

(handlers/register-handler-fx
  ::get-sym-key-error
  (fn [_ [_ error]]
    (log/error "offline inbox: get-sym-key error" error)))

(handlers/register-handler-fx
  ::request-messages-error
  (fn [_ [_ error]]
    (log/error "offline inbox: request-messages error" error)))

(handlers/register-handler-fx
  :handle-whisper-message
  (fn [_ [_ error msg options]]
    {::handle-whisper-message {:error error
                               :msg msg
                               :options options}}))

;;; INITIALIZE PROTOCOL
(handlers/register-handler-fx
  :initialize-protocol
  [re-frame/trim-v
   (re-frame/inject-cofx ::get-web3)
   (re-frame/inject-cofx ::get-chat-groups)
   (re-frame/inject-cofx ::get-pending-messages)
   (re-frame/inject-cofx :get-all-contacts)]
  (fn [{:keys [db web3 groups all-contacts pending-messages]} [current-account-id ethereum-rpc-url]]
    (let [{:keys [public-key status updates-public-key
                  updates-private-key]}
          (get-in db [:accounts/accounts current-account-id])]
      (when public-key
        {::init-whisper {:web3 web3 :public-key public-key :groups groups :pending-messages pending-messages
                         :updates-public-key updates-public-key :updates-private-key updates-private-key
                         :status status :contacts all-contacts}
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
  (fn [{{:keys [web3]} :db} _]
    {::web3-get-syncing web3
     :dispatch-later [{:ms 10000 :dispatch [:check-sync]}]}))

(handlers/register-handler-fx
  :initialize-sync-listener
  (fn [{{:keys [sync-listening-started network networks/networks] :as db} :db} _]
    (when (and (not sync-listening-started)
               (not (utils/network-with-upstream-rpc? networks network)))
      {:db (assoc db :sync-listening-started true)
       :dispatch [:check-sync]})))

;;; MESSAGES

(defn- transform-protocol-message [{:keys [from to payload]}]
  (merge payload {:from    from
                  :to      to
                  :chat-id (or (:group-id payload) from)}))

(defn- message-from-self [{:keys [current-public-key]} {:keys [id to group-id]}]
  {:from      to
   :sent-from current-public-key
   :payload   {:message-id id
               :group-id   group-id}})

(defn- get-message-id [{:keys [message-id ack-of-message]}]
  (or ack-of-message message-id))

(handlers/register-handler-fx
  :incoming-message
  (fn [{:keys [db]} [_ type {:keys [payload ttl id] :as message}]]
    (let [message-id (or id (:message-id payload))]
      (when-not (cache/exists? message-id type)
        (let [ttl-s             (* 1000 (or ttl 120))
              processed-message {:id         (random/id)
                                 :message-id message-id
                                 :type       type
                                 :ttl        (+ (datetime/timestamp) ttl-s)}
              chat-message (#{:message :group-message} (:type payload))
              route-fx (case type
                         (:message
                          :group-message
                          :public-group-message) {:dispatch [:pre-received-message (transform-protocol-message message)]}
                         :pending                (cond-> {::pending-messages-save message}
                                                   chat-message
                                                   (assoc :dispatch
                                                          [:update-message-status (message-from-self db message) :pending]))
                         :sent                   {:dispatch [:update-message-status (message-from-self db message) :sent]}
                         :ack                    (cond-> {::pending-messages-delete (get-message-id payload)}
                                                   chat-message
                                                   (assoc :dispatch [:update-message-status message :delivered]))
                         :seen                   {:dispatch [:update-message-status message :seen]}
                         :group-invitation       {:dispatch [:group-chat-invite-received message]}
                         :update-group           {:dispatch [:update-group-message message]}
                         :add-group-identity     {:dispatch [:participant-invited-to-group message]}
                         :remove-group-identity  {:dispatch [:participant-removed-from-group message]}
                         :leave-group            {:dispatch [:participant-left-group message]}
                         :contact-request        {:dispatch [:contact-request-received message]}
                         :discover               {:dispatch [:status-received message]}
                         :discoveries-request    {:dispatch [:discoveries-request-received message]}
                         :discoveries-response   {:dispatch [:discoveries-response-received message]}
                         :profile                {:dispatch [:contact-update-received message]}
                         :update-keys            {:dispatch [:update-keys-received message]}
                         :online                 {:dispatch [:contact-online-received message]}
                         nil)]
          (when (nil? route-fx) (log/debug "Unknown message type" type))
          (cache/add! processed-message)
          (merge
           {::save-processed-messages processed-message}
           route-fx))))))

(handlers/register-handler-fx
  :update-message-status
  [re-frame/trim-v (re-frame/inject-cofx :get-stored-message)]
  (fn [{:keys [db get-stored-message]} [{:keys [from sent-from payload]} status]]
    (let [message-identifier (get-message-id payload)
          chat-identifier    (or (:group-id payload) from)
          message-db-path    [:chats chat-identifier :messages message-identifier]
          from-id            (or sent-from from)
          message            (or (get-in db message-db-path)
                                 (and (get (:not-loaded-message-ids db) message-identifier)
                                      (get-stored-message message-identifier)))]
      ;; proceed with updating status if chat is in db, status is not the same and message was not already seen
      (when (and message
                 (get-in db [:chats chat-identifier])
                 (not= status (get-in message [:user-statuses from-id]))
                 (not (models.message/message-seen-by? message from-id)))
        (let [statuses (assoc (:user-statuses message) from-id status)]
          (cond-> {:update-message {:message-id    message-identifier
                                    :user-statuses statuses}}
            (get-in db message-db-path)
            (assoc :db (assoc-in db (conj message-db-path :user-statuses) statuses))))))))

(handlers/register-handler-fx
  :contact-request-received
  (fn [{{:contacts/keys [contacts]} :db}
       [_ {:keys [from payload timestamp]}]]
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
                              :contact-info (prn-str contact)}
            prev-last-updated (get-in contacts [from :last-updated] 0)
            ;; NOTE(dmitryn) Workaround for old messages not having "payload.timestamp" attribute.
            ;; Get timestamp from message root level.
            ;; Root level "timestamp" is a unix ts in seconds.
            timestamp'        (or (:payload timestamp)
                                  (* 1000 timestamp))]
        (if-not existing-contact
          (let [contact (assoc contact :pending? true)]
            {:dispatch-n [[:add-contacts [contact]]
                          [:add-chat from chat]]})
          (when-not (:pending? existing-contact)
            (cond-> {:dispatch-n [[:update-chat! chat]
                                  [:watch-contact contact]]}
              (<= prev-last-updated timestamp') (update :dispatch-n concat [[:update-contact! contact]]))))))))

;;GROUP

(defn- has-contact? [{:keys [contacts]} identity]
  (let [identities (set (map :identity contacts))]
    (contains? identities identity)))

(handlers/register-handler-fx
  :participant-invited-to-group
  [re-frame/trim-v]
  (fn [{{:keys [current-public-key chats contacts/contacts] :as db} :db}
       [{:keys                                            [from]
         {:keys [group-id identity message-id timestamp]} :payload}]]
    (let [chat  (get-in db [:chats group-id])
          admin (:group-admin chats)]
      (when (= from admin)
        (merge {::participant-invited-to-group-message {:group-id group-id :current-public-key current-public-key
                                                        :identity identity :from from :message-id message-id
                                                        :timestamp timestamp :contacts contacts}}
               (when-not (and (= current-public-key identity) (has-contact? chat identity))
                 {:db (update-in db [:chats group-id :contacts] conj {:identity identity})
                  ::chats-add-contact [group-id identity]}))))))

(handlers/register-handler-fx
  ::you-removed-from-group
  [re-frame/trim-v]
  (fn [{{:keys [web3 contacts/contacts] :as db} :db}
       [{:keys                                               [from]
         {:keys [group-id timestamp message-id]} :payload}]]
    (let [chat        (get-in db [:chats group-id])
          new-update? (chat/new-update? chat timestamp)]
      (when new-update?
        {::you-removed-from-group-message {:from       from
                                           :message-id message-id
                                           :timestamp  timestamp
                                           :group-id   group-id
                                           :contacts   contacts}
         ::stop-watching-group!           {:web3     web3
                                           :group-id group-id}
         :dispatch                        [:update-chat! {:chat-id         group-id
                                                          :removed-from-at timestamp
                                                          :is-active       false}]}))))

(handlers/register-handler-fx
  :participant-removed-from-group
  [re-frame/trim-v
   (re-frame/inject-cofx ::message-get-by-id)]
  (fn [{{:keys [current-public-key chats contacts/contacts]} :db message-by-id :message-by-id}
       [{:keys                                              [from]
         {:keys [group-id identity message-id timestamp]}   :payload
         :as                                                message}]]
    (when-not message-by-id
      (let [admin (get-in chats [group-id :group-admin])]
        (when (= admin from)
          (if (= current-public-key identity)
            {:dispatch [::you-removed-from-group message]}
            {::participant-removed-from-group-message {:identity identity :from from :message-id message-id
                                                       :timestamp timestamp :group-id group-id :contacts contacts}
             ::chats-remove-contact [group-id identity]}))))))

(handlers/register-handler-fx
  :participant-left-group
  [re-frame/trim-v]
  (fn [{{:keys [current-public-key contacts/contacts] :as db} :db}
       [{:keys                                    [from]
         {:keys [group-id timestamp message-id]} :payload}]]
    (let [chat (get-in db [:chats group-id])
          {chat-is-active :is-active chat-timestamp :timestamp} chat]
      (when (and (not= current-public-key from)
                 chat-is-active
                 (> timestamp chat-timestamp))
        {::participant-left-group-message {:chat-id    group-id
                                           :from       from
                                           :message-id message-id
                                           :timestamp  timestamp
                                           :contacts   contacts}
         ::chats-remove-contact [group-id from]
         :db (update-in db [:chats group-id :contacts]
                        #(remove (fn [{:keys [identity]}]
                                   (= identity from)) %))}))))

;;ERROR

(handlers/register-handler-fx
  ::post-error
  (fn [_ [_ error]]
    (let [android-error? (re-find (re-pattern "Failed to connect") (.-message error))]
      (when android-error?
        {::status-init-jail nil}))))

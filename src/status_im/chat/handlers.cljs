(ns status-im.chat.handlers
  (:require-macros [cljs.core.async.macros :as am])
  (:require [re-frame.core :refer [enrich after debug dispatch reg-fx]]
            [clojure.string :as string]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.constants :as chat-const]
            [status-im.protocol.core :as protocol]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         console-chat-id
                                         wallet-chat-id]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.ui.screens.navigation :as nav]
            [status-im.utils.handlers :refer [register-handler register-handler-fx] :as u]
            [status-im.utils.phone-number :refer [format-phone-number
                                                  valid-mobile-number?]]
            [status-im.native-module.core :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.chat.utils :refer [console? not-console? safe-trim]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            status-im.chat.events
            status-im.chat.handlers.requests
            status-im.chat.handlers.send-message
            [cljs.core.async :as a]
            status-im.chat.handlers.webview-bridge
            [taoensso.timbre :as log]))

(defn load-messages!
  ([db] (load-messages! db nil))
  ([{:keys [current-chat-id] :as db} _]
   (let [messages (messages/get-by-chat-id current-chat-id)]
     (assoc db :messages messages))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (-> db
       (assoc-in [:chats current-chat-id :messages] messages)
       (dissoc :messages))))

(defn load-commands!
  [{:keys [current-chat-id]} _]
  (dispatch [:load-commands! current-chat-id]))

(register-handler :init-chat
  (after #(dispatch [:load-requests!]))

  (u/handlers->
    load-messages!
    init-chat
    load-commands!))

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id] :accounts/keys [current-account-id] :as db} [_ _ id]]
  (let [chat-id           (or id current-chat-id)
        messages          (get-in db [:chats chat-id :messages])
        db'               (-> db
                              (assoc :current-chat-id chat-id)
                              (assoc-in [:chats chat-id :was-opened?] true))
        commands-loaded?  (get-in db [:contacts/contacts chat-id :commands-loaded?])
        bot-url           (get-in db [:contacts/contacts chat-id :bot-url])
        was-opened?       (get-in db [:chats chat-id :was-opened?])
        call-init-command #(when (and (not was-opened?) bot-url)
                             (status/call-function!
                               {:chat-id  chat-id
                                :function :init
                                :context  {:from current-account-id}}))]
       ; Reset validation messages, if any
       (dispatch [:set-chat-ui-props {:validation-messages nil}])
       (dispatch [:load-requests! chat-id])
        ;; todo rewrite this. temporary fix for https://github.com/status-im/status-react/issues/607
       #_(dispatch [:load-commands! chat-id])
        (if-not commands-loaded?
          (dispatch [:load-commands! chat-id call-init-command])
          (do
            (call-init-command)
            (dispatch [:invoke-chat-loaded-callbacks chat-id])))
        (if (and (seq messages)
                 (not= (count messages) 1))
          db'
          (-> db'
              load-messages!
              init-chat))))

(register-handler :add-chat-loaded-callback
  (fn [db [_ chat-id callback]]
    (log/debug "Add chat loaded callback: " chat-id callback)
    (update-in db [:chat-loaded-callbacks chat-id] conj callback)))

(register-handler ::clear-chat-loaded-callbacks
  (fn [db [_ chat-id]]
    (log/debug "Clear chat loaded callback: " chat-id)
    (assoc-in db [:chat-loaded-callbacks chat-id] nil)))

(register-handler :invoke-chat-loaded-callbacks
  (u/side-effect!
    (fn [db [_ chat-id]]
      (log/debug "Invoking chat loaded callbacks: " chat-id)
      (let [callbacks (get-in db [:chat-loaded-callbacks chat-id])]
        (log/debug "Invoking chat loaded callbacks: " callbacks)
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-chat-loaded-callbacks chat-id])))))

(defn prepare-chat [{:contacts/keys [contacts]} chat-id chat]
  (let [name (get-in contacts [chat-id :name])
        whisper-identity (get-in contacts [chat-id :whisper-identity])]
    (merge {:chat-id    chat-id
            :name       (or name (generate-gfy whisper-identity))
            :color      default-chat-color
            :group-chat false
            :is-active  true
            :timestamp  (.getTime (js/Date.))
            :contacts   [{:identity chat-id}]}
           chat)))

(defn add-new-chat
  [db [_ chat-id chat]]
  (assoc db :new-chat (prepare-chat db chat-id chat)))

(defn add-chat [{:keys [new-chat chats] :as db} [_ chat-id]]
  (if-not (get chats chat-id)
    (update db :chats assoc chat-id new-chat)
    db))

(defn save-new-chat!
  [{{:keys [chat-id] :as new-chat} :new-chat} _]
  (when-not (chats/exists? chat-id)
    (chats/save new-chat)))

(defn open-chat!
  [_ [_ chat-id _ navigation-type]]
  (dispatch [(or navigation-type :navigate-to) :chat chat-id]))

(register-handler ::start-chat!
  (u/handlers->
    add-new-chat
    add-chat
    save-new-chat!
    open-chat!))

(register-handler :start-chat
  (u/side-effect!
    (fn [{:keys [chats current-public-key]}
         [_ contact-id options navigation-type]]
      (when-not (= current-public-key contact-id)
        (if (chats contact-id)
          (dispatch [(or navigation-type :navigate-to) :chat contact-id])
          (dispatch [::start-chat! contact-id options navigation-type]))))))

(register-handler :add-chat
  (u/handlers->
    add-new-chat
    add-chat
    save-new-chat!))

(defn update-chat!
  [_ [_ {:keys [name] :as chat}]]
  (let [chat' (if name chat (dissoc chat :name))]
    (chats/save chat')))

(register-handler :update-chat!
  (after update-chat!)
  (fn [db [_ {:keys [chat-id name] :as chat}]]
    (let [chat' (if name chat (dissoc chat :name))]
      (if (get-in db [:chats chat-id])
        (update-in db [:chats chat-id] merge chat')
        db))))

(register-handler :upsert-chat!
  (fn [db [_ {:keys [chat-id] :as opts}]]
    (let [chat (if (chats/exists? chat-id)
                 (-> (chats/get-by-id chat-id)
                     (assoc :timestamp (random/timestamp))
                     (merge opts))
                 (prepare-chat db chat-id opts))]
      (chats/save chat)
      (update-in db [:chats chat-id] merge chat))))

(defn remove-chat
  [db [_ chat-id]]
  (update db :chats dissoc chat-id))

(reg-fx
  ::delete-messages
  (fn [id]
    (messages/delete-by-chat-id id)))

(defn delete-messages!
  [{:keys [current-chat-id]} [_ chat-id]]
  (let [id (or chat-id current-chat-id)]
    (messages/delete-by-chat-id id)))

(defn delete-chat!
  [_ [_ chat-id]]
  (let [{:keys [debug? group-chat]} (chats/get-by-id chat-id)]
    (if (and (not debug?) group-chat)
      (chats/set-inactive chat-id)
      (chats/delete chat-id))))

(defn remove-pending-messages!
  [_ [_ chat-id]]
  (pending-messages/delete-all-by-chat-id chat-id))

(register-handler :leave-group-chat
  ;; todo oreder of operations tbd
  (after (fn [_ _] (dispatch [:navigation-replace :chat-list])))
  (u/side-effect!
    (fn [{:keys [web3 current-chat-id chats current-public-key]} _]
      (let [{:keys [public-key private-key public?]} (chats current-chat-id)]
        (protocol/stop-watching-group!
          {:web3     web3
           :group-id current-chat-id})
        (when-not public?
          (protocol/leave-group-chat!
            {:web3     web3
             :group-id current-chat-id
             :keypair  {:public  public-key
                        :private private-key}
             :message  {:from       current-public-key
                        :message-id (random/id)}})))
      (dispatch [:remove-chat current-chat-id]))))

(register-handler :remove-chat
  (u/handlers->
    remove-chat
    delete-messages!
    remove-pending-messages!
    delete-chat!))

(register-handler :check-and-open-dapp!
  (u/side-effect!
    (fn [{:keys [current-chat-id global-commands]
          :contacts/keys [contacts]}]
      (let [dapp-url (get-in contacts [current-chat-id :dapp-url])]
        (when dapp-url
          (am/go
            (dispatch [:select-chat-input-command
                       (assoc (first (:browse global-commands)) :prefill [dapp-url])
                       nil
                       true])
            (a/<! (a/timeout 100))
            (dispatch [:send-current-message])))))))

(register-handler :update-group-message
  (u/side-effect!
    (fn [{:keys [current-public-key web3 chats]}
         [_ {:keys                                [from]
             {:keys [group-id keypair timestamp]} :payload}]]
      (let [{:keys [private public]} keypair]
        (let [is-active (chats/is-active? group-id)
              chat      {:chat-id     group-id
                         :public-key  public
                         :private-key private
                         :updated-at  timestamp}]
          (when (and (= from (get-in chats [group-id :group-admin]))
                     (or (not (chats/exists? group-id))
                         (chats/new-update? timestamp group-id)))
            (dispatch [:update-chat! chat])
            (when is-active
              (protocol/start-watching-group!
                {:web3     web3
                 :group-id group-id
                 :identity current-public-key
                 :keypair  keypair
                 :callback #(dispatch [:incoming-message %1 %2])}))))))))

(register-handler :update-message-overhead!
  (u/side-effect!
    (fn [_ [_ chat-id network-status]]
      (if (= network-status :offline)
        (chats/inc-message-overhead chat-id)
        (chats/reset-message-overhead chat-id)))))

(reg-fx
  ::save-public-chat
  (fn [chat]
    (chats/save chat)))

(reg-fx
  ::start-watching-group
  (fn [{:keys [group-id web3 current-public-key keypair]}]
    (protocol/start-watching-group!
      {:web3     web3
       :group-id group-id
       :identity current-public-key
       :keypair  keypair
       :callback #(dispatch [:incoming-message %1 %2])})))

(register-handler-fx
  :create-new-public-chat
  (fn [{:keys [db]} [_ topic]]
    (let [exists? (boolean (get-in db [:chats topic]))
          chat    {:chat-id     topic
                   :name        topic
                   :color       default-chat-color
                   :group-chat  true
                   :public?     true
                   :is-active   true
                   :timestamp   (random/timestamp)}]
      (merge
        (when-not exists?
          {:db (assoc-in db [:chats (:chat-id chat)] chat)
           ::save-public-chat chat
           ::start-watching-group (merge {:group-id topic}
                                         (select-keys db [:web3 :current-public-key]))})
        {:dispatch-n [[:navigate-to-clean :chat-list]
                      [:navigate-to :chat topic]]}))))

(reg-fx
  ::save-chat
  (fn [new-chat]
    (chats/save new-chat)))

(reg-fx
  ::start-listen-group
  (fn [{:keys [new-chat web3 current-public-key]}]
    (let [{:keys [chat-id public-key private-key contacts name]} new-chat
          identities (mapv :identity contacts)]
      (protocol/invite-to-group!
        {:web3       web3
         :group      {:id       chat-id
                      :name     name
                      :contacts (conj identities current-public-key)
                      :admin    current-public-key
                      :keypair  {:public  public-key
                                 :private private-key}}
         :identities identities
         :message    {:from       current-public-key
                      :message-id (random/id)}})
      (protocol/start-watching-group!
        {:web3     web3
         :group-id chat-id
         :identity current-public-key
         :keypair  {:public  public-key
                    :private private-key}
         :callback #(dispatch [:incoming-message %1 %2])}))))

(defn group-name-from-contacts [contacts selected-contacts username]
  (->> (select-keys contacts selected-contacts)
       vals
       (map :name)
       (cons username)
       (string/join ", ")))

(defn prepare-group-chat
  [{:keys [current-public-key username]
    :group/keys [selected-contacts]
    :contacts/keys [contacts]} group-name]
  (let [selected-contacts'  (mapv #(hash-map :identity %) selected-contacts)
        chat-name (if-not (string/blank? group-name)
                    group-name
                    (group-name-from-contacts contacts selected-contacts username))
        {:keys [public private]} (protocol/new-keypair!)]
    {:chat-id     (random/id)
     :public-key  public
     :private-key private
     :name        chat-name
     :color       default-chat-color
     :group-chat  true
     :group-admin current-public-key
     :is-active   true
     :timestamp   (random/timestamp)
     :contacts    selected-contacts'}))

(register-handler-fx
  :create-new-group-chat-and-open
  (fn [{:keys [db]} [_ group-name]]
    (let [new-chat (prepare-group-chat (select-keys db [:group/selected-contacts :current-public-key :username
                                                        :contacts/contacts])
                                       group-name)]
      {:db (-> db
               (assoc-in [:chats (:chat-id new-chat)] new-chat)
               (assoc :group/selected-contacts #{}))
       ::save-chat new-chat
       ::start-listen-group (merge {:new-chat new-chat}
                                   (select-keys db [:web3 :current-public-key]))
       :dispatch-n [[:navigate-to-clean :chat-list]
                    [:navigate-to :chat (:chat-id new-chat)]]})))

(register-handler-fx
  :group-chat-invite-received
  (fn [{{:keys [current-public-key] :as db} :db}
       [_ {:keys                                                    [from]
           {:keys [group-id group-name contacts keypair timestamp]} :payload}]]
    (let [{:keys [private public]} keypair]
      (let [contacts' (keep (fn [ident]
                              (when (not= ident current-public-key)
                                {:identity ident})) contacts)
            chat      {:chat-id     group-id
                       :name        group-name
                       :group-chat  true
                       :group-admin from
                       :public-key  public
                       :private-key private
                       :contacts    contacts'
                       :added-to-at timestamp
                       :timestamp   timestamp
                       :is-active   true}
            exists?   (chats/exists? group-id)]
        (when (or (not exists?) (chats/new-update? timestamp group-id))
          {::start-watching-group (merge {:group-id group-id
                                          :keypair keypair}
                                         (select-keys db [:web3 :current-public-key]))
           :dispatch (if exists?
                       [:update-chat! chat]
                       [:add-chat group-id chat])})))))

(register-handler-fx
  :show-profile
  (fn [{db :db} [_ identity]]
    {:db (assoc db :contacts/identity identity)
     :dispatch [:navigate-forget :profile]}))

(ns status-im.chat.events
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.chat.models :as model]
            [status-im.chat.console :as console-chat]
            [status-im.chat.constants :as chat-const]
            [status-im.data-store.messages :as msg-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.data-store.requests :as requests-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.protocol.core :as protocol]
            [status-im.constants :as const]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.chat.events.input :as input-events]
            status-im.chat.events.commands
            status-im.chat.events.requests
            status-im.chat.events.animation
            status-im.chat.events.send-message
            status-im.chat.events.queue-message
            status-im.chat.events.receive-message
            status-im.chat.events.sign-up
            status-im.chat.events.console
            status-im.chat.events.webview-bridge))

;;;; Coeffects

(re-frame/reg-cofx
  :stored-unviewed-messages
  (fn [cofx _]
    (assoc cofx :stored-unviewed-messages
           (msg-store/get-unviewed (-> cofx :db :current-public-key)))))

(re-frame/reg-cofx
  :get-stored-message
  (fn [cofx _]
    (assoc cofx :get-stored-message messages-store/get-by-id)))

(re-frame/reg-cofx
  :get-stored-messages
  (fn [cofx _]
    (assoc cofx :get-stored-messages messages-store/get-by-chat-id)))

(re-frame/reg-cofx
  :all-stored-chats
  (fn [cofx _]
    (assoc cofx :all-stored-chats (chats-store/get-all))))

(re-frame/reg-cofx
  :get-stored-chat
  (fn [cofx _]
    (assoc cofx :get-stored-chat chats-store/get-by-id)))

(re-frame/reg-cofx
  :message-exists?
  (fn [cofx]
    (assoc cofx :message-exists? messages-store/exists?)))

(re-frame/reg-cofx
  :get-last-clock-value
  (fn [cofx]
    (assoc cofx :get-last-clock-value messages-store/get-last-clock-value)))

;;;; Effects

(def ^:private update-message-queue (async/chan 100))

(async/go-loop [message (async/<! update-message-queue)]
  (when message
    (messages-store/update-message message)
    (recur (async/<! update-message-queue))))

(re-frame/reg-fx
  :update-message
  (fn [message]
    (async/put! update-message-queue message)))

(re-frame/reg-fx
  :save-message
  (fn [message]
    (messages-store/save message)))

(re-frame/reg-fx
  :save-chat
  (fn [chat]
    (chats-store/save chat)))

(re-frame/reg-fx
  :save-all-contacts
  (fn [contacts]
    (contacts-store/save-all contacts)))

(re-frame/reg-fx
  :protocol-send-seen
  (fn [params]
    (protocol/send-seen! params)))

(re-frame/reg-fx
  :browse
  (fn [link]
    (list-selection/browse link)))

;;;; Handlers

(handlers/register-handler-db
  :set-layout-height
  [re-frame/trim-v]
  (fn [db [height]]
    (assoc db :layout-height height)))

(handlers/register-handler-db
  :set-chat-ui-props
  [re-frame/trim-v]
  (fn [db [kvs]]
    (model/set-chat-ui-props db kvs)))

(handlers/register-handler-db
  :toggle-chat-ui-props
  [re-frame/trim-v]
  (fn [db [ui-element]]
    (model/toggle-chat-ui-prop db ui-element)))

(handlers/register-handler-db
  :show-message-details
  [re-frame/trim-v]
  (fn [db [details]]
    (model/set-chat-ui-props db {:show-bottom-info? true
                                 :show-emoji? false
                                 :bottom-info details})))

(def index-messages (partial into {} (map (juxt :message-id identity))))

(handlers/register-handler-fx
  :load-more-messages
  [(re-frame/inject-cofx :get-stored-messages)]
  (fn [{{:keys [current-chat-id] :as db} :db get-stored-messages :get-stored-messages} _]
    (when-not (get-in db [:chats current-chat-id :all-loaded?])
      (let [loaded-count (count (get-in db [:chats current-chat-id :messages]))
            new-messages (get-stored-messages current-chat-id loaded-count)]
        {:db (-> db
                 (update-in [:chats current-chat-id :messages] merge (index-messages new-messages))
                 (assoc-in [:chats current-chat-id :all-loaded?]
                           (> const/default-number-of-messages (count new-messages))))}))))

(handlers/register-handler-db
  :set-message-shown
  [re-frame/trim-v]
  (fn [db [{:keys [chat-id message-id]}]]
    (update-in db [:chats chat-id :messages message-id] assoc :new? false)))

(defn init-console-chat
  [{:keys [chats] :accounts/keys [current-account-id] :as db}]
  (if (chats const/console-chat-id)
    {:db db}
    (cond-> {:db (-> db
                     (assoc :current-chat-id const/console-chat-id)
                     (update :chats assoc const/console-chat-id console-chat/chat))
             :dispatch-n [[:add-contacts [console-chat/contact]]]
             :save-chat console-chat/chat
             :save-all-contacts [console-chat/contact]}

      (not current-account-id)
      (update :dispatch-n concat [[:chat-received-message/add-when-commands-loaded console-chat/intro-message1]]))))


(handlers/register-handler-fx
  :init-console-chat
  (fn [{:keys [db]} _]
    (init-console-chat db)))

(handlers/register-handler-fx
  :initialize-chats
  [(re-frame/inject-cofx :all-stored-chats)
   (re-frame/inject-cofx :get-stored-messages)
   (re-frame/inject-cofx :stored-unviewed-messages)
   (re-frame/inject-cofx :get-stored-unanswered-requests)]
  (fn [{:keys [db
               all-stored-chats
               stored-unanswered-requests
               get-stored-messages
               stored-unviewed-messages]} _]
    (let [{:accounts/keys [account-creation?]} db
          load-default-contacts-event [:load-default-contacts!]]
      (if account-creation?
        {:db db
         :dispatch load-default-contacts-event}
        (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                                  (assoc-in acc [chat-id message-id] request))
                                                {}
                                                stored-unanswered-requests)
              chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                              (assoc acc chat-id
                                     (assoc chat
                                            :unviewed-messages (get stored-unviewed-messages chat-id)
                                            :requests (get chat->message-id->request chat-id)
                                            :messages (index-messages (get-stored-messages chat-id)))))
                            {}
                            all-stored-chats)]
          (-> db
              (assoc :chats chats)
              init-console-chat
              (update :dispatch-n conj load-default-contacts-event)))))))

(handlers/register-handler-fx
  :send-seen!
  [re-frame/trim-v]
  (fn [{:keys [db]} [{:keys [chat-id from me message-id]}]]
    (let [{:keys [web3 chats] :contacts/keys [contacts]} db
          {:keys [group-chat public? messages]} (get chats chat-id)
          statuses (assoc (get-in messages [message-id :user-statuses]) me :seen)]
      (cond-> {:db (-> db
                       (update-in [:chats chat-id :unviewed-messages] disj message-id)
                       (assoc-in [:chats chat-id :messages message-id :user-statuses] statuses))
               :update-message {:message-id    message-id
                                :user-statuses statuses}}
        ;; for public chats and 1-1 bot/dapp chats, it makes no sense to signalise `:seen` msg
        (not (or public? (get-in contacts [chat-id :dapp?])))
        (assoc :protocol-send-seen {:web3    web3
                                    :message (cond-> {:from       me
                                                      :to         from
                                                      :message-id message-id}
                                               group-chat (assoc :group-id chat-id))})))))

(handlers/register-handler-fx
  :show-mnemonic
  [(re-frame/inject-cofx :get-stored-message) re-frame/trim-v]
  (fn [{:keys [get-stored-message]} [mnemonic signing-phrase]]
    (let [crazy-math-message? (get-stored-message chat-const/crazy-math-message-id)
          messages-events     (->> (console-chat/passphrase-messages mnemonic signing-phrase crazy-math-message?)
                                   (mapv #(vector :chat-received-message/add %)))]
      {:dispatch-n messages-events})))

;; TODO(alwx): can be simplified
(handlers/register-handler-fx
  :account-generation-message
  [(re-frame/inject-cofx :get-stored-message)]
  (fn [{:keys [get-stored-message]} _]
    (when-not (get-stored-message chat-const/passphrase-message-id)
      {:dispatch [:chat-received-message/add console-chat/account-generation-message]})))

(handlers/register-handler-fx
  :move-to-internal-failure-message
  [(re-frame/inject-cofx :get-stored-message)]
  (fn [{:keys [get-stored-message]} _]
    (when-not (get-stored-message chat-const/move-to-internal-failure-message-id)
      {:dispatch [:chat-received-message/add console-chat/move-to-internal-failure-message]})))

(handlers/register-handler-fx
  :browse-link-from-message
  (fn [{{:contacts/keys [contacts]} :db} [_ link]]
    {:browse link}))

(defn preload-chat-data
  "Takes coeffects map and chat-id, returns effects necessary when navigating to chat"
  [{:keys [db]} chat-id]
  (let [messages (get-in db [:chats chat-id :messages])
        chat-loaded-event (get-in db [:chats chat-id :chat-loaded-event])
        jail-loaded? (get-in db [:contacts/contacts chat-id :jail-loaded?])]
    (cond-> {:db (-> db
                     (assoc :current-chat-id chat-id)
                     (assoc-in [:chats chat-id :was-opened?] true)
                     (model/set-chat-ui-props {:validation-messages nil})
                     (update-in [:chats chat-id] dissoc :chat-loaded-event))}

      chat-loaded-event
      (assoc :dispatch chat-loaded-event))))

(handlers/register-handler-fx
  :add-chat-loaded-event
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [chat-id event]]
    (if (get (:chats db) chat-id)
      {:db (assoc-in db [:chats chat-id :chat-loaded-event] event)}
      (-> (model/add-chat cofx chat-id) ; chat not created yet, we have to create it
          (assoc-in [:db :chats chat-id :chat-loaded-event] event)))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `add-chat` will stay)
(handlers/register-handler-fx
  :add-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id chat-props]]
    (model/add-chat cofx chat-id chat-props)))

(defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  ([cofx chat-id]
   (navigate-to-chat cofx chat-id false))
  ([cofx chat-id navigation-replace?]
   (let [nav-fn (if navigation-replace?
                  #(navigation/navigate-to % :chat)
                  #(navigation/replace-view % :chat))]
     (-> (preload-chat-data cofx chat-id)
         (update :db nav-fn)))))

(handlers/register-handler-fx
  :navigate-to-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id {:keys [navigation-replace?]}]]
    (navigate-to-chat cofx chat-id navigation-replace?)))

(handlers/register-handler-fx
  :start-chat
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [contact-id {:keys [navigation-replace?]}]]
    (when (not= (:current-public-key db) contact-id) ; don't allow to open chat with yourself
      (if (get (:chats db) contact-id)
        (navigate-to-chat cofx contact-id navigation-replace?) ; existing chat, just preload and displey
        (let [add-chat-fx (model/add-chat cofx contact-id)] ; new chat, create before preload & display
          (merge add-chat-fx
                 (navigate-to-chat (assoc cofx :db (:db add-chat-fx))
                                   contact-id
                                   navigation-replace?)))))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `update-chat` will stay)
(handlers/register-handler-fx
  :update-chat!
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [cofx [chat]]
    (model/update-chat cofx chat)))

;; TODO(janherich): remove this unnecessary event in the future (only model function `upsert-chat` will stay)
(handlers/register-handler-fx
  :upsert-chat!
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [cofx [chat]]
    (model/upsert-chat cofx chat)))
(ns status-im.chat.events
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.chat.models :as models]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.console :as console]
            [status-im.chat.constants :as chat.constants]
            [status-im.commands.events.loading :as events.loading]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.group.events :as group.events]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.transport.core :as transport]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.message.v1.public-chat :as public-chat]
            [status-im.transport.message.v1.group-chat :as group-chat]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.chat.events.commands :as events.commands]
            status-im.chat.events.requests
            status-im.chat.events.send-message
            status-im.chat.events.receive-message
            status-im.chat.events.console
            status-im.chat.events.webview-bridge))

;;;; Effects

(re-frame/reg-fx
 :browse
 (fn [link]
   (list-selection/browse link)))

;;;; Handlers

(handlers/register-handler-db
 :set-chat-ui-props
 [re-frame/trim-v]
 (fn [db [kvs]]
   (models/set-chat-ui-props db kvs)))

(handlers/register-handler-db
 :toggle-chat-ui-props
 [re-frame/trim-v]
 (fn [db [ui-element]]
   (models/toggle-chat-ui-prop db ui-element)))

(handlers/register-handler-db
 :show-message-details
 [re-frame/trim-v]
 (fn [db [details]]
   (models/set-chat-ui-props db {:show-bottom-info? true
                                 :bottom-info       details})))

(handlers/register-handler-db
 :show-message-options
 [re-frame/trim-v]
 (fn [db [options]]
   (models/set-chat-ui-props db {:show-message-options? true
                                 :message-options       options})))

(def index-messages (partial into {} (map (juxt :message-id identity))))

(handlers/register-handler-fx
 :load-more-messages
 [(re-frame/inject-cofx :data-store/get-messages)]
 (fn [{{:keys [current-chat-id] :as db} :db get-stored-messages :get-stored-messages} _]
   (when-not (get-in db [:chats current-chat-id :all-loaded?])
     (let [loaded-count (count (get-in db [:chats current-chat-id :messages]))
           new-messages (index-messages (get-stored-messages current-chat-id loaded-count))]
       {:db (-> db
                (update-in [:chats current-chat-id :messages] merge new-messages)
                (update-in [:chats current-chat-id :not-loaded-message-ids] #(apply disj % (keys new-messages)))
                (assoc-in [:chats current-chat-id :all-loaded?]
                          (> constants/default-number-of-messages (count new-messages))))}))))

(handlers/register-handler-db
 :message-appeared
 [re-frame/trim-v]
 (fn [db [{:keys [chat-id message-id]}]]
   (update-in db [:chats chat-id :messages message-id] assoc :appearing? false)))

(handlers/register-handler-fx
 :update-message-status
 [re-frame/trim-v]
 (fn [{:keys [db]} [chat-id message-id user-id status]]
   (let [msg-path [:chats chat-id :messages message-id]
         new-db   (update-in db (conj msg-path :user-statuses) assoc user-id status)]
     {:db            new-db
      :data-store/tx [(messages-store/update-message-tx
                       (-> (get-in new-db msg-path)
                           (select-keys [:message-id :user-statuses])))]})))

(handlers/register-handler-fx
 :transport/set-message-envelope-hash
 [re-frame/trim-v]
 ;; message-type is used for tracking
 (fn [{:keys [db]} [chat-id message-id message-type envelope-hash]]
   {:db (assoc-in db [:transport/message-envelopes envelope-hash] {:chat-id    chat-id
                                                                   :message-id message-id})}))

(handlers/register-handler-fx
 :signals/envelope-status
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} [envelope-hash status]]
   (let [{:keys [chat-id message-id]} (get-in db [:transport/message-envelopes envelope-hash])
         message (get-in db [:chats chat-id :messages message-id])
         {:keys [fcm-token]} (get-in db [:contacts/contacts chat-id])]
     (handlers-macro/merge-fx cofx
                              (models.message/update-message-status message status)
                              (models.message/send-push-notification fcm-token status)))))

;; Change status of messages which are still in "sending" status to "not-sent"
;; (If signal from status-go has not been received)
(handlers/register-handler-fx
 :process-pending-messages
 [re-frame/trim-v]
 (fn [{:keys [db]} []]
   (let [pending-messages (->> db
                               :chats
                               vals
                               (mapcat (comp vals :messages))
                               (filter (fn [{:keys [from user-statuses]}] (= :sending (get user-statuses from)))))
         updated-messages (map (fn [{:keys [from] :as message}]
                                 (assoc-in message [:user-statuses from] :not-sent))
                               pending-messages)]
     {:data-store/tx [(messages-store/update-messages-tx updated-messages)]
      :db            (reduce (fn [m {:keys [chat-id message-id from]}]
                               (assoc-in m [:chats chat-id :messages message-id
                                            :user-statuses from] :not-sent))
                             db
                             pending-messages)})))

(defn init-console-chat
  [{:keys [db] :as cofx}]
  (when-not (get-in db [:chats constants/console-chat-id])
    {:db            (-> db
                        (assoc :current-chat-id constants/console-chat-id)
                        (update :chats assoc constants/console-chat-id console/chat))
     :data-store/tx [(chats-store/save-chat-tx console/chat)]}))

(defn- add-default-contacts
  [{:keys [db default-contacts] :as cofx}]
  (let [new-contacts      (-> {}
                              (into (map (fn [[id props]]
                                           (let [contact-id (name id)]
                                             [contact-id {:whisper-identity contact-id
                                                          :address          (utils.contacts/public-key->address contact-id)
                                                          :name             (-> props :name :en)
                                                          :photo-path       (:photo-path props)
                                                          :public-key       (:public-key props)
                                                          :unremovable?     (-> props :unremovable? boolean)
                                                          :hide-contact?    (-> props :hide-contact? boolean)
                                                          :pending?         (-> props :pending? boolean)
                                                          :dapp?            (:dapp? props)
                                                          :dapp-url         (-> props :dapp-url :en)
                                                          :bot-url          (:bot-url props)
                                                          :description      (:description props)}])))
                                    default-contacts)
                              (assoc constants/console-chat-id console/contact))
        existing-contacts (:contacts/contacts db)
        contacts-to-add   (select-keys new-contacts (set/difference (set (keys new-contacts))
                                                                    (set (keys existing-contacts))))]
    (handlers-macro/merge-fx cofx
                             {:db            (update db :contacts/contacts merge contacts-to-add)
                              :data-store/tx [(contacts-store/save-contacts-tx
                                               (vals contacts-to-add))]}
                             (events.loading/load-commands))))

(handlers/register-handler-fx
 :initialize-chats
 [(re-frame/inject-cofx :get-default-contacts)
  (re-frame/inject-cofx :data-store/all-chats)
  (re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/unviewed-messages)
  (re-frame/inject-cofx :data-store/message-ids)
  (re-frame/inject-cofx :data-store/get-unanswered-requests)
  (re-frame/inject-cofx :data-store/get-local-storage-data)]
 (fn [{:keys [db
              all-stored-chats
              stored-unanswered-requests
              get-stored-messages
              stored-unviewed-messages
              stored-message-ids] :as cofx} _]
   (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                             (assoc-in acc [chat-id message-id] request))
                                           {}
                                           stored-unanswered-requests)
         chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                         (let [chat-messages (index-messages (get-stored-messages chat-id))]
                           (assoc acc chat-id
                                  (assoc chat
                                         :unviewed-messages (get stored-unviewed-messages chat-id)
                                         :requests (get chat->message-id->request chat-id)
                                         :messages chat-messages
                                         :not-loaded-message-ids (set/difference (get stored-message-ids chat-id)
                                                                                 (-> chat-messages keys set))))))
                       {}
                       all-stored-chats)]
     (handlers-macro/merge-fx cofx
                              {:db (assoc db :chats chats)}
                              (init-console-chat)
                              (add-default-contacts)))))

(handlers/register-handler-fx
 :browse-link-from-message
 (fn [_ [_ link]]
   {:browse link}))

(defn- persist-seen-messages
  [chat-id unseen-messages-ids {:keys [db]}]
  {:data-store/tx [(messages-store/update-messages-tx
                    (map (fn [message-id]
                           (-> (get-in db [:chats chat-id :messages message-id])
                               (select-keys [:message-id :user-statuses])))
                         unseen-messages-ids))]})

(defn- send-messages-seen [chat-id message-ids {:keys [db] :as cofx}]
  (when (and (not (get-in db [:chats chat-id :public?]))
             (seq message-ids)
             (not (models/bot-only-chat? db chat-id)))
    (transport.message/send (protocol/map->MessagesSeen {:message-ids message-ids}) chat-id cofx)))

;;TODO (yenda) find a more elegant solution for system messages
(defn- mark-messages-seen
  [chat-id {:keys [db] :as cofx}]
  (let [me                         (:current-public-key db)
        messages-path              [:chats chat-id :messages]
        unseen-messages-ids        (into #{}
                                         (comp (filter (fn [[_ {:keys [from user-statuses outgoing]}]]
                                                         (and (not outgoing)
                                                              (not= constants/system from)
                                                              (not= (get user-statuses me) :seen))))
                                               (map first))
                                         (get-in db messages-path))
        unseen-system-messages-ids (into #{}
                                         (comp (filter (fn [[_ {:keys [from user-statuses]}]]
                                                         (and (= constants/system from)
                                                              (not= (get user-statuses me) :seen))))
                                               (map first))
                                         (get-in db messages-path))]
    (when (or (seq unseen-messages-ids)
              (seq unseen-system-messages-ids))
      (handlers-macro/merge-fx cofx
                               {:db (-> (reduce (fn [new-db message-id]
                                                  (assoc-in new-db (into messages-path [message-id :user-statuses me]) :seen))
                                                db
                                                (into unseen-messages-ids unseen-system-messages-ids))
                                        (update-in [:chats chat-id :unviewed-messages] set/difference unseen-messages-ids unseen-system-messages-ids))}
                               (persist-seen-messages chat-id (into unseen-messages-ids unseen-system-messages-ids))
                               (send-messages-seen chat-id unseen-messages-ids)))))

(defn- fire-off-chat-loaded-event
  [chat-id {:keys [db]}]
  (when-let [event (get-in db [:chats chat-id :chat-loaded-event])]
    {:db       (update-in db [:chats chat-id] dissoc :chat-loaded-event)
     :dispatch event}))

(defn- preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [chat-id {:keys [db] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db (-> (assoc db :current-chat-id chat-id)
                                    (models/set-chat-ui-props {:validation-messages nil}))}
                           (fire-off-chat-loaded-event chat-id)
                           (mark-messages-seen chat-id)))

(handlers/register-handler-fx
 :add-chat-loaded-event
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} [chat-id event]]
   (if (get (:chats db) chat-id)
     {:db (assoc-in db [:chats chat-id :chat-loaded-event] event)}
     (-> (models/upsert-chat {:chat-id chat-id} cofx) ; chat not created yet, we have to create it
         (assoc-in [:db :chats chat-id :chat-loaded-event] event)))))

(defn- navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  [chat-id {:keys [navigation-replace?]} {:keys [db] :as cofx}]
  (if navigation-replace?
    (handlers-macro/merge-fx cofx
                             (navigation/replace-view :chat)
                             (preload-chat-data chat-id))
    (handlers-macro/merge-fx cofx
                             ;; TODO janherich - refactor `navigate-to` so it can be used with `merge-fx` macro
                             {:db (navigation/navigate-to db :chat)}
                             (preload-chat-data chat-id))))

(handlers/register-handler-fx
 :navigate-to-chat
 [re-frame/trim-v]
 (fn [cofx [chat-id opts]]
   (navigate-to-chat chat-id opts cofx)))

(handlers/register-handler-fx
 :execute-stored-command-and-return-to-chat
 (fn [cofx [_ chat-id]]
   (handlers-macro/merge-fx cofx
                            (events.commands/execute-stored-command)
                            (navigate-to-chat chat-id {}))))

(defn start-chat
  "Start a chat, making sure it exists"
  [chat-id opts {:keys [db] :as cofx}]
  ;; don't allow to open chat with yourself
  (when (not= (:current-public-key db) chat-id)
    (handlers-macro/merge-fx cofx
                             (models/upsert-chat {:chat-id chat-id
                                                  :is-active true})
                             (navigate-to-chat chat-id opts))))

(handlers/register-handler-fx
 :start-chat
 [re-frame/trim-v]
 (fn [cofx [contact-id opts]]
   (start-chat contact-id opts cofx)))

;; TODO(janherich): remove this unnecessary event in the future (only model function `update-chat` will stay)
(handlers/register-handler-fx
 :update-chat!
 [re-frame/trim-v]
 (fn [cofx [chat]]
   (models/upsert-chat chat cofx)))

(defn- remove-transport [chat-id {:keys [db] :as cofx}]
  (let [{:keys [group-chat public?]} (get-in db [:chats chat-id])]
    ;; if this is private group chat, we have to broadcast leave and unsubscribe after that
    (if (and group-chat (not public?))
      (handlers-macro/merge-fx cofx (transport.message/send (group-chat/GroupLeave.) chat-id))
      (handlers-macro/merge-fx cofx (transport/unsubscribe-from-chat chat-id)))))

(handlers/register-handler-fx
 :leave-chat-and-navigate-home
 [re-frame/trim-v]
 (fn [cofx [chat-id]]
   (handlers-macro/merge-fx cofx
                            (models/remove-chat chat-id)
                            (navigation/replace-view :home)
                            (remove-transport chat-id))))

(handlers/register-handler-fx
 :leave-group-chat?
 [re-frame/trim-v]
 (fn [_ [chat-id]]
   {:show-confirmation {:title               (i18n/label :t/leave-confirmation)
                        :content             (i18n/label :t/leave-group-chat-confirmation)
                        :confirm-button-text (i18n/label :t/leave)
                        :on-accept           #(re-frame/dispatch [:leave-chat-and-navigate-home chat-id])}}))

(handlers/register-handler-fx
 :remove-chat-and-navigate-home
 [re-frame/trim-v]
 (fn [cofx [chat-id]]
   (handlers-macro/merge-fx cofx
                            (models/remove-chat chat-id)
                            (navigation/replace-view :home))))

(handlers/register-handler-fx
 :remove-chat-and-navigate-home?
 [re-frame/trim-v]
 (fn [_ [chat-id group?]]
   {:show-confirmation {:title               (i18n/label :t/delete-confirmation)
                        :content             (i18n/label (if group? :t/delete-group-chat-confirmation :t/delete-chat-confirmation))
                        :confirm-button-text (i18n/label :t/delete)
                        :on-accept           #(re-frame/dispatch [:remove-chat-and-navigate-home chat-id])}}))

(handlers/register-handler-fx
 :create-new-public-chat
 [re-frame/trim-v]
 (fn [{:keys [db now] :as cofx} [topic]]
   (handlers-macro/merge-fx cofx
                            (models/add-public-chat topic)
                            (navigation/navigate-to-clean :home)
                            (navigate-to-chat topic {})
                            (public-chat/join-public-chat topic))))

(defn- group-name-from-contacts [selected-contacts all-contacts username]
  (->> selected-contacts
       (map (comp :name (partial get all-contacts)))
       (cons username)
       (string/join ", ")))

(handlers/register-handler-fx
 :create-new-group-chat-and-open
 [re-frame/trim-v (re-frame/inject-cofx :random-id)]
 (fn [{:keys [db now random-id] :as cofx} [group-name]]
   (let [selected-contacts (:group/selected-contacts db)
         chat-name         (if-not (string/blank? group-name)
                             group-name
                             (group-name-from-contacts selected-contacts
                                                       (:contacts/contacts db)
                                                       (:username db)))]
     (handlers-macro/merge-fx cofx
                              {:db (assoc db :group/selected-contacts #{})}
                              (models/add-group-chat random-id chat-name (:current-public-key db) selected-contacts)
                              (navigation/navigate-to-clean :home)
                              (navigate-to-chat random-id {})
                              (transport.message/send (group-chat/GroupAdminUpdate. chat-name selected-contacts) random-id)))))

(handlers/register-handler-fx
 :show-profile
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} [identity]]
   (handlers-macro/merge-fx cofx
                            {:db (assoc db :contacts/identity identity)}
                            (navigation/navigate-forget :profile))))

(handlers/register-handler-fx
 :resend-message
 [re-frame/trim-v]
 (fn [cofx [chat-id message-id]]
   (models.message/resend-message chat-id message-id cofx)))

(handlers/register-handler-fx
 :delete-message
 [re-frame/trim-v]
 (fn [cofx [chat-id message-id]]
   (models.message/delete-message chat-id message-id cofx)))

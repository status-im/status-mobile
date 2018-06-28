(ns status-im.chat.events
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.chat.models :as models]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.console :as console]
            [status-im.commands.events.loading :as events.loading]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.utils.utils :as utils]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.message.v1.public-chat :as public-chat]
            [status-im.transport.message.v1.group-chat :as group-chat]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.chat.events.commands :as events.commands]
            status-im.chat.events.requests
            status-im.chat.events.send-message
            status-im.chat.events.receive-message
            status-im.chat.events.console))

;;;; Effects


(re-frame/reg-fx
 :show-cooldown-warning
 (fn [_]
   (utils/show-popup nil
                     (i18n/label :cooldown/warning-message)
                     #())))

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

(handlers/register-handler-db
 :message-appeared
 [re-frame/trim-v]
 (fn [db [{:keys [chat-id message-id]}]]
   (update-in db [:chats chat-id :messages message-id] assoc :appearing? false)))

(handlers/register-handler-fx
 :update-message-status
 [re-frame/trim-v]
 (fn [{:keys [db]} [chat-id message-id user-id status]]
   (let [new-status {:chat-id          chat-id
                     :message-id       message-id
                     :whisper-identity user-id
                     :status           status}]
     {:db            (assoc-in db
                               [:chats chat-id :message-statuses message-id user-id]
                               new-status)
      :data-store/tx [(user-statuses-store/save-status-tx new-status)]})))

;; Change status of own messages which are still in "sending" status to "not-sent"
;; (If signal from status-go has not been received)
(handlers/register-handler-fx
 :process-pending-messages
 [re-frame/trim-v]
 (fn [{:keys [db]} []]
   (let [me               (:current-public-key db)
         pending-statuses (->> (vals (:chats db))
                               (mapcat :message-statuses)
                               (mapcat (fn [[_ user-id->status]]
                                         (filter (comp (partial = :sending) :status)
                                                 (get user-id->status me)))))
         updated-statuses (map #(assoc % :status :not-sent) pending-statuses)]
     {:data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]
      :db            (reduce
                      (fn [acc {:keys [chat-id message-id status whisper-identity]}]
                        (assoc-in acc
                                  [:chats chat-id :message-status message-id
                                   whisper-identity :status]
                                  status))
                      db
                      updated-statuses)})))

(defn init-console-chat
  [{:keys [db]}]
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

(defn- group-chat-messages
  [{:keys [db]}]
  (reduce-kv (fn [fx chat-id {:keys [messages]}]
               (models.message/group-messages chat-id (vals messages) fx))
             {:db db}
             (:chats db)))

(handlers/register-handler-fx
 :initialize-chats
 [(re-frame/inject-cofx :get-default-contacts)
  (re-frame/inject-cofx :get-default-dapps)
  (re-frame/inject-cofx :data-store/all-chats)
  (re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/get-user-statuses)
  (re-frame/inject-cofx :data-store/unviewed-messages)
  (re-frame/inject-cofx :data-store/message-ids)
  (re-frame/inject-cofx :data-store/get-unanswered-requests)
  (re-frame/inject-cofx :data-store/get-local-storage-data)]
 (fn [{:keys [db
              default-dapps
              all-stored-chats
              stored-unanswered-requests
              get-stored-messages
              get-stored-user-statuses
              stored-unviewed-messages
              stored-message-ids] :as cofx} _]
   (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                             (assoc-in acc [chat-id message-id] request))
                                           {}
                                           stored-unanswered-requests)
         chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                         (let [chat-messages (index-messages (get-stored-messages chat-id))
                               message-ids   (keys chat-messages)
                               unviewed-ids  (get stored-unviewed-messages chat-id)]
                           (assoc acc chat-id
                                  (assoc chat
                                         :unviewed-messages unviewed-ids
                                         :requests (get chat->message-id->request chat-id)
                                         :messages chat-messages
                                         :message-statuses (get-stored-user-statuses chat-id message-ids)
                                         :not-loaded-message-ids (set/difference (get stored-message-ids chat-id)
                                                                                 (set message-ids))))))
                       {}
                       all-stored-chats)]
     (handlers-macro/merge-fx cofx
                              {:db (assoc db
                                          :chats          chats
                                          :contacts/dapps default-dapps)}
                              (init-console-chat)
                              (group-chat-messages)
                              (add-default-contacts)))))

(defn- send-messages-seen [chat-id message-ids {:keys [db] :as cofx}]
  (when (and (not (get-in db [:chats chat-id :public?]))
             (not (models/bot-only-chat? db chat-id)))
    (transport.message/send (protocol/map->MessagesSeen {:message-ids message-ids}) chat-id cofx)))

;; TODO (janherich) - ressurect `constants/system` messages for group chats in the future
(defn mark-messages-seen
  [chat-id {:keys [db] :as cofx}]
  (when-let [all-unviewed-ids (seq (get-in db [:chats chat-id :unviewed-messages]))]
    (let [me                  (:current-public-key db)
          updated-statuses    (keep (fn [message-id]
                                      (some-> db
                                              (get-in [:chats chat-id :message-statuses
                                                       message-id me])
                                              (assoc :status :seen)))
                                    all-unviewed-ids)
          loaded-unviewed-ids (map :message-id updated-statuses)]
      (when (seq loaded-unviewed-ids)
        (handlers-macro/merge-fx
         cofx
         {:db            (-> (reduce (fn [acc {:keys [message-id status]}]
                                       (assoc-in acc [:chats chat-id :message-statuses
                                                      message-id me :status]
                                                 status))
                                     db
                                     updated-statuses)
                             (update-in [:chats chat-id :unviewed-messages]
                                        #(apply disj % loaded-unviewed-ids)))
          :data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]}
         (send-messages-seen chat-id loaded-unviewed-ids))))))

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
 :load-more-messages
 [(re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/get-user-statuses)]
 (fn [{{:keys [current-chat-id] :as db} :db
       get-stored-messages :get-stored-messages
       get-stored-user-statuses :get-stored-user-statuses :as cofx} _]
   (when-not (get-in db [:chats current-chat-id :all-loaded?])
     (let [loaded-count     (count (get-in db [:chats current-chat-id :messages]))
           new-messages     (get-stored-messages current-chat-id loaded-count)
           indexed-messages (index-messages new-messages)
           new-message-ids  (keys indexed-messages)
           new-statuses     (get-stored-user-statuses current-chat-id new-message-ids)]
       (handlers-macro/merge-fx
        cofx
        {:db (-> db
                 (update-in [:chats current-chat-id :messages] merge indexed-messages)
                 (update-in [:chats current-chat-id :message-statuses] merge new-statuses)
                 (update-in [:chats current-chat-id :not-loaded-message-ids]
                            #(apply disj % new-message-ids))
                 (assoc-in [:chats current-chat-id :all-loaded?]
                           (> constants/default-number-of-messages (count new-messages))))}
        (models.message/group-messages current-chat-id new-messages)
        (mark-messages-seen current-chat-id))))))

(handlers/register-handler-fx
 :execute-stored-command
 (fn [cofx _]
   (handlers-macro/merge-fx cofx
                            (events.commands/execute-stored-command)
                            (navigation/replace-view :wallet-transaction-sent))))

(handlers/register-handler-fx
 :execute-stored-command-and-return-to-chat
 (fn [cofx [_ chat-id]]
   (handlers-macro/merge-fx cofx
                            (events.commands/execute-stored-command)
                            (navigate-to-chat chat-id {:navigation-replace? true}))))

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

(defn remove-chat-and-navigate-home [cofx [chat-id]]
  (handlers-macro/merge-fx cofx
                           (models/remove-chat chat-id)
                           (navigation/replace-view :home)))

(handlers/register-handler-fx
 :remove-chat-and-navigate-home
 [re-frame/trim-v]
 remove-chat-and-navigate-home)

(handlers/register-handler-fx
 :remove-chat-and-navigate-home?
 [re-frame/trim-v]
 (fn [_ [chat-id group?]]
   {:show-confirmation {:title               (i18n/label :t/delete-confirmation)
                        :content             (i18n/label :t/delete-chat-confirmation)
                        :confirm-button-text (i18n/label :t/delete)
                        :on-accept           #(re-frame/dispatch [:remove-chat-and-navigate-home chat-id])}}))

(handlers/register-handler-fx
 :clear-history
 (fn [{{:keys [current-chat-id]} :db :as cofx} _]
   (models/clear-history current-chat-id cofx)))

(handlers/register-handler-fx
 :clear-history?
 (fn [_ _]
   {:show-confirmation {:title               (i18n/label :t/clear-history-confirmation)
                        :content             (i18n/label :t/clear-history-confirmation-content)
                        :confirm-button-text (i18n/label :t/clear)
                        :on-accept           #(re-frame/dispatch [:clear-history])}}))

(defn create-new-public-chat [topic {:keys [db now] :as cofx}]
  (handlers-macro/merge-fx cofx
                           (models/add-public-chat topic)
                           (navigation/navigate-to-clean :home)
                           (navigate-to-chat topic {})
                           (public-chat/join-public-chat topic)))

(handlers/register-handler-fx
 :create-new-public-chat
 [re-frame/trim-v]
 (fn [cofx [topic]]
   (create-new-public-chat topic cofx)))

(defn- group-name-from-contacts [selected-contacts all-contacts username]
  (->> selected-contacts
       (map (comp :name (partial get all-contacts)))
       (cons username)
       (string/join ", ")))

(handlers/register-handler-fx
 :create-new-group-chat-and-open
 [re-frame/trim-v (re-frame/inject-cofx :random-id)]
 (fn [{:keys [db random-id] :as cofx} [group-name]]
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

(defn show-profile [identity {:keys [db] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db (assoc db :contacts/identity identity)}
                           (navigation/navigate-forget :profile)))

(handlers/register-handler-fx
 :show-profile
 [re-frame/trim-v]
 (fn [cofx [identity]]
   (show-profile identity cofx)))

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

(handlers/register-handler-db
 :disable-cooldown
 [re-frame/trim-v]
 (fn [db]
   (assoc db :chat/cooldown-enabled? false)))

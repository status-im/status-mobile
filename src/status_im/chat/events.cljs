(ns status-im.chat.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as models]
            [status-im.chat.models.message :as models.message]
            [status-im.constants :as constants]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.i18n :as i18n]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.group-chat :as group-chat]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.message.v1.public-chat :as public-chat]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.utils :as utils]))

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

(defn show-profile [identity keep-navigation? {:keys [db] :as cofx}]
  (cond->> {:db (assoc db :contacts/identity identity)}
    keep-navigation? (navigation/navigate-to-cofx :profile nil)
    :else            (navigation/navigate-forget :profile)))

(handlers/register-handler-fx
 :show-profile
 [re-frame/trim-v]
 (fn [cofx [identity keep-navigation?]]
   (show-profile identity keep-navigation? cofx)))

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

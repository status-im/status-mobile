(ns status-im.chat.events
  (:require status-im.chat.events.input
            status-im.chat.events.send-message
            status-im.chat.events.receive-message
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as models]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message :as models.message]
            [status-im.constants :as constants]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.i18n :as i18n]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.group-chat :as group-chat]
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

(handlers/register-handler-fx
 :set-chat-ui-props
 (fn [{:keys [db]} [_ kvs]]
   {:db (models/set-chat-ui-props db kvs)}))

(handlers/register-handler-fx
 :toggle-chat-ui-props
 (fn [{:keys [db]} [_ ui-element]]
   {:db (models/toggle-chat-ui-prop db ui-element)}))

(handlers/register-handler-fx
 :show-message-details
 (fn [{:keys [db]} [_ details]]
   {:db (models/set-chat-ui-props db {:show-bottom-info? true
                                      :bottom-info       details})}))

(handlers/register-handler-fx
 :show-message-options
 (fn [{:keys [db]} [_ options]]
   {:db (models/set-chat-ui-props db {:show-message-options? true
                                      :message-options       options})}))

(handlers/register-handler-fx
 :update-message-status
 (fn [{:keys [db]} [_ chat-id message-id user-id status]]
   (let [new-status {:chat-id          chat-id
                     :message-id       message-id
                     :whisper-identity user-id
                     :status           status}]
     {:db            (assoc-in db
                               [:chats chat-id :message-statuses message-id user-id]
                               new-status)
      :data-store/tx [(user-statuses-store/save-status-tx new-status)]})))

(handlers/register-handler-fx
 :navigate-to-chat
 (fn [cofx [_ chat-id opts]]
   (models/navigate-to-chat chat-id opts cofx)))

(handlers/register-handler-fx
 :load-more-messages
 [(re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/get-user-statuses)]
 (fn [cofx _]
   (chat-loading/load-more-messages cofx)))

(handlers/register-handler-fx
 :start-chat
 (fn [cofx [_ contact-id opts]]
   (models/start-chat contact-id opts cofx)))

(defn remove-chat-and-navigate-home [cofx [_ chat-id]]
  (handlers-macro/merge-fx cofx
                           (models/remove-chat chat-id)
                           (navigation/replace-view :home)))

(handlers/register-handler-fx
 :remove-chat-and-navigate-home
 remove-chat-and-navigate-home)

(handlers/register-handler-fx
 :remove-chat-and-navigate-home?
 (fn [_ [_ chat-id group?]]
   {:ui/show-confirmation {:title               (i18n/label :t/delete-confirmation)
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
   {:ui/show-confirmation {:title               (i18n/label :t/clear-history-confirmation)
                           :content             (i18n/label :t/clear-history-confirmation-content)
                           :confirm-button-text (i18n/label :t/clear)
                           :on-accept           #(re-frame/dispatch [:clear-history])}}))

(defn create-new-public-chat [topic cofx]
  (handlers-macro/merge-fx cofx
                           (models/add-public-chat topic)
                           (models/navigate-to-chat topic {:navigation-replace? true})
                           (public-chat/join-public-chat topic)))

(handlers/register-handler-fx
 :create-new-public-chat
 (fn [cofx [_ topic]]
   (create-new-public-chat topic cofx)))

(defn- group-name-from-contacts [selected-contacts all-contacts username]
  (->> selected-contacts
       (map (comp :name (partial get all-contacts)))
       (cons username)
       (string/join ", ")))

(handlers/register-handler-fx
 :create-new-group-chat-and-open
 [(re-frame/inject-cofx :random-id)]
 (fn [{:keys [db random-id] :as cofx} [_ group-name]]
   (let [selected-contacts (:group/selected-contacts db)
         chat-name         (if-not (string/blank? group-name)
                             group-name
                             (group-name-from-contacts selected-contacts
                                                       (:contacts/contacts db)
                                                       (:username db)))]
     (handlers-macro/merge-fx
      cofx
      {:db (assoc db :group/selected-contacts #{})}
      (models/add-group-chat random-id chat-name (:current-public-key db) selected-contacts)
      (navigation/navigate-to-cofx :home nil)
      (models/navigate-to-chat random-id {})
      (transport.message/send (group-chat/GroupAdminUpdate. chat-name selected-contacts) random-id)))))

(defn show-profile [identity {:keys [db]}]
  (navigation/navigate-to-cofx :profile nil {:db (assoc db :contacts/identity identity)}))

(handlers/register-handler-fx
 :show-profile
 (fn [cofx [_ identity]]
   (show-profile identity cofx)))

(handlers/register-handler-fx
 :resend-message
 (fn [cofx [_ chat-id message-id]]
   (models.message/resend-message chat-id message-id cofx)))

(handlers/register-handler-fx
 :delete-message
 (fn [cofx [_ chat-id message-id]]
   (models.message/delete-message chat-id message-id cofx)))

(handlers/register-handler-fx
 :disable-cooldown
 (fn [{:keys [db]}]
   {:db (assoc db :chat/cooldown-enabled? false)}))

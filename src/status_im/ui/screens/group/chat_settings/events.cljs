(ns status-im.ui.screens.group.chat-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.chat.models.message :as models.message]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.transport.message.v1.group-chat :as group-chat]
            [status-im.transport.message.core :as transport]
            [status-im.utils.handlers :as handlers]))


;;;; Handlers

(handlers/register-handler-fx
  :show-group-chat-profile
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [chat-id]] 
    {:db (-> db
             (assoc :new-chat-name (get-in db [:chats chat-id :name])
                    :group/group-type :chat-group)
             (navigation/navigate-to :group-chat-profile))}))

(handlers/register-handler-fx
  :add-new-group-chat-participants
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:keys [current-chat-id selected-participants] :as db} :db now :now message-id :random-id :as cofx} _]
    (let [participants             (concat (get-in db [:chats current-chat-id :contacts]) selected-participants)
          contacts                 (:contacts/contacts db)
          added-participants-names (map #(get-in contacts [% :name]) selected-participants)]
      (handlers/merge-fx cofx
                         {:db (-> db
                                  (assoc-in [:chats current-chat-id :contacts] participants)
                                  (assoc :selected-participants #{}))
                          :data-store/add-chat-contacts (select-keys db [:current-chat-id :selected-participants])}
                         (models.message/receive
                          (models.message/system-message current-chat-id message-id now
                                                         (str "You've added " (apply str (interpose ", " added-participants-names)))))
                         (transport/send (group-chat/GroupAdminUpdate. nil participants) current-chat-id)))))

(handlers/register-handler-fx
  :remove-group-chat-participants
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [{{:keys [current-chat-id] :as db} :db now :now message-id :random-id :as cofx} [removed-participants]]
    (let [participants               (remove removed-participants (get-in db [:chats current-chat-id :contacts]))
          contacts                   (:contacts/contacts db)
          removed-participants-names (map #(get-in contacts [% :name]) removed-participants)]
      (handlers/merge-fx cofx
                         {:db (assoc-in db [:chats current-chat-id :contacts] participants)
                          :data-store/remove-chat-contacts [current-chat-id removed-participants]}
                         (models.message/receive
                          (models.message/system-message current-chat-id message-id now
                                                         (str "You've removed " (apply str (interpose ", " removed-participants-names)))))
                         (transport/send (group-chat/GroupAdminUpdate. nil participants) current-chat-id)))))

(handlers/register-handler-fx
  :set-group-chat-name
  (fn [{{:keys [current-chat-id] :as db} :db} [_ new-chat-name]]
    {:db                            (assoc-in db [:chats current-chat-id :name] new-chat-name)
     :data-store/save-chat-property [current-chat-id :name new-chat-name]}))

(handlers/register-handler-fx
  :clear-history
  (fn [{{:keys [current-chat-id] :as db} :db} _]
    {:db                       (assoc-in db [:chats current-chat-id :messages] {})
     :data-store/hide-messages current-chat-id}))

(handlers/register-handler-fx
  :clear-history?
  (fn [_ _]
    {:show-confirmation {:title               (i18n/label :t/clear-history-confirmation)
                         :content             (i18n/label :t/clear-group-history-confirmation)
                         :confirm-button-text (i18n/label :t/clear)
                         :on-accept           #(re-frame/dispatch [:clear-history])}}))

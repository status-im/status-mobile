(ns status-im.ui.screens.group.chat-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.models :as models.chat]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.transport.message.v1.group-chat :as group-chat]
            [status-im.transport.message.core :as transport]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]))

(handlers/register-handler-fx
 :show-group-chat-profile
 [re-frame/trim-v]
 (fn [{:keys [db]} [chat-id]]
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
     (handlers-macro/merge-fx cofx
                              {:db            (-> db
                                                  (assoc-in [:chats current-chat-id :contacts] participants)
                                                  (assoc :selected-participants #{}))
                               :data-store/tx [(chats-store/add-chat-contacts-tx current-chat-id selected-participants)]}
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
     (handlers-macro/merge-fx cofx
                              {:db            (assoc-in db [:chats current-chat-id :contacts] participants)
                               :data-store/tx [(chats-store/remove-chat-contacts-tx current-chat-id removed-participants)]}
                              (models.message/receive
                               (models.message/system-message current-chat-id message-id now
                                                              (str "You've removed " (apply str (interpose ", " removed-participants-names)))))
                              (transport/send (group-chat/GroupAdminUpdate. nil participants) current-chat-id)))))

(handlers/register-handler-fx
 :set-group-chat-name
 (fn [{{:keys [current-chat-id] :as db} :db} [_ new-chat-name]]
   {:db            (assoc-in db [:chats current-chat-id :name] new-chat-name)
    :data-store/tx [(chats-store/save-chat-tx {:chat-id current-chat-id
                                               :name    new-chat-name})]}))

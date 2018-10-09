(ns status-im.ui.screens.group.chat-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.chat.models.message :as models.message]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.chats :as chats-store]
            [status-im.utils.fx :as fx]))

(handlers/register-handler-fx
 :show-group-chat-profile
 (fn [{:keys [db] :as cofx} [_ chat-id]]
   (fx/merge cofx
             {:db (assoc db
                         :new-chat-name (get-in db [:chats chat-id :name]))}
             (navigation/navigate-to-cofx :group-chat-profile nil))))

(handlers/register-handler-fx
 :add-new-group-chat-participants
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{{:keys [current-chat-id selected-participants] :as db} :db
       now :now random-id-generator :random-id-generator :as cofx} _]
   (let [message-id               (random-id-generator)
         participants             (concat (get-in db [:chats current-chat-id :contacts]) selected-participants)
         contacts                 (:contacts/contacts db)
         added-participants-names (map #(get-in contacts [% :name]) selected-participants)]
     (fx/merge cofx
               {:db            (-> db
                                   (assoc-in [:chats current-chat-id :contacts] participants)
                                   (assoc :selected-participants #{}))
                :data-store/tx [(chats-store/add-chat-contacts-tx current-chat-id selected-participants)]}
               #_(models.message/receive
                  (models.message/system-message current-chat-id message-id now
                                                 (str "You've added " (apply str (interpose ", " added-participants-names)))))
               #_(transport/send (protocol/GroupAdminUpdate. nil participants current-chat-id) current-chat-id)))))

(handlers/register-handler-fx
 :remove-group-chat-participants
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{{:keys [current-chat-id] :as db} :db now :now random-id-generator :random-id-generator :as cofx}
      [_ removed-participants]]
   (let [message-id                 (random-id-generator)
         participants               (remove removed-participants (get-in db [:chats current-chat-id :contacts]))
         contacts                   (:contacts/contacts db)
         removed-participants-names (map #(get-in contacts [% :name]) removed-participants)]
     (fx/merge cofx
               {:db            (assoc-in db [:chats current-chat-id :contacts] participants)
                :data-store/tx [(chats-store/remove-chat-contacts-tx current-chat-id removed-participants)]}
               #_(models.message/receive
                  (models.message/system-message current-chat-id message-id now
                                                 (str "You've removed " (apply str (interpose ", " removed-participants-names)))))
               #_(transport/send (protocol/GroupAdminUpdate. nil participants current-chat-id) current-chat-id)))))

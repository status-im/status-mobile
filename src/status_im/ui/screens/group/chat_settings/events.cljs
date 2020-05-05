(ns status-im.ui.screens.group.chat-settings.events
  (:require [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :show-group-chat-profile
 (fn [{:keys [db] :as cofx} [_ chat-id]]
   (fx/merge cofx
             {:db (-> db
                      (assoc :new-chat-name (get-in db [:chats chat-id :name]))
                      (assoc :current-chat-id chat-id))}
             (navigation/navigate-to-cofx :group-chat-profile nil))))

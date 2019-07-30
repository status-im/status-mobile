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
             {:db (-> db
                      (assoc :new-chat-name (get-in db [:chats chat-id :name]))
                      (assoc :current-chat-id chat-id))}
             (navigation/navigate-to-cofx :group-chat-profile nil))))


(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.navigation :as navigation]))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [{:keys [db] :as cofx} [_ tab-name]]
   {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)}))

(handlers/register-handler-fx
 :navigate-public-chat-profile
 (fn [{:keys [db] :as cofx} [_ whisper-identity]]
   {:db (-> db
            (assoc :contacts/identity whisper-identity)
            (navigation/navigate-to :chat-profile))}))

(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [{:keys [db] :as cofx} [_ tab-name]]
   {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)}))

(handlers/register-handler-fx
 :set-public-chat-whisper-identity
 (fn [{:keys [db] :as cofx} [_ whisper-identity]]
   {:db (assoc db :contacts/identity whisper-identity)}))

(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [{:keys [db] :as cofx} [_ tab-name]]
   {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)
    :dispatch [:navigate-to (if (and (= tab-name :home) (:current-chat-id db))
                              :chat
                              :home)]}))

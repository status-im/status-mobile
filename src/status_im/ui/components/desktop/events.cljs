(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [{:keys [db] :as cofx} [_ tab-name]]
   (merge {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)
           :dispatch [:navigate-to (if (and (= tab-name :home) (:current-chat-id db))
                                     :chat
                                     :home)]}
          (when (= tab-name :profile)
            {:http-get
             {:url
              "https://raw.githubusercontent.com/status-im/status-im.github.io/develop/env.sh"
              :success-event-creator
              (fn [o]
                [:fetch-desktop-version-success o])}}))))

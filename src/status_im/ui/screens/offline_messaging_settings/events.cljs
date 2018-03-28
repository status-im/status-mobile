(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.i18n :as i18n]))

(handlers/register-handler-fx
  ::save-wnode
  (fn [{:keys [db now]} [_ wnode]]
    (-> (accounts-events/account-update {:db db}
                                        {:wnode wnode :last-updated now})
     (merge {:dispatch    [:navigate-to-clean :accounts]
             :stop-whisper nil}))))

(handlers/register-handler-fx
  :connect-wnode
  (fn [{:keys [db]} [_ wnode]]
    {:show-confirmation {:title               (i18n/label :t/close-app-title)
                         :content             (i18n/label :t/connect-wnode-content
                                                          {:name (get-in db [:inbox/wnodes wnode :name])})
                         :confirm-button-text (i18n/label :t/close-app-button)
                         :on-accept           #(dispatch [::save-wnode wnode])
                         :on-cancel           nil}}))

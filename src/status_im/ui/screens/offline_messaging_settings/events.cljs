(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.models.fleet :as fleet]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.i18n :as i18n]
            status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
            [status-im.utils.ethereum.core :as ethereum]))

(handlers/register-handler-fx
 ::save-wnode
 (fn [{:keys [db now] :as cofx} [_ current-fleet wnode]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              (accounts.models/update-settings
                               (assoc-in settings [:wnode current-fleet] wnode)
                               [:logout])))))

(handlers/register-handler-fx
 :connect-wnode
 (fn [{:keys [db]} [_ wnode]]
   (let [current-fleet (fleet/current-fleet db)]
     {:show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/connect-wnode-content
                                                           {:name (get-in db [:inbox/wnodes  current-fleet wnode :name])})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [::save-wnode current-fleet wnode])
                          :on-cancel           nil}})))

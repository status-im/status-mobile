(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.i18n :as i18n]
            [status-im.transport.core :as transport]
            status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
            [status-im.utils.ethereum.core :as ethereum]))

(handlers/register-handler-fx
 ::save-wnode
 (fn [{:keys [db now] :as cofx} [_ chain wnode]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              (accounts-events/update-settings
                               (assoc-in settings [:wnode chain] wnode)
                               [:logout])))))

(handlers/register-handler-fx
 :connect-wnode
 (fn [{:keys [db]} [_ wnode]]
   (let [network (get (:networks (:account/account db)) (:network db))
         chain   (ethereum/network->chain-keyword network)]
     {:show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/connect-wnode-content
                                                           {:name (get-in db [:inbox/wnodes chain wnode :name])})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [::save-wnode chain wnode])
                          :on-cancel           nil}})))


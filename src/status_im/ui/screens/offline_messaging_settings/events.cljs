(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.constants :as constants]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.models.mailserver :as models.mailserver]
            [status-im.transport.inbox :as inbox]
            [status-im.i18n :as i18n]
            status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
            [status-im.utils.ethereum.core :as ethereum]))

(handlers/register-handler-fx
 ::update-wnode
 (fn [{:keys [db now] :as cofx} [_ chain wnode]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              (accounts.models/update-settings
                               (assoc-in settings [:wnode chain] wnode))
                              (models.mailserver/set-current-mailserver)
                              (inbox/connect-to-mailserver)))))

(handlers/register-handler-fx
 :wnode/disconnect
 (fn [{:keys [db] :as cofx} [_ chain newnode]]
   (let [{:keys [address] :as wnode} (models.mailserver/fetch-current cofx)
         args {:jsonrpc "2.0"
               :id      2
               :method  constants/admin-remove-peer
               :params  [address]}
         payload (.stringify js/JSON (clj->js args))]
     (status/call-private-rpc payload
                              (inbox/response-handler
                               ethereum/handle-error
                               #(re-frame/dispatch [::update-wnode chain newnode]))))))

(handlers/register-handler-fx
 :connect-wnode
 (fn [{:keys [db]} [_ wnode]]
   (let [network (get (:networks (:account/account db)) (:network db))
         chain   (ethereum/network->chain-keyword network)]
     {:show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/connect-wnode-content
                                                           {:name (get-in db [:inbox/wnodes chain wnode :name])})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [:wnode/disconnect chain wnode])
                          :on-cancel           nil}})))


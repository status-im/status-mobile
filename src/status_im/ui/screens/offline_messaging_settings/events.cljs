(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.i18n :as i18n]
            [status-im.transport.core :as transport]
            [status-im.utils.ethereum.core :as ethereum]))

(handlers/register-handler-fx
 ::save-wnode
 (fn [{:keys [db now] :as cofx} [_ wnode]]
   (let [network  (ethereum/network->chain-keyword (:network db))
         settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              {:dispatch [:logout]}
                              (accounts-events/update-settings (assoc-in settings [:wnode network] wnode))))))

(handlers/register-handler-fx
 :connect-wnode
 (fn [{:keys [db]} [_ wnode]]
   (let [network (ethereum/network->chain-keyword (:network db))]
     {:show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/connect-wnode-content
                                                           {:name (get-in db [:inbox/wnodes network wnode :name])})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(dispatch [::save-wnode wnode])
                          :on-cancel           nil}})))

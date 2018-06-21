(ns status-im.ui.screens.network-settings.events
  (:require [re-frame.core :refer [dispatch dispatch-sync after] :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            status-im.ui.screens.network-settings.edit-network.events
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as utils]
            [status-im.transport.core :as transport]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [clojure.string :as string]))

;; handlers

(handlers/register-handler-fx
 :close-application
 (fn [_ _]
   {:close-application nil}))

(handlers/register-handler-fx
 ::save-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (handlers-macro/merge-fx cofx
                            (accounts.utils/account-update {:network      network
                                                            :last-updated now}
                                                           [::close-application]))))

(handlers/register-handler-fx
 ::remove-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (let [networks         (dissoc (get-in db [:account/account :networks]) network)]
     (handlers-macro/merge-fx cofx
                              {:dispatch [:navigate-back]}
                              (accounts.utils/account-update {:networks     networks
                                                              :last-updated now})))))

(handlers/register-handler-fx
 :connect-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (let [current-network (get-in db [:account/account :networks (:network db)])
         chats           (:transport/chats db)]
     (if (utils/network-with-upstream-rpc? current-network)
       (handlers-macro/merge-fx cofx
                                (accounts.utils/account-update {:network      network
                                                                :last-updated now}
                                                               [:logout]))
       {:show-confirmation {:title               (i18n/label :t/close-app-title)
                            :content             (i18n/label :t/close-app-content)
                            :confirm-button-text (i18n/label :t/close-app-button)
                            :on-accept           #(dispatch [::save-network network])
                            :on-cancel           nil}}))))

(handlers/register-handler-fx
 :delete-network
 (fn [{{:account/keys [account] :as db} :db :as cofx} [_ network]]
   (let [current-network? (= (:network account) network)]
     (if current-network?
       {:show-error (i18n/label :t/delete-network-error)}
       {:show-confirmation {:title               (i18n/label :t/delete-network-title)
                            :content             (i18n/label :t/delete-network-confirmation)
                            :confirm-button-text (i18n/label :t/delete)
                            :on-accept           #(dispatch [::remove-network network])
                            :on-cancel           nil}}))))

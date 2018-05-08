(ns status-im.ui.screens.network-settings.events
  (:require [re-frame.core :refer [dispatch dispatch-sync after] :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as utils]
            [status-im.transport.core :as transport]))

;; handlers

(handlers/register-handler-fx
 :add-networks
 (fn [{{:networks/keys [networks] :as db} :db} [_ new-networks]]
   (let [identities    (set (keys networks))
         new-networks' (->> new-networks
                            (remove #(identities (:id %)))
                            (map #(vector (:id %) %))
                            (into {}))]
     {:db            (-> db
                         (update :networks merge new-networks')
                         (assoc :new-networks (vals new-networks')))
      :save-networks new-networks'})))

(handlers/register-handler-fx
 ::close-application
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
 :connect-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (let [current-network (:network db)
         networks        (:networks/networks db)
         chats           (:transport/chats db)]
     (if (utils/network-with-upstream-rpc? networks current-network)
       (handlers-macro/merge-fx cofx
                                {:dispatch-n            [[:load-accounts]
                                                         [:navigate-to-clean :accounts]]}
                                (transport/stop-whisper)
                                (accounts.utils/account-update {:network      network
                                                                :last-updated now}))
       {:show-confirmation {:title               (i18n/label :t/close-app-title)
                            :content             (i18n/label :t/close-app-content)
                            :confirm-button-text (i18n/label :t/close-app-button)
                            :on-accept           #(dispatch [::save-network network])
                            :on-cancel           nil}}))))

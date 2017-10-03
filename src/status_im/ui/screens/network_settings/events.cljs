(ns status-im.ui.screens.network-settings.events
  (:require [re-frame.core :refer [dispatch dispatch-sync after] :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.data-store.networks :as networks]
            [status-im.ui.screens.network-settings.navigation]
            [status-im.ui.screens.accounts.events :as accounts-events]))

;;;; FX

(re-frame/reg-fx
  ::save-networks
  (fn [networks]
    (networks/save-all networks)))

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
  :connect-network
  (fn [cofx [_ network]]
    (merge (accounts-events/account-update cofx {:network network})
           {:dispatch-n [[:navigate-to-clean :accounts]]})))

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

(defn network-with-upstream-rpc? [networks network]
  (get-in networks [network :raw-config :UpstreamConfig :Enabled]))

(defn connect-network [cofx [_ network]]
  (merge (accounts-events/account-update cofx {:network network})
         {:close-application nil}))

(handlers/register-handler-fx ::save-network connect-network)

(handlers/register-handler-fx
  :connect-network
  (fn [{:keys [db] :as cofx} [_ network]]
    (let [current-network (:network db)
          networks        (:networks/networks db)]
      (if (and (network-with-upstream-rpc? networks current-network)
               (network-with-upstream-rpc? networks network))
        (merge (accounts-events/account-update cofx {:network network})
               {:dispatch     [:navigate-to-clean :accounts]
                :stop-whisper nil})
        {:show-confirmation {:title               "Warning!"
                             :content             "App will be closed. When you restart it selected network will be used."
                             :confirm-button-text "Confirm"
                             :on-accept           #(dispatch [::save-network network])
                             :on-cancel           nil}}))))

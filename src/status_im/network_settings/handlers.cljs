(ns status-im.network-settings.handlers
  (:require [re-frame.core :refer [dispatch dispatch-sync after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.data-store.networks :as networks]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.types :as t]
            [status-im.components.status :as status]))

(defn save-networks! [{:keys [new-networks]} _]
  (networks/save-all new-networks))

(defn add-new-networks
  [{:keys [networks] :as db} [_ new-networks]]
  (let [identities  (set (keys networks))
        new-networks' (->> new-networks
                         (remove #(identities (:id %)))
                         (map #(vector (:id %) %))
                         (into {}))]
    (-> db
        (update :networks merge new-networks')
        (assoc :new-networks (vals new-networks')))))

(register-handler :add-networks
  (after save-networks!)
  add-new-networks)

(defn generate-config [network-id data-dir]
  (t/clj->json {:NetworkId network-id :DataDir data-dir}))

(register-handler :load-default-networks!
  (fn [db _]
   (let [default-networks (mapv
                            (fn [[id {:keys [network-id name data-dir]}]]
                              {:id     (clojure.core/name id)
                               :name   name
                               :config (generate-config network-id data-dir)})
                            js-res/default-networks)
         new-networks' (->> default-networks
                            (map #(vector (:id %) %))
                            (into {}))]
     (assoc db :networks new-networks'))))

(register-handler :connect-network
  (u/side-effect!
    (fn [{:keys [current-account-id networks]} [_ network]]
      (dispatch [:account-update {:network network}])
      (status/stop-node
        (fn []
          (let [config (get-in networks [network :config])]
            (status/start-node config (fn [])))))
      (dispatch [:navigate-to-clean :accounts]))))

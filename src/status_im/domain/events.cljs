(ns status-im.domain.events
  (:require
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn- update-saved-addresses
  [saved-addresses-db new-saved-addresses]
  (reduce
   (fn [acc {:keys [address removed? test?] :as saved-address}]
     (let [db-key (if test? :test :prod)]
       (if removed?
         (update acc db-key dissoc address)
         (assoc-in acc [db-key address] saved-address))))
   (or saved-addresses-db
       {:test {}
        :prod {}})
   new-saved-addresses))

(defn reconcile-saved-addresses
  [{:keys [db]} [saved-addresses]]
  {:db (update-in db [:wallet :saved-addresses] update-saved-addresses saved-addresses)})

(rf/reg-event-fx :domain/reconcile-saved-addresses reconcile-saved-addresses)

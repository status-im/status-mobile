(ns status-im.ui.screens.wallet.transactions.events
  (:require [status-im.utils.handlers :as handlers]))

(defn- mark-all-checked [filters]
  (update filters
          :type
          #(map (fn [m]
                  (assoc m :checked? true))
                %)))

(defn- mark-checked [filters {:keys [type]} checked?]
  (update filters
          :type
          #(map (fn [{:keys [id] :as m}]
                  (if (= type id)
                    (assoc m :checked? checked?)
                    m))
                %)))

(defn- update-filters [db f]
  (update-in db [:wallet.transactions :filters] f))

(handlers/register-handler-fx
 :wallet.transactions/filter
 (fn [{:keys [db]} [_ path checked?]]
   {:db (update-filters db #(mark-checked % path checked?))}))

(handlers/register-handler-fx
 :wallet.transactions/filter-all
 (fn [{:keys [db]} _]
   {:db (update-filters db mark-all-checked)}))

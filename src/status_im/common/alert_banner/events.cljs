(ns status-im.common.alert-banner.events
  (:require [re-frame.core :as re-frame]))

(defn add-alert-banner
  [{:keys [db]} [banner]]
  (let [current-banners-count (count (get db :alert-banners))
        db                    (assoc-in db [:alert-banners (:type banner)] banner)]
    (cond-> {:db db}
      (zero? current-banners-count)
      (assoc :show-alert-banner [(:view-id db) (:theme db)]))))

(defn remove-alert-banner
  [{:keys [db]} [banner-type]]
  (let [db        (update-in db [:alert-banners] dissoc banner-type)
        new-count (count (get db :alert-banners))]
    (cond-> {:db db}
      (zero? new-count)
      (assoc :hide-alert-banner [(:view-id db) (:theme db)]))))

(defn remove-all-alert-banners
  [{:keys [db]}]
  {:db                (dissoc db :alert-banners)
   :hide-alert-banner [(:view-id db) (:theme db)]})

(re-frame/reg-event-fx :alert-banners/add add-alert-banner)
(re-frame/reg-event-fx :alert-banners/remove remove-alert-banner)
(re-frame/reg-event-fx :alert-banners/remove-all remove-all-alert-banners)

(ns status-im.mailserver.subs
  (:require
   [status-im.mailserver.core :as mailserver]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :mailserver/state
 (fn [db]
   (get db :mailserver/state)))

(re-frame/reg-sub
 :mailserver/pending-requests
 (fn [db]
   (get db :mailserver/pending-requests)))

(re-frame/reg-sub
 :mailserver/connecting?
 :<- [:mailserver/state]
 (fn [state]
   (#{:connecting :added} state)))

(re-frame/reg-sub
 :mailserver/connection-error?
 :<- [:mailserver/state]
 (fn [state]
   (#{:error :disconnected} state)))

(re-frame/reg-sub
 :mailserver/request-error?
 (fn [db]
   (get db :mailserver/request-error)))

(re-frame/reg-sub
 :mailserver/fetching?
 :<- [:mailserver/state]
 :<- [:mailserver/pending-requests]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 (fn [[state pending-requests connecting? connection-error? request-error?]]
   (and pending-requests
        (= state :connected)
        (pos-int? pending-requests)
        (not (or connecting? connection-error? request-error?)))))

(re-frame/reg-sub
 :mailserver/connected?
 (fn [db]
   (let [connected? (= :connected (:mailserver/state db))
         online?    (= :online (:network-status db))]
     (and connected? online?))))

(re-frame/reg-sub
 :mailserver/current-id
 (fn [db]
   (:mailserver/current-id db)))

(re-frame/reg-sub
 :mailserver/preferred-id
 (fn [db]
   (mailserver/preferred-mailserver-id {:db db})))

(re-frame/reg-sub
 :mailserver/mailservers
 (fn [db]
   (:mailserver/mailservers db)))

(re-frame/reg-sub
 :mailserver/fleet-mailservers
 :<- [:settings/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-fleet mailservers]]
   (current-fleet mailservers)))

(re-frame/reg-sub
 :mailserver.edit/mailserver
 (fn [db]
   (get db :mailserver.edit/mailserver)))

(re-frame/reg-sub
 :mailserver.edit/connected?
 :<- [:mailserver.edit/mailserver]
 :<- [:mailserver/current-id]
 (fn [[mailserver current-mailserver-id]]
   (= (get-in mailserver [:id :value])
      current-mailserver-id)))

(re-frame/reg-sub
 :mailserver.edit/validation-errors
 :<- [:mailserver.edit/mailserver]
 (fn [mailserver]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         mailserver))))

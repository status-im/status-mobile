(ns legacy.status-im.subs.mailservers
  (:require
    [legacy.status-im.fleet.core :as fleet]
    [legacy.status-im.mailserver.core :as mailserver]
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :mailserver/current-name
 :<- [:mailserver/current-id]
 :<- [:fleets/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-mailserver-id current-fleet mailservers]]
   (get-in mailservers [current-fleet current-mailserver-id :name])))

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
 :chats/fetching-gap-in-progress?
 :<- [:mailserver/fetching-gaps-in-progress]
 (fn [gaps [_ ids _]]
   (seq (select-keys gaps ids))))

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
 :mailserver/fleet-mailservers
 :<- [:fleets/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-fleet mailservers]]
   (current-fleet mailservers)))

(re-frame/reg-sub
 :mailserver.edit/connected?
 :<- [:mailserver.edit/mailserver]
 :<- [:mailserver/current-id]
 (fn [[mailserver current-mailserver-id]]
   (= (get-in mailserver [:id :value])
      current-mailserver-id)))

(re-frame/reg-sub
 :mailserver/use-status-nodes?
 (fn [db _]
   (boolean (mailserver/fetch-use-mailservers? {:db db}))))

(re-frame/reg-sub
 :mailserver.edit/validation-errors
 :<- [:mailserver.edit/mailserver]
 (fn [mailserver]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         mailserver))))

(re-frame/reg-sub
 :mailserver/connected?
 :<- [:mailserver/state]
 :<- [:disconnected?]
 (fn [[mail-state disconnected?]]
   (let [mailserver-connected? (= :connected mail-state)]
     (and mailserver-connected?
          (not disconnected?)))))

(re-frame/reg-sub
 :mailserver/preferred-id
 :<- [:profile/profile]
 (fn [multiaccount]
   (get-in multiaccount
           [:pinned-mailservers (fleet/current-fleet-sub multiaccount)])))

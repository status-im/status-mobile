(ns status-im2.subs.activity-center
  (:require [re-frame.core :as re-frame]
            [status-im2.contexts.activity-center.notification-types :as types]))

(re-frame/reg-sub
 :activity-center/notifications
 :<- [:activity-center]
 (fn [activity-center]
   (:notifications activity-center)))

(re-frame/reg-sub
 :activity-center/unread-count
 :<- [:activity-center]
 (fn [activity-center]
   (:unread-count activity-center)))

(re-frame/reg-sub
 :activity-center/filter-status
 :<- [:activity-center]
 (fn [activity-center]
   (get-in activity-center [:filter :status])))

(re-frame/reg-sub
 :activity-center/filter-type
 :<- [:activity-center]
 (fn [activity-center]
   (get-in activity-center [:filter :type] types/no-type)))

(re-frame/reg-sub
 :activity-center/filtered-notifications
 :<- [:activity-center/filter-type]
 :<- [:activity-center/filter-status]
 :<- [:activity-center/notifications]
 (fn [[filter-type filter-status notifications]]
   (get-in notifications [filter-type filter-status :data])))

(re-frame/reg-sub
 :activity-center/filter-status-unread-enabled?
 :<- [:activity-center/filter-status]
 (fn [filter-status]
   (= :unread filter-status)))

(re-frame/reg-sub
 :activity-center/pending-contact-requests
 :<- [:activity-center/notifications]
 (fn [notifications]
   (get-in notifications [types/contact-request :unread :data])))

(ns status-im2.subs.activity-center
  (:require
    [malli.core :as malli]
    malli.util
    [re-frame.core :as re-frame]
    [status-im2.contexts.shell.activity-center.notification-types :as types]
    utils.schema))

(re-frame/reg-sub
 :activity-center/notifications
 :<- [:activity-center]
 (fn [activity-center]
   (:notifications activity-center)))

(re-frame/reg-sub
 :activity-center/unread-counts-by-type
 :<- [:activity-center]
 (utils.schema/instrument
  :activity-center/unread-counts-by-type
  [:=>
   [:cat (malli.util/get (malli/deref :schema.re-frame/db) :activity-center) :any]
   [:map-of {:min 1} :schema.shell/notification-type :int]]
  (fn [activity-center]
    (:unread-counts-by-type activity-center))))

(re-frame/reg-sub
 :activity-center/notification-types-with-unread
 :<- [:activity-center/unread-counts-by-type]
 (fn [unread-counts]
   (reduce-kv
    (fn [acc notification-type unread-count]
      (if (pos? unread-count)
        (conj acc notification-type)
        acc))
    #{}
    unread-counts)))

(re-frame/reg-sub
 :activity-center/unread-count
 :<- [:activity-center/unread-counts-by-type]
 (fn [unread-counts]
   (->> unread-counts
        vals
        (reduce + 0))))

(re-frame/reg-sub
 :activity-center/seen?
 :<- [:activity-center]
 (fn [activity-center]
   (:seen? activity-center)))

(re-frame/reg-sub
 :activity-center/unread-indicator
 :<- [:activity-center/seen?]
 :<- [:activity-center/unread-count]
 (fn [[seen? unread-count]]
   (cond
     (zero? unread-count) :unread-indicator/none
     seen?                :unread-indicator/seen
     :else                :unread-indicator/new)))

(re-frame/reg-sub
 :activity-center/mark-all-as-read-undoable-till
 :<- [:activity-center]
 (fn [activity-center]
   (:mark-all-as-read-undoable-till activity-center)))

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
 :activity-center/filter-status-unread-enabled?
 :<- [:activity-center/filter-status]
 (fn [filter-status]
   (= :unread filter-status)))

(re-frame/reg-sub
 :activity-center/pending-contact-requests
 :<- [:activity-center]
 (fn [activity-center]
   (:contact-requests activity-center)))

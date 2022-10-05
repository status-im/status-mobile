(ns status-im.subs.activity-center
  (:require [re-frame.core :as re-frame]
            [status-im.utils.datetime :as datetime]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.constants :as constants]
            [clojure.string :as string]))

(re-frame/reg-sub
 :activity-center/notifications-read
 (fn [db]
   (get-in db [:activity-center :notifications-read :data])))

(re-frame/reg-sub
 :activity-center/notifications-unread
 (fn [db]
   (get-in db [:activity-center :notifications-unread :data])))

(re-frame/reg-sub
 :activity-center/current-status-filter
 (fn [db]
   (get-in db [:activity-center :current-status-filter])))

(re-frame/reg-sub
 :activity-center/status-filter-unread-enabled?
 :<- [:activity-center/current-status-filter]
 (fn [current-status-filter]
   (= :unread current-status-filter)))

(re-frame/reg-sub
 :activity-center/notifications-per-read-status
 :<- [:activity-center/notifications-read]
 :<- [:activity-center/notifications-unread]
 :<- [:activity-center/status-filter-unread-enabled?]
 (fn [[notifications-read notifications-unread unread-filter-enabled?]]
   (if unread-filter-enabled?
     notifications-unread
     notifications-read)))

(defn- group-notifications-by-date
  [notifications]
  (->> notifications
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key >)
       (map (fn [[date-key notifications]]
              (let [first-notification (first notifications)]
                {:title (string/capitalize (datetime/day-relative (:timestamp first-notification)))
                 :key   date-key
                 :data  (sort-by :timestamp > notifications)})))))

(re-frame/reg-sub
 :activity.center/notifications-grouped-by-date
 :<- [:activity.center/notifications]
 :<- [:contacts/contacts]
 (fn [[{:keys [notifications]} contacts]]
   (let [supported-notifications
         (filter (fn [{:keys [type last-message message]}]
                   (or (and (= constants/activity-center-notification-type-one-to-one-chat type)
                            (not (nil? last-message)))
                       (and (= constants/activity-center-notification-type-contact-request type)
                            (not= constants/contact-request-message-state-none
                                  (-> contacts
                                      (multiaccounts/contact-by-identity (:from message))
                                      :contact-request-state)))
                       (= constants/activity-center-notification-type-contact-request-retracted type)
                       (= constants/activity-center-notification-type-private-group-chat type)
                       (= constants/activity-center-notification-type-reply type)
                       (= constants/activity-center-notification-type-mention type)))
                 notifications)]
     (group-notifications-by-date
      (map #(assoc %
                   :timestamp (or (:timestamp %) (:timestamp (or (:message %) (:last-message %))))
                   :contact (multiaccounts/contact-by-identity contacts (get-in % [:message :from])))
           supported-notifications)))))

(ns status-im.subs.activity-center
  (:require [re-frame.core :as re-frame]
            [status-im.utils.datetime :as datetime]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.constants :as constants]
            [clojure.string :as string]))

(re-frame/reg-sub
 :activity-center/notifications
 :<- [:activity-center]
 (fn [activity-center]
   (:notifications activity-center)))

(re-frame/reg-sub
 :activity-center/filter-status
 :<- [:activity-center]
 (fn [activity-center]
   (get-in activity-center [:filter :status])))

(re-frame/reg-sub
 :activity-center/filter-type
 :<- [:activity-center]
 (fn [activity-center]
   (get-in activity-center [:filter :type] constants/activity-center-notification-type-no-type)))

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

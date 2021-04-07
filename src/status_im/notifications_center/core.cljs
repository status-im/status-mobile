(ns status-im.notifications-center.core
  (:require [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.data-store.activities :as data-store.activities]))

(fx/defn handle-activities [{:keys [db]} activities]
  (if (= (:view-id db) :notifications-center)
    {:db (-> db
             (update-in [:activity.center/notifications :notifications] #(concat activities %)))
     :dispatch [:mark-all-activity-center-notifications-as-read]}
    {:db (-> db
             (update :activity.center/notifications-count + (count activities)))}))

(fx/defn get-activity-center-notifications-count
  {:events [:get-activity-center-notifications-count]}
  [_]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "unreadActivityCenterNotificationsCount")
                     :params     []
                     :on-success #(re-frame/dispatch [:get-activity-center-notifications-count-success %])
                     :on-error   #()}]})

(fx/defn get-activity-center-notifications-count-success
  {:events [:get-activity-center-notifications-count-success]}
  [{:keys [db]} result]
  {:db (assoc db :activity.center/notifications-count result)})

(fx/defn dismiss-activity-center-notifications
  {:events [:dismiss-activity-center-notifications]}
  [{:keys [db]} ids]
  {:db (update-in db [:activity.center/notifications :notifications]
                  (fn [items] (remove #(get ids (:id %)) items)))
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "dismissActivityCenterNotifications")
                     :params     [ids]
                     :on-success #()
                     :on-error   #()}]})

(fx/defn accept-activity-center-notifications
  {:events [:accept-activity-center-notifications]}
  [{:keys [db]} ids]
  {:db (update-in db [:activity.center/notifications :notifications]
                  (fn [items] (remove #(get ids (:id %)) items)))
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "acceptActivityCenterNotifications")
                     :params     [ids]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #()}]})

(fx/defn accept-activity-center-notification-and-open-chat
  {:events [:accept-activity-center-notification-and-open-chat]}
  [{:keys [db]} id]
  {:db             (update-in db [:activity.center/notifications :notifications]
                              (fn [items] (remove #(= id (:id %)) items)))
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "acceptActivityCenterNotifications")
                     :params     [[id]]
                     :js-response true
                     :on-success #(re-frame/dispatch [:ensure-and-open-chat %])
                     :on-error   #()}]})

(fx/defn ensure-and-open-chat
  {:events [:ensure-and-open-chat]}
  [{:keys [db]} response-js]
  {:db (update db :activity.center/notifications dissoc :cursor)
   :dispatch-n [[:sanitize-messages-and-process-response response-js]
                [:chat.ui/navigate-to-chat (.-id (aget (.-chats response-js) 0))]]})

(fx/defn dismiss-all-activity-center-notifications
  {:events [:dismiss-all-activity-center-notifications]}
  [{:keys [db]}]
  {:db (assoc-in db [:activity.center/notifications :notifications] [])
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "dismissAllActivityCenterNotifications")
                     :params     []
                     :on-success #()
                     :on-error   #()}]})

(fx/defn accept-all-activity-center-notifications
  {:events [:accept-all-activity-center-notifications]}
  [{:keys [db]}]
  {:db (assoc-in db [:activity.center/notifications :notifications] [])
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "acceptAllActivityCenterNotifications")
                     :params     []
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #()}]})

(fx/defn mark-all-activity-center-notifications-as-read
  {:events [:mark-all-activity-center-notifications-as-read]}
  [{:keys [db]}]
  {:db (assoc db :activity.center/notifications-count 0)
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "markAllActivityCenterNotificationsRead")
                     :params     []
                     :on-success #()
                     :on-error   #()}]})

(fx/defn get-activity-center-notifications
  {:events [:get-activity-center-notifications]}
  [{:keys [db]}]
  (let [{:keys [cursor]} (:activity.center/notifications db)]
    (when (not= cursor "")
      {::json-rpc/call [{:method     (json-rpc/call-ext-method "activityCenterNotifications")
                         :params     [cursor 20]
                         :on-success #(re-frame/dispatch [:activity-center-notifications-success %])
                         :on-error   #(log/warn "failed to get notification center activities" %)}]})))

(fx/defn activity-center-notifications-success
  {:events [:activity-center-notifications-success]}
  [{:keys [db]} {:keys [cursor notifications]}]
  {:db (-> db
           (assoc-in [:activity.center/notifications :cursor] cursor)
           (update-in [:activity.center/notifications :notifications]
                      concat
                      (map data-store.activities/<-rpc notifications)))})

(fx/defn close-center
  {:events [:close-notifications-center]}
  [{:keys [db]}]
  {:db (dissoc db :activity.center/notifications)})

(ns status-im.notifications-center.core
  (:require [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.activity-center.notification-types :as types]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.data-store.activities :as data-store.activities]))

(def non-dismissable-notifications
  #{types/contact-request
    types/contact-request-retracted})

(fx/defn accept-all-activity-center-notifications-from-chat
  {:events [:accept-all-activity-center-notifications-from-chat]}
  [{:keys [db]} chat-id]
  (let [notifications (get-in db [:activity.center/notifications :notifications])
        notifications-from-chat (filter #(and
                                          (= chat-id (:chat-id %))
                                          (not (contains? non-dismissable-notifications (:type %))))
                                        notifications)
        notifications-from-chat-not-read (filter #(and (= chat-id (:chat-id %))
                                                       (not (:read %)))
                                                 notifications)
        ids (into #{} (map :id notifications-from-chat))]
    (when (seq ids)
      {:db (-> db
               (update-in [:activity.center/notifications :notifications]
                          (fn [items]
                            (filter
                             #(not (contains? ids (:id %)))
                             items)))
               (update-in [:activity-center :unread-count] - (min (get-in db [:activity-center :unread-count])
                                                                  (count notifications-from-chat-not-read))))
       ::json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                         :params     [ids]
                         :js-response true
                         :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                         :on-error   #(log/info "unable to accept activity center notifications" %)}]})))

(fx/defn mark-all-activity-center-notifications-as-read
  {:events [:mark-all-activity-center-notifications-as-read]}
  [{:keys [db]}]
  {:db             (assoc-in db [:activity-center :unread-count] 0)
   ::json-rpc/call [{:method     "wakuext_markAllActivityCenterNotificationsRead"
                     :params     []
                     :on-success #()
                     :on-error   #()}]})

(fx/defn load-notifications [{:keys [db]} cursor]
  (when-not (:activity.center/loading? db)
    {:db (assoc db :activity.center/loading? true)
     ::json-rpc/call [{:method     "wakuext_activityCenterNotifications"
                       :params     [cursor 20]
                       :on-success #(re-frame/dispatch [:activity-center-notifications-success %])
                       :on-error   #(re-frame/dispatch [:activity-center-notifications-error %])}]}))

(fx/defn clean-notifications [{:keys [db]}]
  {:db (dissoc db :activity.center/notifications)})

(fx/defn get-activity-center-notifications
  {:events [:get-activity-center-notifications]}
  [{:keys [db] :as cofx}]
  (let [{:keys [cursor]} (:activity.center/notifications db)]
    (fx/merge cofx
              (clean-notifications)
              (load-notifications ""))))

(fx/defn load-more-activity-center-notifications
  {:events [:load-more-activity-center-notifications]}
  [{:keys [db] :as cofx}]
  (let [{:keys [cursor]} (:activity.center/notifications db)]
    (when (not= cursor "")
      (load-notifications cofx cursor))))

(fx/defn activity-center-notifications-error
  {:events [:activity-center-notifications-error]}
  [{:keys [db]} error]
  (log/warn "failed to load activity center notifications" error)
  {:db (dissoc db :activity.center/loading?)})

(fx/defn activity-center-notifications-success
  {:events [:activity-center-notifications-success]}
  [{:keys [db]} {:keys [cursor notifications]}]
  {:db (-> db
           (dissoc :activity.center/loading?)
           (assoc-in [:activity.center/notifications :cursor] cursor)
           (update-in [:activity.center/notifications :notifications]
                      concat
                      (map data-store.activities/<-rpc notifications)))})

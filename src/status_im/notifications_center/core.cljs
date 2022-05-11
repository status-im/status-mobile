(ns status-im.notifications-center.core
  (:require [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.data-store.activities :as data-store.activities]))

(fx/defn handle-activities [{:keys [db]} activities]
  (let [{:keys [unread-count notifications]}
        (reduce (fn [acc {:keys [read dismissed accepted] :as notification}]
                  (as-> acc a
                    (if read
                      (update a :unread-count dec)
                      (update a :unread-count inc))

                    (if (or dismissed accepted)
                      (update a :notifications (fn [items] (remove #(= (:id notification) (:id %)) items)))
                      (update a :notifications conj notification))))
                {:unread-count (get db :activity.center/notifications-count 0)
                 :notifications (get-in db [:activity.center/notifications :notifications])}
                activities)]
    (merge
     {:db (-> db
              (assoc-in [:activity.center/notifications :notifications] notifications)
              (assoc :activity.center/notifications-count (max 0 unread-count)))}
     (cond
       (= (:view-id db) :notifications-center)
       {:dispatch [:mark-all-activity-center-notifications-as-read]}

       (= (:view-id db) :chat)
       {:dispatch [:accept-all-activity-center-notifications-from-chat (:current-chat-id db)]}))))

(fx/defn get-activity-center-notifications-count
  {:events [:get-activity-center-notifications-count]}
  [_]
  {::json-rpc/call [{:method     "wakuext_unreadActivityCenterNotificationsCount"
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
   ::json-rpc/call [{:method     "wakuext_dismissActivityCenterNotifications"
                     :params     [ids]
                     :on-success #()
                     :on-error   #()}]})

(fx/defn accept-activity-center-notifications
  {:events [:accept-activity-center-notifications]}
  [{:keys [db]} ids]
  (when (seq ids)
    {:db (update-in db [:activity.center/notifications :notifications]
                    (fn [items] (remove #(get ids (:id %)) items)))
     ::json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                       :params     [ids]
                       :js-response true
                       :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                       :on-error   #(log/info "unable to accept activity center notifications" %)}]}))

(fx/defn accept-all-activity-center-notifications-from-chat
  {:events [:accept-all-activity-center-notifications-from-chat]}
  [{:keys [db]} chat-id]
  (let [notifications (get-in db [:activity.center/notifications :notifications])
        notifications-from-chat (filter #(= chat-id (:chat-id %)) notifications)
        notifications-from-chat-not-read (filter #(and (= chat-id (:chat-id %))
                                                       (not (:read %))) notifications)
        ids (map :id notifications-from-chat)]
    (when (seq ids)
      {:db (-> db
               (update-in [:activity.center/notifications :notifications]
                          (fn [items] (filter #(not (= chat-id (:chat-id %))) items)))
               (update :activity.center/notifications-count - (min (db :activity.center/notifications-count) (count notifications-from-chat-not-read))))
       ::json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                         :params     [ids]
                         :js-response true
                         :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                         :on-error   #(log/info "unable to accept activity center notifications" %)}]})))

(fx/defn accept-activity-center-notification-and-open-chat
  {:events [:accept-activity-center-notification-and-open-chat]}
  [{:keys [db]} id]
  {:db             (update-in db [:activity.center/notifications :notifications]
                              (fn [items] (remove #(= id (:id %)) items)))
   ::json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                     :params     [[id]]
                     :js-response true
                     :on-success #(re-frame/dispatch [:ensure-and-open-chat %])
                     :on-error   #()}]})

(fx/defn ensure-and-open-chat
  {:events [:ensure-and-open-chat]}
  [{:keys [db]} response-js]
  {:dispatch-n [[:sanitize-messages-and-process-response response-js]
                [:chat.ui/navigate-to-chat (.-id (aget (.-chats response-js) 0))]]})

(fx/defn dismiss-all-activity-center-notifications
  {:events [:dismiss-all-activity-center-notifications]}
  [{:keys [db]}]
  {:db (assoc-in db [:activity.center/notifications :notifications] [])
   ::json-rpc/call [{:method     "wakuext_dismissAllActivityCenterNotifications"
                     :params     []
                     :on-success #()
                     :on-error   #()}]})

(fx/defn accept-all-activity-center-notifications
  {:events [:accept-all-activity-center-notifications]}
  [{:keys [db]}]
  {:db (assoc-in db [:activity.center/notifications :notifications] [])
   ::json-rpc/call [{:method     "wakuext_acceptAllActivityCenterNotifications"
                     :params     []
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #()}]})

(fx/defn mark-all-activity-center-notifications-as-read
  {:events [:mark-all-activity-center-notifications-as-read]}
  [{:keys [db]}]
  {:db (assoc db :activity.center/notifications-count 0)
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


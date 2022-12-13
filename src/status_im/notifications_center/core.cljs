(ns status-im.notifications-center.core
  (:require [re-frame.core :as re-frame]
            [status-im.activity-center.notification-types :as types]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

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

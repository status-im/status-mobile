(ns status-im.notifications-center.core
  (:require
   [status-im.ethereum.json-rpc :as json-rpc]
   [status-im.utils.fx :as fx]))

(fx/defn mark-all-activity-center-notifications-as-read
  {:events [:mark-all-activity-center-notifications-as-read]}
  [{:keys [db]}]
  {:db             (assoc-in db [:activity-center :unread-count] 0)
   ::json-rpc/call [{:method     "wakuext_markAllActivityCenterNotificationsRead"
                     :params     []
                     :on-success #()
                     :on-error   #()}]})

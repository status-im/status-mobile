(ns status-im.activity-center.core
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.activities :as data-store.activities]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(def notifications-per-page
  20)

(def start-or-end-cursor
  "")

(defn notifications-group->rpc-method
  [notifications-group]
  (if (= notifications-group :notifications-read)
    "wakuext_readActivityCenterNotifications"
    "wakuext_unreadActivityCenterNotifications"))

(defn notifications-read-status->group
  [status-filter]
  (if (= status-filter :read)
    :notifications-read
    :notifications-unread))

(fx/defn notifications-fetch
  [{:keys [db]} cursor notifications-group]
  (when-not (get-in db [:activity-center notifications-group :loading?])
    {:db             (assoc-in db [:activity-center notifications-group :loading?] true)
     ::json-rpc/call [{:method     (notifications-group->rpc-method notifications-group)
                       :params     [cursor notifications-per-page]
                       :on-success #(re-frame/dispatch [:activity-center/notifications-fetch-success notifications-group %])
                       :on-error   #(re-frame/dispatch [:activity-center/notifications-fetch-error notifications-group %])}]}))

(fx/defn notifications-fetch-first-page
  {:events [:activity-center/notifications-fetch-first-page]}
  [{:keys [db] :as cofx} {:keys [status-filter] :or {status-filter :unread}}]
  (let [notifications-group (notifications-read-status->group status-filter)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:activity-center :current-status-filter] status-filter)
                       (update-in [:activity-center notifications-group] dissoc :loading?)
                       (update-in [:activity-center notifications-group] dissoc :data))}
              (notifications-fetch start-or-end-cursor notifications-group))))

(fx/defn notifications-fetch-next-page
  {:events [:activity-center/notifications-fetch-next-page]}
  [{:keys [db] :as cofx}]
  (let [status-filter       (get-in db [:activity-center :current-status-filter])
        notifications-group (notifications-read-status->group status-filter)
        {:keys [cursor]}    (get-in db [:activity-center notifications-group])]
    (when-not (= cursor start-or-end-cursor)
      (notifications-fetch cofx cursor notifications-group))))

(fx/defn notifications-fetch-success
  {:events [:activity-center/notifications-fetch-success]}
  [{:keys [db]} notifications-group {:keys [cursor notifications]}]
  {:db (-> db
           (update-in [:activity-center notifications-group] dissoc :loading?)
           (assoc-in [:activity-center notifications-group :cursor] cursor)
           (update-in [:activity-center notifications-group :data]
                      concat
                      (map data-store.activities/<-rpc notifications)))})

(fx/defn notifications-fetch-error
  {:events [:activity-center/notifications-fetch-error]}
  [{:keys [db]} notifications-group error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center notifications-group] dissoc :loading?)})

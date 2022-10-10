(ns status-im.activity-center.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.activities :as data-store.activities]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

;;;; Notification reconciliation

(defn- update-notifications
  "Insert `new-notifications` in `db-notifications`.

  Although correct, this is a naive implementation for reconciling notifications
  because for every notification in `new-notifications`, linear scans will be
  performed to remove it and sorting will be performed for every new insertion.
  If the number of existing notifications cached in the app db becomes
  ~excessively~ big, this implementation will probably need to be revisited."
  [db-notifications new-notifications]
  (reduce (fn [acc {:keys [id type read] :as notification}]
            (let [filter-status (if read :read :unread)]
              (cond-> (-> acc
                          (update-in [type :read :data]
                                     (fn [data]
                                       (remove #(= id (:id %)) data)))
                          (update-in [type :unread :data]
                                     (fn [data]
                                       (remove #(= id (:id %)) data))))
                (not (or (:dismissed notification) (:accepted notification)))
                (update-in [type filter-status :data]
                           (fn [data]
                             (->> notification
                                  (conj data)
                                  (sort-by (juxt :timestamp :id))
                                  reverse))))))
          db-notifications
          new-notifications))

(fx/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db (update-in db [:activity-center :notifications]
                    update-notifications new-notifications)}))

;;;; Notifications fetching and pagination

(def defaults
  {:filter-status          :unread
   :filter-type            constants/activity-center-notification-type-no-type
   :notifications-per-page 10})

(def start-or-end-cursor
  "")

(defn- valid-cursor?
  [cursor]
  (and (some? cursor)
       (not= cursor start-or-end-cursor)))

(defn- filter-status->rpc-method
  [filter-status]
  (if (= filter-status :read)
    "wakuext_readActivityCenterNotifications"
    "wakuext_unreadActivityCenterNotifications"))

(fx/defn notifications-fetch
  [{:keys [db]} {:keys [cursor filter-type filter-status reset-data?]}]
  (when-not (get-in db [:activity-center :notifications filter-type filter-status :loading?])
    {:db             (assoc-in db [:activity-center :notifications filter-type filter-status :loading?] true)
     ::json-rpc/call [{:method     (filter-status->rpc-method filter-status)
                       :params     [cursor (defaults :notifications-per-page) filter-type]
                       :on-success #(re-frame/dispatch [:activity-center.notifications/fetch-success filter-type filter-status reset-data? %])
                       :on-error   #(re-frame/dispatch [:activity-center.notifications/fetch-error filter-type filter-status %])}]}))

(fx/defn notifications-fetch-first-page
  {:events [:activity-center.notifications/fetch-first-page]}
  [{:keys [db] :as cofx} {:keys [filter-type filter-status]}]
  (let [filter-type   (or filter-type
                          (get-in db [:activity-center :filter :type]
                                  (defaults :filter-type)))
        filter-status (or filter-status
                          (get-in db [:activity-center :filter :status]
                                  (defaults :filter-status)))]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:activity-center :filter :type] filter-type)
                       (assoc-in [:activity-center :filter :status] filter-status))}
              (notifications-fetch {:cursor        start-or-end-cursor
                                    :filter-type   filter-type
                                    :filter-status filter-status
                                    :reset-data?   true}))))

(fx/defn notifications-fetch-next-page
  {:events [:activity-center.notifications/fetch-next-page]}
  [{:keys [db] :as cofx}]
  (let [{:keys [type status]} (get-in db [:activity-center :filter])
        {:keys [cursor]}      (get-in db [:activity-center :notifications type status])]
    (when (valid-cursor? cursor)
      (notifications-fetch cofx {:cursor        cursor
                                 :filter-type   type
                                 :filter-status status
                                 :reset-data?   false}))))

(fx/defn notifications-fetch-success
  {:events [:activity-center.notifications/fetch-success]}
  [{:keys [db]}
   filter-type
   filter-status
   reset-data?
   {:keys [cursor notifications]}]
  (let [processed (map data-store.activities/<-rpc notifications)]
    {:db (-> db
             (assoc-in [:activity-center :notifications filter-type filter-status :cursor] cursor)
             (update-in [:activity-center :notifications filter-type filter-status] dissoc :loading?)
             (update-in [:activity-center :notifications filter-type filter-status :data]
                        (if reset-data?
                          (constantly processed)
                          #(concat %1 processed))))}))

(fx/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} filter-type filter-status error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center :notifications filter-type filter-status] dissoc :loading?)})

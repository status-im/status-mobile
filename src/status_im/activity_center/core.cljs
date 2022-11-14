(ns status-im.activity-center.core
  (:require [re-frame.core :as rf]
            [status-im.activity-center.notification-types :as types]
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
            (let [filter-status       (if read :read :unread)
                  remove-notification (fn [data]
                                        (remove #(= id (:id %)) data))
                  insert-and-sort     (fn [data]
                                        (->> notification
                                             (conj data)
                                             (sort-by (juxt :timestamp :id))
                                             reverse))]
              (as-> acc $
                (update-in $ [type :read :data] remove-notification)
                (update-in $ [type :unread :data] remove-notification)
                (update-in $ [types/no-type :read :data] remove-notification)
                (update-in $ [types/no-type :unread :data] remove-notification)
                (if (or (:dismissed notification) (:accepted notification))
                  $
                  (-> $ (update-in [type filter-status :data] insert-and-sort)
                      (update-in [types/no-type filter-status :data] insert-and-sort))))))
          db-notifications
          new-notifications))

(fx/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db (update-in db [:activity-center :notifications]
                    update-notifications new-notifications)}))

;;;; Contact verification

(fx/defn contact-verification-decline
  {:events [:activity-center.contact-verification/decline]}
  [_ contact-verification-id]
  {::json-rpc/call [{:method     "wakuext_declineContactVerificationRequest"
                     :params     [contact-verification-id]
                     :on-success #(rf/dispatch [:activity-center.contact-verification/decline-success %])
                     :on-error   #(rf/dispatch [:activity-center.contact-verification/decline-error contact-verification-id %])}]})

(fx/defn contact-verification-decline-success
  {:events [:activity-center.contact-verification/decline-success]}
  [cofx response]
  (->> response
       :activityCenterNotifications
       (map data-store.activities/<-rpc)
       (notifications-reconcile cofx)))

(fx/defn contact-verification-decline-error
  {:events [:activity-center.contact-verification/decline-error]}
  [_ contact-verification-id error]
  (log/warn "Failed to decline contact verification"
            {:contact-verification-id contact-verification-id
             :error                   error})
  nil)

;;;; Notifications fetching and pagination

(def defaults
  {:filter-status          :unread
   :filter-type            types/no-type
   :notifications-per-page 10})

(def start-or-end-cursor
  "")

(defn- valid-cursor?
  [cursor]
  (and (some? cursor)
       (not= cursor start-or-end-cursor)))

(def ^:const status-read 1)
(def ^:const status-unread 2)
(def ^:const status-all 3)

(defn status [filter-status]
  (case filter-status
    :read status-read
    :unread status-unread
    :all status-all
    99))

(fx/defn notifications-fetch
  [{:keys [db]} {:keys [cursor filter-type filter-status reset-data?]}]
  (when-not (get-in db [:activity-center :notifications filter-type filter-status :loading?])
    {:db             (assoc-in db [:activity-center :notifications filter-type filter-status :loading?] true)
     ::json-rpc/call [{:method     "wakuext_activityCenterNotificationsBy"
                       :params     [cursor (defaults :notifications-per-page) filter-type (status filter-status)]
                       :on-success #(rf/dispatch [:activity-center.notifications/fetch-success filter-type filter-status reset-data? %])
                       :on-error   #(rf/dispatch [:activity-center.notifications/fetch-error filter-type filter-status %])}]}))

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

(ns status-im2.contexts.activity-center.events
  (:require [re-frame.core :as rf]
            [status-im.data-store.activities :as data-store.activities]
            [status-im2.common.json-rpc.events :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im2.contexts.activity-center.notification-types :as types]
            [taoensso.timbre :as log]))

(def defaults
  {:filter-status          :unread
   :filter-type            types/no-type
   ;; Choose the maximum number of notifications that *usually/safely* fit on
   ;; most screens, so that the UI doesn't have to needlessly render
   ;; notifications.
   :notifications-per-page 8})

;;;; Navigation

(fx/defn open-activity-center
  {:events [:activity-center/open]}
  [_]
  (rf/dispatch [:show-popover {:view                       :activity-center
                               :style                      {:margin 0}
                               :disable-touchable-overlay? true
                               :blur-view?                 true
                               :blur-view-props            {:blur-amount 20
                                                            :blur-type   :dark}}]))

;;;; Misc

(fx/defn process-notification-failure
  {:events [:activity-center/process-notification-failure]}
  [_ notification-id action error]
  (log/warn (str "Failed to " action)
            {:notification-id notification-id :error error}))

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
            (let [remove-notification (fn [data]
                                        (remove #(= id (:id %)) data))
                  insert-and-sort     (fn [data]
                                        (->> notification
                                             (conj data)
                                             (sort-by (juxt :timestamp :id))
                                             reverse))]
              (as-> acc $
                (update-in $ [type :all :data] remove-notification)
                (update-in $ [types/no-type :all :data] remove-notification)
                (update-in $ [type :unread :data] remove-notification)
                (update-in $ [types/no-type :unread :data] remove-notification)
                (if (or (:dismissed notification) (:accepted notification))
                  $
                  (cond-> (-> $
                              (update-in [type :all :data] insert-and-sort)
                              (update-in [types/no-type :all :data] insert-and-sort))
                    (not read) (update-in [type :unread :data] insert-and-sort)
                    (not read) (update-in [types/no-type :unread :data] insert-and-sort))))))
          db-notifications
          new-notifications))

(fx/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db (update-in db [:activity-center :notifications]
                    update-notifications new-notifications)}))

(fx/defn notifications-reconcile-from-response
  {:events [:activity-center/reconcile-notifications-from-response]}
  [cofx response]
  (->> response
       :activityCenterNotifications
       (map data-store.activities/<-rpc)
       (notifications-reconcile cofx)))

;;;; Mark notifications as read

(defn- get-notification
  [db notification-id]
  (->> (get-in db [:activity-center
                   :notifications
                   (get-in db [:activity-center :filter :type])
                   (get-in db [:activity-center :filter :status])
                   :data])
       (filter #(= notification-id (:id %)))
       first))

(fx/defn mark-as-read
  {:events [:activity-center.notifications/mark-as-read]}
  [{:keys [db]} notification-id]
  (when-let [notification (get-notification db notification-id)]
    {::json-rpc/call [{:method     "wakuext_markActivityCenterNotificationsRead"
                       :params     [[notification-id]]
                       :on-success #(rf/dispatch [:activity-center.notifications/mark-as-read-success notification])
                       :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                  notification-id
                                                  :notification/mark-as-read
                                                  %])}]}))

(fx/defn mark-as-read-success
  {:events [:activity-center.notifications/mark-as-read-success]}
  [cofx notification]
  (notifications-reconcile cofx [(assoc notification :read true)]))

;;;; Contact verification

(fx/defn contact-verification-decline
  {:events [:activity-center.contact-verification/decline]}
  [_ notification-id]
  {::json-rpc/call [{:method     "wakuext_declineContactVerificationRequest"
                     :params     [notification-id]
                     :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response %])
                     :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                notification-id
                                                :contact-verification/decline
                                                %])}]})

(fx/defn contact-verification-reply
  {:events [:activity-center.contact-verification/reply]}
  [_ notification-id reply]
  {::json-rpc/call [{:method     "wakuext_acceptContactVerificationRequest"
                     :params     [notification-id reply]
                     :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response %])
                     :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                notification-id
                                                :contact-verification/reply
                                                %])}]})

(fx/defn contact-verification-mark-as-trusted
  {:events [:activity-center.contact-verification/mark-as-trusted]}
  [_ notification-id]
  {::json-rpc/call [{:method     "wakuext_verifiedTrusted"
                     :params     [{:id notification-id}]
                     :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response %])
                     :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                notification-id
                                                :contact-verification/mark-as-trusted
                                                %])}]})

(fx/defn contact-verification-mark-as-untrustworthy
  {:events [:activity-center.contact-verification/mark-as-untrustworthy]}
  [_ notification-id]
  {::json-rpc/call [{:method     "wakuext_verifiedUntrustworthy"
                     :params     [{:id notification-id}]
                     :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response %])
                     :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                notification-id
                                                :contact-verification/mark-as-untrustworthy
                                                %])}]})

;;;; Notifications fetching and pagination

(def start-or-end-cursor
  "")

(defn- valid-cursor?
  [cursor]
  (and (some? cursor)
       (not= cursor start-or-end-cursor)))

(def ^:const status-unread 2)
(def ^:const status-all 3)

(defn status [filter-status]
  (case filter-status
    :unread status-unread
    :all    status-all
    99))

(fx/defn notifications-fetch
  [{:keys [db]} {:keys [cursor per-page filter-type filter-status reset-data?]}]
  (when-not (get-in db [:activity-center :notifications filter-type filter-status :loading?])
    (let [per-page (or per-page (defaults :notifications-per-page))]
      {:db             (assoc-in db [:activity-center :notifications filter-type filter-status :loading?] true)
       ::json-rpc/call [{:method     "wakuext_activityCenterNotificationsBy"
                         :params     [cursor per-page filter-type (status filter-status)]
                         :on-success #(rf/dispatch [:activity-center.notifications/fetch-success filter-type filter-status reset-data? %])
                         :on-error   #(rf/dispatch [:activity-center.notifications/fetch-error filter-type filter-status %])}]})))

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

(fx/defn notifications-fetch-unread-contact-requests
  "Unread contact requests are, in practical terms, the same as pending contact
  requests in the new Activity Center, because pending contact requests are
  always marked as unread, and once the user declines/accepts the request, they
  are marked as read.

  If this relationship ever changes, we will probably need to change the backend
  to explicitly support fetching notifications for 'pending' contact requests."
  {:events [:activity-center.notifications/fetch-unread-contact-requests]}
  [cofx]
  (notifications-fetch cofx {:cursor        start-or-end-cursor
                             :filter-status :unread
                             :filter-type   types/contact-request
                             :per-page      20
                             :reset-data?   true}))

(fx/defn notifications-fetch-unread-count
  {:events [:activity-center.notifications/fetch-unread-count]}
  [_]
  {::json-rpc/call [{:method     "wakuext_unreadActivityCenterNotificationsCount"
                     :params     []
                     :on-success #(rf/dispatch [:activity-center.notifications/fetch-unread-count-success %])
                     :on-error   #()}]})

(fx/defn notifications-fetch-unread-count-success
  {:events [:activity-center.notifications/fetch-unread-count-success]}
  [{:keys [db]} result]
  {:db (assoc-in db [:activity-center :unread-count] result)})

(fx/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} filter-type filter-status error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center :notifications filter-type filter-status] dissoc :loading?)})

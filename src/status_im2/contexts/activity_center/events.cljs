(ns status-im2.contexts.activity-center.events
  (:require [status-im.data-store.activities :as data-store.activities]
            [status-im.data-store.chats :as data-store.chats]
            [status-im2.contexts.activity-center.notification-types :as types]
            [status-im2.contexts.chat.events :as chat.events]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(def defaults
  {:filter-status          :unread
   :filter-type            types/no-type
   ;; Choose the maximum number of notifications that *usually/safely* fit on
   ;; most screens, so that the UI doesn't have to needlessly render
   ;; notifications.
   :notifications-per-page 8})

;;;; Navigation

(rf/defn open-activity-center
  {:events [:activity-center/open]}
  [{:keys [db]} {:keys [filter-type filter-status]}]
  {:db       (cond-> db
               filter-status
               (assoc-in [:activity-center :filter :status] filter-status)

               filter-type
               (assoc-in [:activity-center :filter :type] filter-type))
   :dispatch [:show-popover
              {:view                       :activity-center
               :style                      {:margin 0}
               :disable-touchable-overlay? true
               :blur-view?                 true
               :blur-view-props            {:blur-amount 20
                                            :blur-type   :dark}}]})

;;;; Misc

(rf/defn process-notification-failure
  {:events [:activity-center/process-notification-failure]}
  [_ notification-id action error]
  (log/warn (str "Failed to " action)
            {:notification-id notification-id :error error}))

;;;; Notification reconciliation

(defn- notification-type->filter-type
  [type]
  (if (some types/membership [type])
    types/membership
    type))

(defn- update-notifications
  "Insert `new-notifications` in `db-notifications`.

  Although correct, this is a naive implementation for reconciling notifications
  because for every notification in `new-notifications`, linear scans will be
  performed to remove it and sorting will be performed for every new insertion.
  If the number of existing notifications cached in the app db becomes
  ~excessively~ big, this implementation will probably need to be revisited."
  [db-notifications new-notifications]
  (reduce (fn [acc {:keys [id read] :as notification}]
            (let [filter-type         (notification-type->filter-type (:type notification))
                  remove-notification (fn [data]
                                        (remove #(= id (:id %)) data))
                  insert-and-sort     (fn [data]
                                        (->> notification
                                             (conj data)
                                             (sort-by (juxt :timestamp :id))
                                             reverse))]
              (as-> acc $
                (update-in $ [filter-type :all :data] remove-notification)
                (update-in $ [types/no-type :all :data] remove-notification)
                (update-in $ [filter-type :unread :data] remove-notification)
                (update-in $ [types/no-type :unread :data] remove-notification)
                (if (:dismissed notification)
                  $
                  (cond-> (-> $
                              (update-in [filter-type :all :data] insert-and-sort)
                              (update-in [types/no-type :all :data] insert-and-sort))
                    (not read) (update-in [filter-type :unread :data] insert-and-sort)
                    (not read) (update-in [types/no-type :unread :data] insert-and-sort))))))
          db-notifications
          new-notifications))

(rf/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db       (update-in db
                          [:activity-center :notifications]
                          update-notifications
                          new-notifications)
     :dispatch [:activity-center.notifications/fetch-unread-count]}))

(rf/defn notifications-reconcile-from-response
  {:events [:activity-center/reconcile-notifications-from-response]}
  [cofx response]
  (->> response
       :activityCenterNotifications
       (map data-store.activities/<-rpc)
       (notifications-reconcile cofx)))

(defn- remove-pending-contact-request
  [notifications contact-id]
  (remove #(= contact-id (:author %))
          notifications))

(rf/defn notifications-remove-pending-contact-request
  {:events [:activity-center/remove-pending-contact-request]}
  [{:keys [db]} contact-id]
  {:db (-> db
           (update-in [:activity-center :notifications types/no-type :all :data]
                      remove-pending-contact-request
                      contact-id)
           (update-in [:activity-center :notifications types/no-type :unread :data]
                      remove-pending-contact-request
                      contact-id)
           (update-in [:activity-center :notifications types/contact-request :all :data]
                      remove-pending-contact-request
                      contact-id)
           (update-in [:activity-center :notifications types/contact-request :unread :data]
                      remove-pending-contact-request
                      contact-id))})

;;;; Mark notifications as read

(defn- get-notification
  [db notification-id]
  (->> (get-in db
               [:activity-center
                :notifications
                (get-in db [:activity-center :filter :type])
                (get-in db [:activity-center :filter :status])
                :data])
       (filter #(= notification-id (:id %)))
       first))

(rf/defn mark-as-read
  {:events [:activity-center.notifications/mark-as-read]}
  [{:keys [db]} notification-id]
  (when-let [notification (get-notification db notification-id)]
    {:json-rpc/call [{:method     "wakuext_markActivityCenterNotificationsRead"
                      :params     [[notification-id]]
                      :on-success #(rf/dispatch [:activity-center.notifications/mark-as-read-success
                                                 notification])
                      :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                 notification-id
                                                 :notification/mark-as-read
                                                 %])}]}))

(rf/defn mark-as-read-success
  {:events [:activity-center.notifications/mark-as-read-success]}
  [cofx notification]
  (notifications-reconcile cofx [(assoc notification :read true)]))

;;;; Acceptance/dismissal

(rf/defn accept-notification
  {:events [:activity-center.notifications/accept]}
  [{:keys [db]} notification-id]
  {:json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                    :params     [[notification-id]]
                    :on-success #(rf/dispatch [:activity-center.notifications/accept-success
                                               notification-id %])
                    :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                               notification-id
                                               :notification/accept
                                               %])}]})

(rf/defn accept-notification-success
  {:events [:activity-center.notifications/accept-success]}
  [{:keys [db] :as cofx} notification-id {:keys [chats]}]
  (let [notification (get-notification db notification-id)]
    (rf/merge cofx
              (chat.events/ensure-chats (map data-store.chats/<-rpc chats))
              (notifications-reconcile [(assoc notification :read true :accepted true)]))))

(rf/defn dismiss-notification
  {:events [:activity-center.notifications/dismiss]}
  [{:keys [db]} notification-id]
  {:json-rpc/call [{:method     "wakuext_dismissActivityCenterNotifications"
                    :params     [[notification-id]]
                    :on-success #(rf/dispatch [:activity-center.notifications/dismiss-success
                                               notification-id])
                    :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                               notification-id
                                               :notification/dismiss
                                               %])}]})

(rf/defn dismiss-notification-success
  {:events [:activity-center.notifications/dismiss-success]}
  [{:keys [db] :as cofx} notification-id]
  (let [notification (get-notification db notification-id)]
    (notifications-reconcile cofx [(assoc notification :dismissed true)])))

;;;; Contact verification

(rf/defn contact-verification-decline
  {:events [:activity-center.contact-verification/decline]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_declineContactVerificationRequest"
                    :params     [notification-id]
                    :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response
                                               %])
                    :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                               notification-id
                                               :contact-verification/decline
                                               %])}]})

(rf/defn contact-verification-reply
  {:events [:activity-center.contact-verification/reply]}
  [_ notification-id reply]
  {:json-rpc/call [{:method     "wakuext_acceptContactVerificationRequest"
                    :params     [notification-id reply]
                    :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response
                                               %])
                    :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                               notification-id
                                               :contact-verification/reply
                                               %])}]})

(rf/defn contact-verification-mark-as-trusted
  {:events [:activity-center.contact-verification/mark-as-trusted]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_verifiedTrusted"
                    :params     [{:id notification-id}]
                    :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response
                                               %])
                    :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                               notification-id
                                               :contact-verification/mark-as-trusted
                                               %])}]})

(rf/defn contact-verification-mark-as-untrustworthy
  {:events [:activity-center.contact-verification/mark-as-untrustworthy]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_verifiedUntrustworthy"
                    :params     [{:id notification-id}]
                    :on-success #(rf/dispatch [:activity-center/reconcile-notifications-from-response
                                               %])
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

(defn status
  [filter-status]
  (case filter-status
    :unread status-unread
    :all    status-all
    99))

(defn filter-type->rpc-param
  [filter-type]
  (cond
    (coll? filter-type)
    filter-type

    ;; A "no-type" notification shouldn't be sent to the backend. If, for
    ;; instance, the mobile client needs notifications of any type (as in the
    ;; `All` tab), then just don't filter by type at all.
    (= types/no-type filter-type)
    nil

    :else
    [filter-type]))

(rf/defn notifications-fetch
  [{:keys [db]} {:keys [cursor per-page filter-type filter-status reset-data?]}]
  (when-not (get-in db [:activity-center :notifications filter-type filter-status :loading?])
    (let [per-page  (or per-page (defaults :notifications-per-page))
          accepted? true]
      {:db            (assoc-in db
                       [:activity-center :notifications filter-type filter-status :loading?]
                       true)
       :json-rpc/call [{:method     "wakuext_activityCenterNotificationsBy"
                        :params     [cursor
                                     per-page
                                     (filter-type->rpc-param filter-type)
                                     (status filter-status)
                                     accepted?]
                        :on-success #(rf/dispatch [:activity-center.notifications/fetch-success
                                                   filter-type filter-status reset-data? %])
                        :on-error   #(rf/dispatch [:activity-center.notifications/fetch-error
                                                   filter-type filter-status %])}]})))

(rf/defn notifications-fetch-first-page
  {:events [:activity-center.notifications/fetch-first-page]}
  [{:keys [db] :as cofx} {:keys [filter-type filter-status]}]
  (let [filter-type   (or filter-type
                          (get-in db
                                  [:activity-center :filter :type]
                                  (defaults :filter-type)))
        filter-status (or filter-status
                          (get-in db
                                  [:activity-center :filter :status]
                                  (defaults :filter-status)))]
    (rf/merge cofx
              {:db (-> db
                       (assoc-in [:activity-center :filter :type] filter-type)
                       (assoc-in [:activity-center :filter :status] filter-status))}
              (notifications-fetch {:cursor        start-or-end-cursor
                                    :filter-type   filter-type
                                    :filter-status filter-status
                                    :reset-data?   true}))))

(rf/defn notifications-fetch-next-page
  {:events [:activity-center.notifications/fetch-next-page]}
  [{:keys [db] :as cofx}]
  (let [{:keys [type status]} (get-in db [:activity-center :filter])
        {:keys [cursor]}      (get-in db [:activity-center :notifications type status])]
    (when (valid-cursor? cursor)
      (notifications-fetch cofx
                           {:cursor        cursor
                            :filter-type   type
                            :filter-status status
                            :reset-data?   false}))))

(rf/defn notifications-fetch-success
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

(rf/defn notifications-fetch-unread-contact-requests
  "Unread contact requests are, in practical terms, the same as pending contact
  requests in the new Activity Center, because pending contact requests are
  always marked as unread, and once the user declines/accepts the request, they
  are marked as read.

  If this relationship ever changes, we will probably need to change the backend
  to explicitly support fetching notifications for 'pending' contact requests."
  {:events [:activity-center.notifications/fetch-unread-contact-requests]}
  [cofx]
  (notifications-fetch cofx
                       {:cursor        start-or-end-cursor
                        :filter-status :unread
                        :filter-type   types/contact-request
                        :per-page      20
                        :reset-data?   true}))

(rf/defn notifications-fetch-unread-count
  {:events [:activity-center.notifications/fetch-unread-count]}
  [_]
  {:json-rpc/call [{:method     "wakuext_unreadAndAcceptedActivityCenterNotificationsCount"
                    :params     [types/all-supported]
                    :on-success #(rf/dispatch [:activity-center.notifications/fetch-unread-count-success
                                               %])
                    :on-error   #()}]})

(rf/defn notifications-fetch-unread-count-success
  {:events [:activity-center.notifications/fetch-unread-count-success]}
  [{:keys [db]} result]
  {:db (assoc-in db [:activity-center :unread-count] result)})

(rf/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} filter-type filter-status error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center :notifications filter-type filter-status] dissoc :loading?)})

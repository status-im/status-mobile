(ns status-im2.contexts.activity-center.events
  (:require [status-im.data-store.activities :as data-store.activities]
            [status-im.data-store.chats :as data-store.chats]
            [status-im2.contexts.activity-center.notification-types :as types]
            [status-im2.contexts.chat.events :as chat.events]
            [status-im2.common.toasts.events :as toasts]
            status-im2.contexts.activity-center.notification.contact-requests.events
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [status-im2.constants :as constants]))

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

(rf/defn show-toasts
  {:events [:activity-center.notifications/show-toasts]}
  [{:keys [db]} new-notifications]
  (let [my-public-key (get-in db [:multiaccount :public-key])]
    (reduce (fn [cofx {:keys [author type accepted dismissed message name] :as x}]
              (cond
                (and (not= author my-public-key)
                     (= type types/contact-request)
                     (not accepted)
                     (not dismissed))
                (toasts/upsert cofx
                               {:icon       :placeholder
                                :icon-color colors/primary-50-opa-40
                                :title      (i18n/label :t/contact-request-sent-toast
                                                        {:name name})
                                :text       (get-in message [:content :text])})

                (and (= author my-public-key)               ;; we show it for user who sent the request
                     (= type types/contact-request)
                     accepted
                     (not dismissed))
                (toasts/upsert cofx
                               {:icon       :placeholder
                                :icon-color colors/primary-50-opa-40
                                :title      (i18n/label :t/contact-request-accepted-toast
                                                        {:name (:alias message)})})

                :else
                cofx))
            {:db db}
            new-notifications)))

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

(rf/defn mark-all-as-read
  {:events [:activity-center.notifications/mark-all-as-read]}
  [{:keys [db now]}]
  (when-let [undoable-till (get-in db [:activity-center :mark-all-as-read-undoable-till])]
    (when (>= now undoable-till)
      {:json-rpc/call [{:method     "wakuext_markAllActivityCenterNotificationsRead"
                        :params     []
                        :on-success #(rf/dispatch
                                      [:activity-center.notifications/mark-all-as-read-success])
                        :on-error   #(rf/dispatch [:activity-center/process-notification-failure
                                                   nil
                                                   :notification/mark-all-as-read
                                                   %])}]})))

(rf/defn mark-all-as-read-success
  {:events [:activity-center.notifications/mark-all-as-read-success]}
  [{:keys [db]}]
  {:db (-> (reduce (fn [acc notification-type]
                     (assoc-in acc [:activity-center :unread-counts-by-type notification-type] 0))
                   db
                   types/all-supported)
           (update :activity-center dissoc :mark-all-as-read-undoable-till))})

(rf/defn undo-mark-all-as-read
  {:events [:activity-center.notifications/undo-mark-all-as-read-locally]}
  [{:keys [db]} {:keys [notifications]}]
  {:db (-> db
           (update-in [:activity-center :notifications]
                      update-notifications
                      notifications)
           (update :activity-center dissoc :mark-all-as-read-undoable-till))})

(rf/defn mark-all-as-read-locally
  {:events [:activity-center.notifications/mark-all-as-read-locally]}
  [{:keys [db now]} get-toast-ui-props]
  (let [unread-notifications (get-in db [:activity-center :notifications types/no-type :unread :data])
        undo-time-limit-ms   constants/activity-center-mark-all-as-read-undo-time-limit-ms
        undoable-till        (+ now undo-time-limit-ms)]
    {:db                   (-> db
                               (update-in [:activity-center :notifications]
                                          update-notifications
                                          (data-store.activities/mark-notifications-as-read
                                           unread-notifications))
                               (assoc-in [:activity-center :mark-all-as-read-undoable-till]
                                         undoable-till))
     :dispatch             [:toasts/upsert
                            (merge
                             {:id :activity-center-mark-all-as-read
                              :duration undo-time-limit-ms
                              :undo-duration (/ undo-time-limit-ms 1000)
                              :undo-on-press
                              (fn []
                                (rf/dispatch
                                 [:activity-center.notifications/undo-mark-all-as-read-locally
                                  {:notifications unread-notifications}])
                                (rf/dispatch [:toasts/close :activity-center-mark-all-as-read]))}
                             (get-toast-ui-props))]
     :utils/dispatch-later [{:dispatch [:activity-center.notifications/mark-all-as-read]
                             :ms       undo-time-limit-ms}]}))

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
  {:dispatch-n (mapv (fn [notification-type]
                       [:activity-center.notifications/fetch-unread-count-for-type notification-type])
                     types/all-supported)})

(rf/defn notifications-fetch-unread-count-for-type
  {:events [:activity-center.notifications/fetch-unread-count-for-type]}
  [_ notification-type]
  {:json-rpc/call [{:method     "wakuext_unreadAndAcceptedActivityCenterNotificationsCount"
                    :params     [[notification-type]]
                    :on-success #(rf/dispatch [:activity-center.notifications/fetch-unread-count-success
                                               notification-type %])
                    :on-error   #(rf/dispatch [:activity-center.notifications/fetch-unread-count-error
                                               %])}]})

(rf/defn notifications-fetch-unread-count-success
  {:events [:activity-center.notifications/fetch-unread-count-success]}
  [{:keys [db]} notification-type result]
  {:db (assoc-in db [:activity-center :unread-counts-by-type notification-type] result)})

(rf/defn notifications-fetch-unread-count-error
  {:events [:activity-center.notifications/fetch-unread-count-error]}
  [_ error]
  (log/error "Failed to fetch count of notifications" {:error error})
  nil)

(rf/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} filter-type filter-status error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center :notifications filter-type filter-status] dissoc :loading?)})

(ns status-im2.contexts.shell.activity-center.events
  (:require [quo2.foundations.colors :as colors]
            [status-im.data-store.activities :as activities]
            [status-im.data-store.chats :as data-store.chats]
            [status-im2.common.toasts.events :as toasts]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.events :as chat.events]
            [status-im2.contexts.shell.activity-center.notification-types :as types]
            [taoensso.timbre :as log]
            [utils.collection :as collection]
            [utils.i18n :as i18n]
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
  {:db             (cond-> db
                     filter-status
                     (assoc-in [:activity-center :filter :status] filter-status)

                     filter-type
                     (assoc-in [:activity-center :filter :type] filter-type))
   :dispatch       [:open-modal :activity-center {}]
   ;; We delay marking as seen so that the user doesn't see the unread bell icon
   ;; change while the Activity Center modal is opening.
   :dispatch-later [{:ms       1000
                     :dispatch [:activity-center/mark-as-seen]}]})

;;;; Misc

(rf/defn process-notification-failure
  {:events [:activity-center/process-notification-failure]}
  [_ notification-id action error]
  (log/warn (str "Failed to " action)
            {:notification-id notification-id :error error}))

(defn get-notification
  [db notification-id]
  (->> (get-in db [:activity-center :notifications])
       (filter #(= notification-id (:id %)))
       first))

;;;; Reconciliation

(defn- update-notifications
  [db-notifications new-notifications {filter-type :type filter-status :status}]
  (->> new-notifications
       (reduce (fn [acc {:keys [id type deleted read] :as notification}]
                 (if (or deleted
                         (and (= :unread filter-status) read)
                         (and (set? filter-type)
                              (not (contains? filter-type type)))
                         (and (not (set? filter-type))
                              (not= filter-type types/no-type)
                              (not= filter-type type)))
                   (dissoc acc id)
                   (assoc acc id notification)))
               (collection/index-by :id db-notifications))
       (vals)
       (sort-by (juxt :timestamp :id)
                #(compare %2 %1))))

(rf/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db         (update-in db
                            [:activity-center :notifications]
                            update-notifications
                            new-notifications
                            (get-in db [:activity-center :filter]))
     :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                  [:activity-center.notifications/fetch-pending-contact-requests]]}))

(rf/defn notifications-reconcile-from-response
  {:events [:activity-center/reconcile-notifications-from-response]}
  [cofx response]
  (->> response
       :activityCenterNotifications
       (map activities/<-rpc)
       (notifications-reconcile cofx)))

(rf/defn notifications-remove-pending-contact-request
  {:events [:activity-center/remove-pending-contact-request]}
  [{:keys [db]} contact-id]
  {:db (update-in db
                  [:activity-center :notifications]
                  (fn [notifications]
                    (remove #(activities/pending-contact-request? contact-id %)
                            notifications)))})

(rf/defn reconcile-seen-state
  {:events [:activity-center/reconcile-seen-state]}
  [{:keys [db]} seen?]
  (cond-> {:db (assoc-in db [:activity-center :seen?] seen?)}

    (= (:view-id db) :activity-center)
    (assoc :dispatch [:activity-center/mark-as-seen])))

;;;; Status changes (read/dismissed/deleted)

(rf/defn mark-as-read
  {:events [:activity-center.notifications/mark-as-read]}
  [{:keys [db]} notification-id]
  (when-let [notification (get-notification db notification-id)]
    {:json-rpc/call [{:method     "wakuext_markActivityCenterNotificationsRead"
                      :params     [[notification-id]]
                      :on-success [:activity-center.notifications/mark-as-read-success notification]
                      :on-error   [:activity-center/process-notification-failure notification-id
                                   :notification/mark-as-read]}]}))

(rf/defn mark-as-read-success
  {:events [:activity-center.notifications/mark-as-read-success]}
  [cofx notification]
  (notifications-reconcile cofx [(assoc notification :read true)]))

(rf/defn mark-as-unread
  {:events [:activity-center.notifications/mark-as-unread]}
  [{:keys [db]} notification-id]
  (when-let [notification (get-notification db notification-id)]
    {:json-rpc/call [{:method     "wakuext_markActivityCenterNotificationsUnread"
                      :params     [[notification-id]]
                      :on-success [:activity-center.notifications/mark-as-unread-success notification]
                      :on-error   [:activity-center/process-notification-failure notification-id
                                   :notification/mark-as-unread]}]}))

(rf/defn mark-as-unread-success
  {:events [:activity-center.notifications/mark-as-unread-success]}
  [cofx notification]
  (notifications-reconcile cofx [(assoc notification :read false)]))

(rf/defn mark-all-as-read
  {:events [:activity-center.notifications/mark-all-as-read]}
  [{:keys [db now]}]
  (when-let [undoable-till (get-in db [:activity-center :mark-all-as-read-undoable-till])]
    (when (>= now undoable-till)
      {:json-rpc/call [{:method     "wakuext_markAllActivityCenterNotificationsRead"
                        :params     []
                        :on-success [:activity-center.notifications/mark-all-as-read-success]
                        :on-error   [:activity-center/process-notification-failure nil
                                     :notification/mark-all-as-read]}]})))

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
                      notifications
                      (get-in db [:activity-center :filter]))
           (update :activity-center dissoc :mark-all-as-read-undoable-till))})

(rf/defn mark-all-as-read-locally
  {:events [:activity-center.notifications/mark-all-as-read-locally]}
  [{:keys [db now]} get-toast-ui-props]
  (let [unread-notifications (filter #(not (:read %))
                                     (get-in db [:activity-center :notifications]))
        undo-time-limit-ms   constants/activity-center-mark-all-as-read-undo-time-limit-ms
        undoable-till        (+ now undo-time-limit-ms)]
    {:db                   (-> db
                               (update-in [:activity-center :notifications]
                                          update-notifications
                                          (activities/mark-notifications-as-read
                                           unread-notifications)
                                          (get-in db [:activity-center :filter]))
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

(rf/defn accept-notification
  {:events [:activity-center.notifications/accept]}
  [{:keys [db]} notification-id]
  {:json-rpc/call [{:method     "wakuext_acceptActivityCenterNotifications"
                    :params     [[notification-id]]
                    :on-success [:activity-center.notifications/accept-success notification-id]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :notification/accept]}]})

(rf/defn accept-notification-success
  {:events [:activity-center.notifications/accept-success]}
  [{:keys [db] :as cofx} notification-id {:keys [chats]}]
  (when-let [notification (get-notification db notification-id)]
    (rf/merge cofx
              (chat.events/ensure-chats (map data-store.chats/<-rpc chats))
              (notifications-reconcile [(assoc notification :read true :accepted true)]))))

(rf/defn dismiss-notification
  {:events [:activity-center.notifications/dismiss]}
  [{:keys [db]} notification-id]
  {:json-rpc/call [{:method     "wakuext_dismissActivityCenterNotifications"
                    :params     [[notification-id]]
                    :on-success [:activity-center.notifications/dismiss-success notification-id]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :notification/dismiss]}]})

(rf/defn dismiss-notification-success
  {:events [:activity-center.notifications/dismiss-success]}
  [{:keys [db] :as cofx} notification-id]
  (when-let [notification (get-notification db notification-id)]
    (notifications-reconcile cofx [(assoc notification :read true :dismissed true)])))

(rf/defn delete-notification
  {:events [:activity-center.notifications/delete]}
  [{:keys [db]} notification-id]
  {:json-rpc/call [{:method     "wakuext_deleteActivityCenterNotifications"
                    :params     [[notification-id]]
                    :on-success [:activity-center.notifications/delete-success notification-id]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :notification/delete]}]})

(rf/defn delete-notification-success
  {:events [:activity-center.notifications/delete-success]}
  [{:keys [db] :as cofx} notification-id]
  (let [notification (get-notification db notification-id)]
    (notifications-reconcile cofx [(assoc notification :deleted true)])))

;;;; Contact verification

(rf/defn contact-verification-decline
  {:events [:activity-center.contact-verification/decline]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_declineContactVerificationRequest"
                    :params     [notification-id]
                    :on-success [:activity-center/reconcile-notifications-from-response]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :contact-verification/decline]}]})

(rf/defn contact-verification-reply
  {:events [:activity-center.contact-verification/reply]}
  [_ notification-id reply]
  {:json-rpc/call [{:method     "wakuext_acceptContactVerificationRequest"
                    :params     [notification-id reply]
                    :on-success [:activity-center/reconcile-notifications-from-response]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :contact-verification/reply]}]})

(rf/defn contact-verification-mark-as-trusted
  {:events [:activity-center.contact-verification/mark-as-trusted]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_verifiedTrusted"
                    :params     [{:id notification-id}]
                    :on-success [:activity-center/reconcile-notifications-from-response]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :contact-verification/mark-as-trusted]}]})

(rf/defn contact-verification-mark-as-untrustworthy
  {:events [:activity-center.contact-verification/mark-as-untrustworthy]}
  [_ notification-id]
  {:json-rpc/call [{:method     "wakuext_verifiedUntrustworthy"
                    :params     [{:id notification-id}]
                    :on-success [:activity-center/reconcile-notifications-from-response]
                    :on-error   [:activity-center/process-notification-failure notification-id
                                 :contact-verification/mark-as-untrustworthy]}]})

;;;; Notifications fetching and pagination

(def start-or-end-cursor
  "")

(defn- fetch-more?
  [cursor]
  (and (some? cursor)
       (not= cursor start-or-end-cursor)))

(def ^:const status-unread 2)
(def ^:const status-all 3)
(def ^:const read-type-read 1)
(def ^:const read-type-unread 2)
(def ^:const read-type-all 3)

(defn status
  [filter-status]
  (case filter-status
    :unread status-unread
    :all    status-all
    99))

(defn ->rpc-read-type
  [read-type]
  (case read-type
    :read   read-type-read
    :unread read-type-unread
    :all    read-type-all
    ;; Send invalid type, so the backend fails fast.
    -1))

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
  (when-not (get-in db [:activity-center :loading?])
    (let [per-page (or per-page (defaults :notifications-per-page))]
      {:db            (assoc-in db [:activity-center :loading?] true)
       :json-rpc/call [{:method     "wakuext_activityCenterNotifications"
                        :params     [{:cursor        cursor
                                      :limit         per-page
                                      :activityTypes (filter-type->rpc-param filter-type)
                                      :readType      (->rpc-read-type filter-status)}]
                        :on-success [:activity-center.notifications/fetch-success reset-data?]
                        :on-error   [:activity-center.notifications/fetch-error filter-type
                                     filter-status]}]})))

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
        cursor                (get-in db [:activity-center :cursor])]
    (when (fetch-more? cursor)
      (notifications-fetch cofx
                           {:cursor        cursor
                            :filter-type   type
                            :filter-status status
                            :reset-data?   false}))))

(rf/defn notifications-fetch-success
  {:events [:activity-center.notifications/fetch-success]}
  [{:keys [db]} reset-data? {:keys [cursor notifications]}]
  (let [processed (map activities/<-rpc notifications)]
    {:db (-> db
             (assoc-in [:activity-center :cursor] cursor)
             (update :activity-center dissoc :loading?)
             (update-in [:activity-center :notifications]
                        (if reset-data?
                          (constantly processed)
                          #(concat % processed))))}))

(rf/defn notifications-fetch-pending-contact-requests
  "Unread contact requests are, in practical terms, the same as pending contact
  requests in the Activity Center, because pending contact requests are always
  marked as unread in status-go, and once the user declines/accepts the request,
  they are marked as read.

  If this relationship ever changes, we will probably need to change the backend
  to explicitly support fetching notifications for 'pending' contact requests."
  {:events [:activity-center.notifications/fetch-pending-contact-requests]}
  [{:keys [db]}]
  {:db (assoc-in db [:activity-center :loading?] true)
   :json-rpc/call
   [{:method     "wakuext_activityCenterNotifications"
     :params     [{:cursor        start-or-end-cursor
                   :limit         20
                   :activityTypes [types/contact-request]
                   :readType      (->rpc-read-type :unread)}]
     :on-success [:activity-center.notifications/fetch-pending-contact-requests-success]
     :on-error   [:activity-center.notifications/fetch-error types/contact-request :unread]}]})

(rf/defn notifications-fetch-pending-contact-requests-success
  {:events [:activity-center.notifications/fetch-pending-contact-requests-success]}
  [{:keys [db]} {:keys [notifications]}]
  {:db (-> db
           (update :activity-center dissoc :loading?)
           (assoc-in [:activity-center :contact-requests]
                     (->> notifications
                          (map activities/<-rpc)
                          (filter (fn [notification]
                                    (= constants/contact-request-message-state-pending
                                       (get-in notification [:message :contact-request-state])))))))})

(rf/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update db :activity-center dissoc :loading?)})

;;;; Unread counters

(rf/defn update-seen-state
  {:events [:activity-center/update-seen-state]}
  [_]
  {:json-rpc/call
   [{:method     "wakuext_hasUnseenActivityCenterNotifications"
     :params     []
     :on-success [:activity-center/update-seen-state-success]
     :on-error   [:activity-center/update-seen-state-error]}]})

(rf/defn update-seen-state-success
  {:events [:activity-center/update-seen-state-success]}
  [{:keys [db]} unseen?]
  {:db (assoc-in db [:activity-center :seen?] (not unseen?))})

(rf/defn update-seen-state-error
  {:events [:activity-center/update-seen-state-error]}
  [_ error]
  (log/error "Failed to update Activity Center seen state"
             {:error error
              :event :activity-center/update-seen-state}))

(rf/defn mark-as-seen
  {:events [:activity-center/mark-as-seen]}
  [_]
  {:json-rpc/call
   [{:method     "wakuext_markAsSeenActivityCenterNotifications"
     :params     []
     :on-success [:activity-center/mark-as-seen-success]
     :on-error   [:activity-center/mark-as-seen-error]}]})

(rf/defn mark-as-seen-success
  {:events [:activity-center/mark-as-seen-success]}
  [{:keys [db]} response]
  {:db (assoc-in db
        [:activity-center :seen?]
        (get-in response [:activityCenterState :hasSeen]))})

(rf/defn mark-as-seen-error
  {:events [:activity-center/mark-as-seen-error]}
  [_ error]
  (log/error "Failed to mark Activity Center as seen"
             {:error error
              :event :activity-center/mark-as-seen}))

(rf/defn notifications-fetch-unread-count
  {:events [:activity-center.notifications/fetch-unread-count]}
  [_]
  {:json-rpc/call
   [{:method     "wakuext_activityCenterNotificationsCount"
     :params     [{:activityTypes types/all-supported
                   :readType      (->rpc-read-type :unread)}]
     :on-success [:activity-center.notifications/fetch-unread-count-success]
     :on-error   [:activity-center.notifications/fetch-unread-count-error]}]})

(rf/defn notifications-fetch-unread-count-success
  {:events [:activity-center.notifications/fetch-unread-count-success]}
  [{:keys [db]} response]
  {:db (assoc-in db
        [:activity-center :unread-counts-by-type]
        (activities/parse-notification-counts-response response))})

(rf/defn notifications-fetch-unread-count-error
  {:events [:activity-center.notifications/fetch-unread-count-error]}
  [_ error]
  (log/error "Failed to fetch count of notifications" {:error error})
  nil)

;;;; Toasts

(rf/defn show-toasts
  {:events [:activity-center.notifications/show-toasts]}
  [{:keys [db]} new-notifications]
  (let [my-public-key (get-in db [:profile/profile :public-key])]
    (reduce (fn [cofx {:keys [author chat-id type accepted dismissed message name] :as x}]
              (let [user-avatar {:full-name         name
                                 :status-indicator? true
                                 :online?           nil
                                 :size              :small
                                 :ring?             true}]
                (cond
                  (and (not= author my-public-key)
                       (= type types/contact-request)
                       (not accepted)
                       (not dismissed))
                  (toasts/upsert cofx
                                 {:user            user-avatar
                                  :user-public-key author
                                  :icon-color      colors/primary-50-opa-40
                                  :title           (i18n/label :t/contact-request-sent-toast
                                                               {:name name})
                                  :text            (get-in message [:content :text])})

                  (and (= author my-public-key) ;; we show it for user who sent the request
                       (= type types/contact-request)
                       accepted
                       (not dismissed))
                  (toasts/upsert cofx
                                 {:user            user-avatar
                                  :user-public-key chat-id ;; user public key who accepted the request
                                  :icon-color      colors/success-50-opa-40
                                  :title           (i18n/label :t/contact-request-accepted-toast
                                                               {:name (or name (:alias message))})})
                  :else
                  cofx)))
            {:db db}
            new-notifications)))

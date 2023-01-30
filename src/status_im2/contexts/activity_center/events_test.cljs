(ns status-im2.contexts.activity-center.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.constants :as constants]
            status-im.events
            [test-helpers.unit :as h]
            [status-im2.contexts.activity-center.events :as activity-center]
            [status-im2.contexts.activity-center.notification-types :as types]
            [utils.re-frame :as rf]))

(h/use-log-fixture)

(def notification-id "0x1")

(defn setup
  []
  (h/register-helper-events)
  (rf/dispatch [:setup/app-started]))

(defn test-log-on-failure
  [{:keys [before-test notification-id event action]}]
  (h/run-test-sync
   (setup)
   (when before-test
     (before-test))
   (h/stub-fx-with-callbacks :json-rpc/call :on-error (constantly :fake-error))

   (rf/dispatch event)

   (is (= {:args  [(str "Failed to " action)
                   {:notification-id notification-id
                    :error           :fake-error}]
           :level :warn}
          (last @h/logs)))))

;;;; Misc

(deftest open-activity-center-test
  (testing "opens the activity center with filters enabled"
    (h/run-test-sync
     (let [spy-queue (atom [])]
       (setup)
       (h/spy-fx spy-queue :show-popover)

       (rf/dispatch [:activity-center/open
                     {:filter-type   types/contact-request
                      :filter-status :unread}])

       (is (= {:status :unread
               :type   types/contact-request}
              (get-in (h/db) [:activity-center :filter])))
       (is (= [{:id :show-popover :args nil}]
              @spy-queue)))))

  (testing "opens the activity center without custom filters"
    (h/run-test-sync
     (let [spy-queue        (atom [])
           existing-filters {:status :all}]
       (setup)
       (h/spy-fx spy-queue :show-popover)
       (rf/dispatch [:test/assoc-in [:activity-center :filter] existing-filters])

       (rf/dispatch [:activity-center/open])

       (is (= existing-filters
              (get-in (h/db) [:activity-center :filter])))
       (is (= [{:id :show-popover :args nil}]
              @spy-queue))))))

(deftest mark-as-read-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)
       (let [notifications {types/one-to-one-chat
                            {:all    {:cursor ""
                                      :data   [{:id   notification-id
                                                :read false
                                                :type types/one-to-one-chat}]}
                             :unread {:cursor "" :data []}}}]
         (rf/dispatch [:test/assoc-in [:activity-center]
                       {:notifications notifications
                        :filter        {:type   types/one-to-one-chat
                                        :status :all}}])

         (rf/dispatch [:activity-center.notifications/mark-as-read "0x666"])

         (is (= [] @spy-queue))
         (is (= notifications (get-in (h/db) [:activity-center :notifications])))))))

  (testing "marks notifications as read and updates app db"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat}
           notif-2     {:id "0x2" :read false :type types/one-to-one-chat}
           notif-3     {:id "0x3" :read false :type types/one-to-one-chat}
           new-notif-3 (assoc notif-3 :read true)
           new-notif-2 (assoc notif-2 :read true)]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly nil))
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:notifications {types/one-to-one-chat
                                      {:all    {:cursor "" :data [notif-3 notif-2 notif-1]}
                                       :unread {:cursor "" :data [notif-3 notif-2]}}}
                      :filter        {:type   types/one-to-one-chat
                                      :status :unread}}])

       (rf/dispatch [:activity-center.notifications/mark-as-read (:id notif-2)])
       (is (= {types/one-to-one-chat
               {:all    {:cursor "" :data [notif-3 new-notif-2 notif-1]}
                :unread {:cursor "" :data [notif-3]}}

               types/no-type
               {:all    {:data [new-notif-2]}
                :unread {:data []}}}
              (get-in (h/db) [:activity-center :notifications])))

       (rf/dispatch [:activity-center.notifications/mark-as-read (:id notif-3)])
       (is (= {types/one-to-one-chat
               {:all    {:cursor "" :data [new-notif-3 new-notif-2 notif-1]}
                :unread {:cursor "" :data []}}

               types/no-type
               {:all    {:data [new-notif-3 new-notif-2]}
                :unread {:data []}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.notifications/mark-as-read notification-id]
      :action          :notification/mark-as-read
      :before-test     (fn []
                         (rf/dispatch
                          [:test/assoc-in [:activity-center]
                           {:notifications {types/one-to-one-chat
                                            {:all    {:cursor ""
                                                      :data   [{:id   notification-id
                                                                :read false
                                                                :type types/one-to-one-chat}]}
                                             :unread {:cursor "" :data []}}}
                            :filter        {:type   types/one-to-one-chat
                                            :status :all}}]))})))

;;;; Acceptance/dismissal

(deftest notification-acceptance-test
  (testing "marks notification as accepted and read, then reconciles"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :type types/private-group-chat}
           notif-2     {:id "0x2" :type types/private-group-chat}
           notif-3     {:id "0x3" :type types/admin}
           notif-2-new (assoc notif-2 :accepted true :read true)]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly notif-2))
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:notifications {types/membership
                                      {:unread {:cursor "" :data [notif-2 notif-1]}}

                                      types/admin
                                      {:all {:cursor "" :data [notif-3]}}}
                      :filter        {:type   types/membership
                                      :status :unread}}])

       (rf/dispatch [:activity-center.notifications/accept (:id notif-2)])

       (is (= {types/no-type    {:all    {:data [notif-2-new]}
                                 :unread {:data []}}
               types/membership {:all    {:data [notif-2-new]}
                                 :unread {:cursor "" :data [notif-1]}}
               types/admin      {:all {:cursor "" :data [notif-3]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.notifications/accept notification-id]
      :action          :notification/accept})))

(deftest notification-dismissal-test
  (testing "dismisses notification and removes from app db"
    (h/run-test-sync
     (setup)
     (let [notif-1 {:id "0x1" :type types/private-group-chat}
           notif-2 {:id "0x2" :type types/admin}]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly notif-2))
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:notifications {types/no-type
                                      {:all {:cursor "" :data [notif-2 notif-1]}}

                                      types/membership
                                      {:unread {:cursor "" :data [notif-1]}}}
                      :filter        {:type   types/membership
                                      :status :unread}}])

       (rf/dispatch [:activity-center.notifications/dismiss (:id notif-1)])

       (is (= {types/no-type
               {:all    {:cursor "" :data [notif-2]}
                :unread {:data []}}

               types/membership
               {:all    {:data []}
                :unread {:cursor "" :data []}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.notifications/dismiss notification-id]
      :action          :notification/dismiss})))

;;;; Contact verification

(def contact-verification-rpc-response
  {:activityCenterNotifications
   [{:accepted                  false
     :author                    "0x04d03f"
     :chatId                    "0x04d03f"
     :contactVerificationStatus constants/contact-verification-status-pending
     :dismissed                 false
     :id                        notification-id
     :message                   {}
     :name                      "0x04d03f"
     :read                      true
     :timestamp                 1666647286000
     :type                      types/contact-verification}]})

(def contact-verification-expected-notification
  {:accepted                    false
   :author                      "0x04d03f"
   :chat-id                     "0x04d03f"
   :contact-verification-status constants/contact-verification-status-pending
   :dismissed                   false
   :id                          notification-id
   :last-message                nil
   :message                     {:command-parameters nil
                                 :content            {:chat-id     nil
                                                      :ens-name    nil
                                                      :image       nil
                                                      :line-count  nil
                                                      :links       nil
                                                      :parsed-text nil
                                                      :response-to nil
                                                      :rtl?        nil
                                                      :sticker     nil
                                                      :text        nil}
                                 :outgoing           false
                                 :outgoing-status    nil
                                 :quoted-message     nil}
   :name                        "0x04d03f"
   :read                        true
   :reply-message               nil
   :timestamp                   1666647286000
   :type                        types/contact-verification})

(defn test-contact-verification-event
  [{:keys [event expected-rpc-call]}]
  (h/run-test-sync
   (setup)
   (let [spy-queue (atom [])]
     (h/stub-fx-with-callbacks :json-rpc/call
                               :on-success
                               (constantly contact-verification-rpc-response))
     (h/spy-fx spy-queue :json-rpc/call)
     (rf/dispatch event)

     (is (= {types/no-type
             {:all    {:data [contact-verification-expected-notification]}
              :unread {:data []}}
             types/contact-verification
             {:all    {:data [contact-verification-expected-notification]}
              :unread {:data []}}}
            (get-in (h/db) [:activity-center :notifications])))

     (is (= expected-rpc-call
            (-> @spy-queue
                (get-in [0 :args 0])
                (select-keys [:method :params])))))))

(deftest contact-verification-decline-test
  (testing "declines notification and reconciles"
    (test-contact-verification-event
     {:event             [:activity-center.contact-verification/decline notification-id]
      :expected-rpc-call {:method "wakuext_declineContactVerificationRequest"
                          :params [notification-id]}}))
  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/decline notification-id]
      :action          :contact-verification/decline})))

(deftest contact-verification-reply-test
  (testing "sends reply and reconciles"
    (let [reply "any answer"]
      (test-contact-verification-event
       {:event             [:activity-center.contact-verification/reply notification-id reply]
        :expected-rpc-call {:method "wakuext_acceptContactVerificationRequest"
                            :params [notification-id reply]}})))
  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/reply notification-id "any answer"]
      :action          :contact-verification/reply})))

(deftest contact-verification-mark-as-trusted-test
  (testing "marks notification as trusted and reconciles"
    (test-contact-verification-event
     {:event             [:activity-center.contact-verification/mark-as-trusted notification-id]
      :expected-rpc-call {:method "wakuext_verifiedTrusted"
                          :params [{:id notification-id}]}}))
  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/mark-as-trusted notification-id]
      :action          :contact-verification/mark-as-trusted})))

(deftest contact-verification-mark-as-untrustworthy-test
  (testing "marks notification as untrustworthy and reconciles"
    (test-contact-verification-event
     {:event             [:activity-center.contact-verification/mark-as-untrustworthy notification-id]
      :expected-rpc-call {:method "wakuext_verifiedUntrustworthy"
                          :params [{:id notification-id}]}}))
  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/mark-as-untrustworthy notification-id]
      :action          :contact-verification/mark-as-untrustworthy})))

;;;; Notification reconciliation

(deftest notifications-reconcile-test
  (testing "does nothing when there are no new notifications"
    (h/run-test-sync
     (setup)
     (let [notifications {types/one-to-one-chat
                          {:all    {:cursor ""
                                    :data   [{:id   "0x1"
                                              :read true
                                              :type types/one-to-one-chat}
                                             {:id   "0x2"
                                              :read false
                                              :type types/one-to-one-chat}]}
                           :unread {:cursor ""
                                    :data   [{:id   "0x3"
                                              :read false
                                              :type types/one-to-one-chat}]}}
                          types/private-group-chat
                          {:unread {:cursor ""
                                    :data   [{:id   "0x4"
                                              :read false
                                              :type types/private-group-chat}]}}}]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications] notifications])

       (rf/dispatch [:activity-center.notifications/reconcile nil])

       (is (= notifications (get-in (h/db) [:activity-center :notifications]))))))

  (testing "removes dismissed notifications"
    (h/run-test-sync
     (setup)
     (let [notif-1 {:id "0x1" :read true :type types/one-to-one-chat}
           notif-2 {:id "0x2" :read false :type types/one-to-one-chat}
           notif-3 {:id "0x3" :read false :type types/system}
           notif-4 {:id "0x4" :read true :type types/system}
           notif-5 {:id "0x5" :read false :type types/system :accepted true}]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:all    {:cursor "" :data [notif-1 notif-2]}
                       :unread {:cursor "" :data [notif-2]}}
                      types/system
                      {:all    {:cursor "" :data [notif-4]}
                       :unread {:cursor "" :data [notif-3 notif-5]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile
                     [(assoc notif-1 :dismissed true)
                      (assoc notif-4 :dismissed true)
                      notif-5]])

       (is (= {types/no-type
               {:all    {:data [notif-5]}
                :unread {:data [notif-5]}}
               types/one-to-one-chat
               {:all    {:cursor "" :data [notif-2]}
                :unread {:cursor "" :data [notif-2]}}
               types/system
               {:all    {:cursor "" :data [notif-5]}
                :unread {:cursor "" :data [notif-5 notif-3]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "replaces old notifications with newly arrived ones"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat}
           notif-4     {:id "0x4" :read false :type types/system}
           notif-6     {:id "0x6" :read false :type types/system}
           new-notif-1 (assoc notif-1 :last-message {})
           new-notif-4 (assoc notif-4 :author "0xabc")]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/no-type
                      {:all    {:cursor "" :data [notif-1]}
                       :unread {:cursor "" :data [notif-4 notif-6]}}
                      types/one-to-one-chat
                      {:all {:cursor "" :data [notif-1]}}
                      types/system
                      {:unread {:cursor "" :data [notif-4 notif-6]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile [new-notif-1 new-notif-4 notif-6]])

       (is (= {types/no-type
               {:all    {:cursor "" :data [notif-6 new-notif-4 new-notif-1]}
                :unread {:cursor "" :data [notif-6 new-notif-4]}}
               types/one-to-one-chat
               {:all    {:cursor "" :data [new-notif-1]}
                :unread {:data []}}
               types/system
               {:all    {:data [notif-6 new-notif-4]}
                :unread {:cursor "" :data [notif-6 new-notif-4]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "reconciles notifications that switched their read/unread status"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat}
           new-notif-1 (assoc notif-1 :read false)]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:all {:cursor "" :data [notif-1]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile [new-notif-1]])

       (is (= {types/no-type
               {:all    {:data [new-notif-1]}
                :unread {:data [new-notif-1]}}

               types/one-to-one-chat
               {:all    {:cursor "" :data [new-notif-1]}
                :unread {:data [new-notif-1]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "membership notifications"
    (h/run-test-sync
     (setup)
     (let [notif {:read      false
                  :dismissed false
                  :accepted  false
                  :type      types/private-group-chat
                  :id        "0x7"
                  :timestamp 1673445663000}]
       (rf/dispatch [:activity-center.notifications/reconcile [notif]])

       (is (= {types/no-type
               {:all    {:data [notif]}
                :unread {:data [notif]}}

               types/membership
               {:all    {:data [notif]}
                :unread {:data [notif]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  ;; Sorting by timestamp and ID is compatible with what the backend does when
  ;; returning paginated results.
  (testing "sorts notifications by timestamp and id in descending order"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat :timestamp 1}
           notif-2     {:id "0x2" :read true :type types/one-to-one-chat :timestamp 1}
           notif-3     {:id "0x3" :read false :type types/one-to-one-chat :timestamp 50}
           notif-4     {:id "0x4" :read false :type types/one-to-one-chat :timestamp 100}
           notif-5     {:id "0x5" :read false :type types/one-to-one-chat :timestamp 100}
           new-notif-1 (assoc notif-1 :last-message {})
           new-notif-4 (assoc notif-4 :last-message {})]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:all    {:cursor "" :data [notif-1 notif-2]}
                       :unread {:cursor "" :data [notif-3 notif-4 notif-5]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile [new-notif-1 new-notif-4]])

       (is (= {types/no-type
               {:all    {:data [new-notif-4 new-notif-1]}
                :unread {:data [new-notif-4]}}
               types/one-to-one-chat
               {:all    {:cursor "" :data [new-notif-4 notif-2 new-notif-1]}
                :unread {:cursor "" :data [notif-5 new-notif-4 notif-3]}}}
              (get-in (h/db) [:activity-center :notifications])))))))

(deftest remove-pending-contact-request-test
  (testing "removes notification from all related filter types and status"
    (h/run-test-sync
     (setup)
     (let [contact-pub-key "0x99"
           notif-1         {:id "0x1" :read true :type types/contact-request}
           notif-2         {:id "0x2" :read false :type types/contact-request :author contact-pub-key}
           notif-3         {:id "0x3" :read false :type types/private-group-chat}
           notifications   {types/contact-request
                            {:all    {:cursor "" :data [notif-2 notif-1]}
                             :unread {:cursor "" :data [notif-2]}}
                            types/private-group-chat
                            {:unread {:cursor "" :data [notif-3]}}
                            types/no-type
                            {:all    {:cursor "" :data [notif-3 notif-2 notif-1]}
                             :unread {:cursor "" :data [notif-2 notif-3]}}}]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications] notifications])

       (rf/dispatch [:activity-center/remove-pending-contact-request contact-pub-key])

       (is (= {types/contact-request
               {:all    {:cursor "" :data [notif-1]}
                :unread {:cursor "" :data []}}
               types/private-group-chat
               {:unread {:cursor "" :data [notif-3]}}
               types/no-type
               {:all    {:cursor "" :data [notif-3 notif-1]}
                :unread {:cursor "" :data [notif-3]}}}
              (get-in (h/db) [:activity-center :notifications])))))))

;;;; Notifications fetching and pagination

(deftest notifications-fetch-test
  (testing "fetches first page"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks
        :json-rpc/call
        :on-success
        (constantly {:cursor        "10"
                     :notifications [{:id     "0x1"
                                      :type   types/one-to-one-chat
                                      :read   false
                                      :chatId "0x9"}]}))
       (h/spy-fx spy-queue :json-rpc/call)

       (rf/dispatch [:activity-center.notifications/fetch-first-page
                     {:filter-type types/one-to-one-chat}])

       (is (= :unread (get-in (h/db) [:activity-center :filter :status])))
       (is (= "" (get-in @spy-queue [0 :args 0 :params 0]))
           "Should be called with empty cursor when fetching first page")
       (is (= {types/one-to-one-chat
               {:unread {:cursor "10"
                         :data   [{:chat-id       "0x9"
                                   :chat-name     nil
                                   :chat-type     types/one-to-one-chat
                                   :group-chat    false
                                   :id            "0x1"
                                   :public?       false
                                   :last-message  nil
                                   :message       nil
                                   :read          false
                                   :reply-message nil
                                   :type          types/one-to-one-chat}]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "does not fetch next page when pagination cursor reached the end"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                     :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                     types/one-to-one-chat])
       (rf/dispatch [:test/assoc-in
                     [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                     ""])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= [] @spy-queue)))))

  ;; The cursor can be nil sometimes because the reconciliation doesn't care
  ;; about updating the cursor value, but we have to make sure the next page is
  ;; only fetched if the current cursor is valid.
  (testing "does not fetch next page when cursor is nil"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                     :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                     types/one-to-one-chat])
       (rf/dispatch [:test/assoc-in
                     [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                     nil])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= [] @spy-queue)))))

  (testing "fetches next page when pagination cursor is not empty"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks
        :json-rpc/call
        :on-success
        (constantly {:cursor        ""
                     :notifications [{:id     "0x1"
                                      :type   types/mention
                                      :read   false
                                      :chatId "0x9"}]}))
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                     :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                     types/mention])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications types/mention :unread :cursor]
                     "10"])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= "wakuext_activityCenterNotificationsBy" (get-in @spy-queue [0 :args 0 :method])))
       (is (= "10" (get-in @spy-queue [0 :args 0 :params 0]))
           "Should be called with current cursor")
       (is (= {types/mention
               {:unread {:cursor ""
                         :data   [{:chat-id       "0x9"
                                   :chat-name     nil
                                   :chat-type     3
                                   :id            "0x1"
                                   :last-message  nil
                                   :message       nil
                                   :read          false
                                   :reply-message nil
                                   :type          types/mention}]}}}
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "does not fetch next page while it is still loading"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                     :all])
       (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                     types/one-to-one-chat])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :all :cursor]
                     "10"])
       (rf/dispatch [:test/assoc-in
                     [:activity-center :notifications types/one-to-one-chat :all :loading?]
                     true])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= [] @spy-queue)))))

  (testing "resets loading flag after an error"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks :json-rpc/call :on-error (constantly :fake-error))
       (h/spy-event-fx spy-queue :activity-center.notifications/fetch-error)
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                     :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                     types/one-to-one-chat])
       (rf/dispatch [:test/assoc-in
                     [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                     ""])

       (rf/dispatch [:activity-center.notifications/fetch-first-page])

       (is (nil? (get-in (h/db)
                         [:activity-center :notifications types/one-to-one-chat :unread :loading?])))
       (is (= [:activity-center.notifications/fetch-error
               types/one-to-one-chat
               :unread
               :fake-error]
              (:args (last @spy-queue))))))))

(deftest notifications-fetch-unread-contact-requests-test
  (testing "fetches latest unread contact requests"
    (let [actual   (activity-center/notifications-fetch-unread-contact-requests {:db {}})
          per-page 20]
      (is (= {:activity-center
              {:notifications
               {types/contact-request
                {:unread {:loading? true}}}}}
             (:db actual)))

      (is (= {:method "wakuext_activityCenterNotificationsBy"
              :params ["" per-page [types/contact-request] activity-center/status-unread true]}
             (-> actual
                 :json-rpc/call
                 first
                 (select-keys [:method :params])))))))

(deftest notifications-fetch-unread-count-test
  (testing "fetches total notification count and store in db"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly 9))
       (h/spy-fx spy-queue :json-rpc/call)

       (rf/dispatch [:activity-center.notifications/fetch-unread-count])

       (is (= "wakuext_unreadAndAcceptedActivityCenterNotificationsCount"
              (get-in @spy-queue [0 :args 0 :method])))
       (is (= 9 (get-in (h/db) [:activity-center :unread-count])))))))

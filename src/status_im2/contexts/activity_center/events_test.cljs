(ns status-im2.contexts.activity-center.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.constants :as constants]
            status-im.events
            [test-helpers.unit :as h]
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
     (setup)
     (rf/dispatch [:activity-center/open
                   {:filter-type   types/contact-request
                    :filter-status :unread}])

     (is (= {:status :unread
             :type   types/contact-request}
            (get-in (h/db) [:activity-center :filter])))))

  (testing "opens the activity center with default filters"
    (h/run-test-sync
     (setup)

     (rf/dispatch [:activity-center/open])

     (is (= {:status :unread :type types/no-type}
            (get-in (h/db) [:activity-center :filter]))))))

(deftest mark-as-read-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)
       (let [notifications [{:id   notification-id
                             :read false
                             :type types/one-to-one-chat}]]
         (rf/dispatch [:test/assoc-in [:activity-center :notifications] notifications])

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
                     {:filter        {:status :all :type types/no-type}
                      :notifications [notif-3 notif-2 notif-1]}])

       (rf/dispatch [:activity-center.notifications/mark-as-read (:id notif-2)])
       (is (= [notif-3 new-notif-2 notif-1]
              (get-in (h/db) [:activity-center :notifications])))

       (rf/dispatch [:activity-center.notifications/mark-as-read (:id notif-3)])
       (is (= [new-notif-3 new-notif-2 notif-1]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.notifications/mark-as-read notification-id]
      :action          :notification/mark-as-read
      :before-test     (fn []
                         (rf/dispatch
                          [:test/assoc-in [:activity-center :notifications]
                           [{:id   notification-id
                             :read false
                             :type types/one-to-one-chat}]]))})))

;;;; Acceptance/dismissal

(deftest notification-acceptance-test
  (testing "marks notification as accepted and read, then reconciles"
    (h/run-test-sync
     (setup)
     (let [notif-1          {:id "0x1" :type types/private-group-chat}
           notif-2          {:id "0x2" :type types/private-group-chat}
           notif-2-accepted (assoc notif-2 :accepted true :read true)]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly notif-2))
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter        {:type types/no-type :status :all}
                      :notifications [notif-2 notif-1]}])

       (rf/dispatch [:activity-center.notifications/accept (:id notif-2)])

       (is (= [notif-2-accepted notif-1]
              (get-in (h/db) [:activity-center :notifications])))

       ;; Ignores accepted notification if the Unread filter is enabled because
       ;; accepted notifications are also marked as read in status-go.
       (rf/dispatch [:test/assoc-in [:activity-center :filter]
                     {:filter {:type types/no-type :status :unread}}])
       (rf/dispatch [:activity-center.notifications/accept (:id notif-2)])
       (is (= [notif-1]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.notifications/accept notification-id]
      :action          :notification/accept})))

(deftest notification-dismissal-test
  (testing "dismisses notification, but keep it in the app db"
    (h/run-test-sync
     (setup)
     (let [notif-1           {:id "0x1" :type types/private-group-chat}
           notif-2           {:id "0x2" :type types/admin}
           dismissed-notif-1 (assoc notif-1 :dismissed true)]
       (h/stub-fx-with-callbacks :json-rpc/call :on-success (constantly notif-2))
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     [notif-2 notif-1]])

       (rf/dispatch [:activity-center.notifications/dismiss (:id notif-1)])

       (is (= [notif-2 dismissed-notif-1]
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

(deftest contact-verification-decline-test
  (testing "declines notification and reconciles"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks :json-rpc/call
                                 :on-success
                                 (constantly contact-verification-rpc-response))
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter {:type types/contact-verification :status :all}}])

       (rf/dispatch [:activity-center.contact-verification/decline notification-id])

       (is (= [contact-verification-expected-notification]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/decline notification-id]
      :action          :contact-verification/decline})))

(deftest contact-verification-reply-test
  (testing "sends reply and reconciles"
    (let [reply "any answer"]
      (h/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/stub-fx-with-callbacks :json-rpc/call
                                   :on-success
                                   (constantly contact-verification-rpc-response))
         (h/spy-fx spy-queue :json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center]
                       {:filter {:type types/contact-verification :status :all}}])

         (rf/dispatch [:activity-center.contact-verification/reply notification-id reply])

         (is (= [contact-verification-expected-notification]
                (get-in (h/db) [:activity-center :notifications])))))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/reply notification-id "any answer"]
      :action          :contact-verification/reply})))

(deftest contact-verification-mark-as-trusted-test
  (testing "app db reconciliation"
    (h/run-test-sync
     (setup)
     (h/stub-fx-with-callbacks :json-rpc/call
                               :on-success
                               (constantly contact-verification-rpc-response))

     ;; With "Unread" filter disabled
     (rf/dispatch [:test/assoc-in [:activity-center]
                   {:filter {:type types/no-type :status :all}}])
     (rf/dispatch [:activity-center.contact-verification/mark-as-trusted notification-id])
     (is (= [contact-verification-expected-notification]
            (get-in (h/db) [:activity-center :notifications])))

     ;; With "Unread" filter enabled
     (rf/dispatch [:test/assoc-in [:activity-center :filter :status] :unread])
     (rf/dispatch [:activity-center.contact-verification/mark-as-trusted notification-id])
     (is (= [] (get-in (h/db) [:activity-center :notifications])))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/mark-as-trusted notification-id]
      :action          :contact-verification/mark-as-trusted})))

(deftest contact-verification-mark-as-untrustworthy-test
  (testing "app db reconciliation"
    (h/run-test-sync
     (setup)
     (h/stub-fx-with-callbacks
      :json-rpc/call
      :on-success
      (constantly contact-verification-rpc-response))

     ;; With "Unread" filter disabled
     (rf/dispatch [:test/assoc-in [:activity-center]
                   {:filter {:type types/no-type :status :all}}])
     (rf/dispatch [:activity-center.contact-verification/mark-as-untrustworthy notification-id])
     (is (= [contact-verification-expected-notification]
            (get-in (h/db) [:activity-center :notifications])))

     ;; With "Unread" filter enabled
     (rf/dispatch [:test/assoc-in [:activity-center :filter :status] :unread])
     (rf/dispatch [:activity-center.contact-verification/mark-as-untrustworthy notification-id])
     (is (= [] (get-in (h/db) [:activity-center :notifications])))))

  (testing "logs on failure"
    (test-log-on-failure
     {:notification-id notification-id
      :event           [:activity-center.contact-verification/mark-as-untrustworthy notification-id]
      :action          :contact-verification/mark-as-untrustworthy})))

;;;; Notification reconciliation

(deftest notifications-reconcile-test
  (testing "All tab + All filter"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat}
           notif-2     {:id "0x2" :read false :type types/system}
           new-notif-3 {:id "0x3" :read false :type types/system}
           new-notif-4 {:id "0x4" :read true :type types/system}
           new-notif-2 (assoc notif-2 :read true)]
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter        {:type types/no-type :status :all}
                      :notifications [notif-2 notif-1]}])

       (rf/dispatch
        [:activity-center.notifications/reconcile
         [(assoc notif-1 :deleted true) ; will be removed
          new-notif-2
          new-notif-3
          new-notif-4]])

       (is (= [new-notif-4 new-notif-3 new-notif-2]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "All tab + Unread filter"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read false :type types/one-to-one-chat}
           notif-2     {:id "0x2" :read false :type types/system}
           new-notif-2 (assoc notif-2 :read true)
           new-notif-3 {:id "0x3" :read false :type types/system}
           new-notif-4 {:id "0x4" :read true :type types/system}
           notif-5     {:id "0x5" :type types/system}]
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter        {:type types/no-type :status :unread}
                      :notifications [notif-5 notif-2 notif-1]}])

       (rf/dispatch
        [:activity-center.notifications/reconcile
         [new-notif-2 ; will be removed because it's read
          new-notif-3 ; will be inserted
          new-notif-4 ; will be ignored because it's read
          (assoc notif-5 :deleted true) ; will be removed
         ]])

       (is (= [new-notif-3 notif-1]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "Contact request tab + All filter"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read true :type types/contact-request}
           notif-2     {:id "0x2" :read false :type types/contact-request}
           new-notif-2 (assoc notif-2 :read true)
           new-notif-3 {:id "0x3" :read false :type types/contact-request}
           new-notif-4 {:id "0x4" :read true :type types/system}
           notif-5     {:id "0x5" :read false :type types/contact-request}]
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter        {:type types/contact-request :status :all}
                      :notifications [notif-5 notif-2 notif-1]}])

       (rf/dispatch
        [:activity-center.notifications/reconcile
         [new-notif-2 ; will be updated
          new-notif-3 ; will be inserted
          new-notif-4 ; will be ignored because it's not a contact request
          (assoc notif-5 :deleted true) ; will be removed
         ]])

       (is (= [new-notif-3 new-notif-2 notif-1]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "Contact request tab + Unread filter"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :read false :type types/contact-request}
           notif-2     {:id "0x2" :read false :type types/contact-request}
           new-notif-2 (assoc notif-2 :read true)
           new-notif-3 {:id "0x3" :read false :type types/contact-request}
           new-notif-4 {:id "0x4" :read true :type types/contact-request}
           new-notif-5 {:id "0x5" :read true :type types/system}
           notif-6     {:id "0x6" :read false :type types/contact-request}]
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter        {:type types/contact-request :status :unread}
                      :notifications [notif-6 notif-2 notif-1]}])

       (rf/dispatch
        [:activity-center.notifications/reconcile
         [new-notif-2 ; will be removed because it's read
          new-notif-3 ; will be inserted
          new-notif-4 ; will be ignored because it's read
          new-notif-5 ; will be ignored because it's not a contact request
          (assoc notif-6 :deleted true) ; will be removed
         ]])

       (is (= [new-notif-3 notif-1]
              (get-in (h/db) [:activity-center :notifications]))))))

  ;; Sorting by timestamp and ID is compatible with what the backend does when
  ;; returning paginated results.
  (testing "sorts notifications by timestamp and id in descending order"
    (h/run-test-sync
     (setup)
     (let [notif-1     {:id "0x1" :timestamp 1}
           notif-2     {:id "0x2" :timestamp 1}
           notif-3     {:id "0x3" :timestamp 50}
           notif-4     {:id "0x4" :timestamp 100}
           notif-5     {:id "0x5" :timestamp 100}
           new-notif-1 (assoc notif-1 :last-message {})
           new-notif-4 (assoc notif-4 :last-message {})]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     [notif-1 notif-3 notif-4 notif-2 notif-5]])

       (rf/dispatch [:activity-center.notifications/reconcile [new-notif-1 new-notif-4]])

       (is (= [notif-5 new-notif-4 notif-3 notif-2 new-notif-1]
              (get-in (h/db) [:activity-center :notifications])))))))

(deftest remove-pending-contact-request-test
  (testing "removes notification from all related filter types and status"
    (h/run-test-sync
     (setup)
     (let [author  "0x99"
           notif-1 {:id "0x1" :read true :type types/contact-request}
           notif-2 {:id "0x2" :read false :type types/contact-request :author author}
           notif-3 {:id "0x3" :read false :type types/private-group-chat :author author}]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     [notif-3 ; will be ignored because it's not a contact request
                      notif-2 ; will be removed
                      notif-1 ; will be ignored because it's not from the same author
                     ]])

       (rf/dispatch [:activity-center/remove-pending-contact-request author])

       (is (= [notif-3 notif-1]
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
       (is (= "10" (get-in (h/db) [:activity-center :cursor])))
       (is (= [{:chat-id       "0x9"
                :chat-name     nil
                :chat-type     types/one-to-one-chat
                :group-chat    false
                :id            "0x1"
                :public?       false
                :last-message  nil
                :message       nil
                :read          false
                :reply-message nil
                :type          types/one-to-one-chat}]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "does not fetch next page when pagination cursor reached the end"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue :json-rpc/call)

       (rf/dispatch [:test/assoc-in [:activity-center :cursor] ""])
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
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter {:status :unread :type types/mention}
                      :cursor "10"}])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= "wakuext_activityCenterNotificationsBy" (get-in @spy-queue [0 :args 0 :method])))
       (is (= "10" (get-in @spy-queue [0 :args 0 :params 0]))
           "Should be called with current cursor")
       (is (= "" (get-in (h/db) [:activity-center :cursor])))
       (is (= [{:chat-id       "0x9"
                :chat-name     nil
                :chat-type     3
                :id            "0x1"
                :last-message  nil
                :message       nil
                :read          false
                :reply-message nil
                :type          types/mention}]
              (get-in (h/db) [:activity-center :notifications]))))))

  (testing "resets loading flag after an error"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks :json-rpc/call :on-error (constantly :fake-error))
       (h/spy-event-fx spy-queue :activity-center.notifications/fetch-error)
       (h/spy-fx spy-queue :json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center]
                     {:filter {:status :unread :type types/one-to-one-chat}
                      :cursor ""}])

       (rf/dispatch [:activity-center.notifications/fetch-first-page])

       (is (nil? (get-in (h/db) [:activity-center :loading?])))
       (is (= [:activity-center.notifications/fetch-error
               types/one-to-one-chat
               :unread
               :fake-error]
              (:args (last @spy-queue))))))))

(deftest notifications-fetch-unread-count-test
  (testing "fetches total notification count and store in db"
    (h/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks :json-rpc/call
                                 :on-success
                                 (fn [{:keys [params]}]
                                   (if (= types/mention (ffirst params))
                                     9
                                     0)))
       (h/spy-fx spy-queue :json-rpc/call)

       (rf/dispatch [:activity-center.notifications/fetch-unread-count])

       (is (= "wakuext_unreadAndAcceptedActivityCenterNotificationsCount"
              (get-in @spy-queue [0 :args 0 :method])))

       (let [actual (get-in (h/db) [:activity-center :unread-counts-by-type])]
         (is (= {types/one-to-one-chat      0
                 types/private-group-chat   0
                 types/contact-verification 0
                 types/contact-request      0
                 types/mention              9
                 types/reply                0
                 types/admin                0}
                actual))
         (is (= types/all-supported (set (keys actual)))))))))

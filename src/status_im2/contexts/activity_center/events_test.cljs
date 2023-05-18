(ns status-im2.contexts.activity-center.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.events :as events]
            [status-im2.contexts.activity-center.notification-types :as types]
            [test-helpers.unit :as h]))

(h/use-log-fixture)

(def notification-id "0x1")

;;;; Misc

(deftest open-activity-center-test
  (testing "opens the activity center with default filters"
    (is (= {:db             {}
            :dispatch       [:open-modal :activity-center {}]
            :dispatch-later [{:ms 1000 :dispatch [:activity-center/mark-as-seen]}]}
           (events/open-activity-center {:db {}} nil))))

  (testing "opens the activity center with filters enabled"
    (is (= {:db             {:activity-center {:filter {:status :unread :type types/contact-request}}}
            :dispatch       [:open-modal :activity-center {}]
            :dispatch-later [{:ms 1000 :dispatch [:activity-center/mark-as-seen]}]}
           (events/open-activity-center {:db {}}
                                        {:filter-type   types/contact-request
                                         :filter-status :unread})))))

(deftest process-notification-failure-test
  (testing "logs and returns nil"
    (is (nil? (events/process-notification-failure
               {:db {}}
               notification-id
               :some-action-name
               :some-error)))
    (is (= {:args  ["Failed to :some-action-name"
                    {:notification-id notification-id
                     :error           :some-error}]
            :level :warn}
           (last @h/logs)))))

;;;; Mark as read/unread

(deftest mark-as-read-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (let [cofx {:db {:activity-center
                     {:notifications [{:id   "0x1"
                                       :read false
                                       :type types/one-to-one-chat}]}}}]
      (is (nil? (events/mark-as-read cofx "0x99")))))

  (testing "dispatches RPC call"
    (let [notif {:id "0x1" :read false :type types/one-to-one-chat}
          cofx  {:db {:activity-center {:notifications [notif]}}}]
      (is (= {:json-rpc/call
              [{:method     "wakuext_markActivityCenterNotificationsRead"
                :params     [[(:id notif)]]
                :on-success [:activity-center.notifications/mark-as-read-success notif]
                :on-error   [:activity-center/process-notification-failure (:id notif)
                             :notification/mark-as-read]}]}
             (events/mark-as-read cofx (:id notif)))))))

(deftest mark-as-read-success-test
  (let [f-args (atom [])
        cofx   {:db {}}
        notif  {:id "0x1" :read false :type types/one-to-one-chat}]
    (with-redefs [events/notifications-reconcile
                  (fn [& args]
                    (reset! f-args args)
                    :result)]
      (is (= :result (events/mark-as-read-success cofx notif)))
      (is (= [cofx [(assoc notif :read true)]]
             @f-args)))))

(deftest mark-as-unread-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (let [cofx {:db {:activity-center
                     {:notifications [{:id   "0x1"
                                       :read true
                                       :type types/one-to-one-chat}]}}}]
      (is (nil? (events/mark-as-unread cofx "0x99")))))

  (testing "dispatches RPC call"
    (let [notif {:id "0x1" :read true :type types/one-to-one-chat}
          cofx  {:db {:activity-center {:notifications [notif]}}}]
      (is (= {:json-rpc/call
              [{:method     "wakuext_markActivityCenterNotificationsUnread"
                :params     [[(:id notif)]]
                :on-success [:activity-center.notifications/mark-as-unread-success notif]
                :on-error   [:activity-center/process-notification-failure (:id notif)
                             :notification/mark-as-unread]}]}
             (events/mark-as-unread cofx (:id notif)))))))

(deftest mark-as-unread-success-test
  (let [f-args (atom [])
        cofx   {:db {}}
        notif  {:id "0x1" :read true :type types/one-to-one-chat}]
    (with-redefs [events/notifications-reconcile
                  (fn [& args]
                    (reset! f-args args)
                    :reconciliation-result)]
      (is (= :reconciliation-result (events/mark-as-unread-success cofx notif)))
      (is (= [cofx [(assoc notif :read false)]]
             @f-args)))))

;;;; Acceptance/dismissal

(deftest accept-notification-test
  (is (= {:json-rpc/call
          [{:method     "wakuext_acceptActivityCenterNotifications"
            :params     [[notification-id]]
            :on-success [:activity-center.notifications/accept-success notification-id]
            :on-error   [:activity-center/process-notification-failure notification-id
                         :notification/accept]}]}
         (events/accept-notification {:db {}} notification-id))))

(deftest accept-notification-success-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (let [cofx {:db {:activity-center
                     {:notifications [{:id   "0x1"
                                       :read false
                                       :type types/one-to-one-chat}]}}}]
      (is (nil? (events/accept-notification-success cofx "0x99" nil)))))

  (testing "marks notification as accepted and read, then reconciles"
    (let [notif-1          {:id "0x1" :type types/private-group-chat}
          notif-2          {:id "0x2" :type types/private-group-chat}
          notif-2-accepted (assoc notif-2 :accepted true :read true)
          cofx             {:db {:activity-center {:filter        {:type types/no-type :status :all}
                                                   :notifications [notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center {:filter        {:type 0 :status :all}
                                             :notifications [notif-2-accepted notif-1]}
                           :chats           {}
                           :chats-home-list nil}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/accept-notification-success cofx (:id notif-2) nil))))))

(deftest dismiss-notification-test
  (is (= {:json-rpc/call
          [{:method     "wakuext_dismissActivityCenterNotifications"
            :params     [[notification-id]]
            :on-success [:activity-center.notifications/dismiss-success notification-id]
            :on-error   [:activity-center/process-notification-failure notification-id
                         :notification/dismiss]}]}
         (events/dismiss-notification {:db {}} notification-id))))

(deftest dismiss-notification-success-test
  (testing "does nothing if the notification ID cannot be found in the app db"
    (let [cofx {:db {:activity-center
                     {:notifications [{:id   "0x1"
                                       :read false
                                       :type types/one-to-one-chat}]}}}]
      (is (nil? (events/dismiss-notification-success cofx "0x99")))))

  (testing "marks notification as dismissed and read, then reconciles"
    (let [notif-1           {:id "0x1" :type types/private-group-chat}
          notif-2           {:id "0x2" :type types/private-group-chat}
          notif-2-dismissed (assoc notif-2 :dismissed true :read true)
          cofx              {:db {:activity-center {:filter        {:type types/no-type :status :all}
                                                    :notifications [notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center {:filter        {:type 0 :status :all}
                                             :notifications [notif-2-dismissed notif-1]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/dismiss-notification-success cofx (:id notif-2)))))))

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
  (is (= {:json-rpc/call
          [{:method     "wakuext_declineContactVerificationRequest"
            :params     [notification-id]
            :on-success [:activity-center/reconcile-notifications-from-response]
            :on-error   [:activity-center/process-notification-failure notification-id
                         :contact-verification/decline]}]}
         (events/contact-verification-decline {:db {}} notification-id))))

(deftest contact-verification-reply-test
  (let [reply "The answer is 42"]
    (is (= {:json-rpc/call
            [{:method     "wakuext_acceptContactVerificationRequest"
              :params     [notification-id reply]
              :on-success [:activity-center/reconcile-notifications-from-response]
              :on-error   [:activity-center/process-notification-failure notification-id
                           :contact-verification/reply]}]}
           (events/contact-verification-reply {:db {}} notification-id reply)))))

(deftest contact-verification-mark-as-trusted-test
  (is (= {:json-rpc/call
          [{:method     "wakuext_verifiedTrusted"
            :params     [{:id notification-id}]
            :on-success [:activity-center/reconcile-notifications-from-response]
            :on-error   [:activity-center/process-notification-failure notification-id
                         :contact-verification/mark-as-trusted]}]}
         (events/contact-verification-mark-as-trusted {:db {}} notification-id))))

(deftest contact-verification-mark-as-untrustworthy-test
  (is (= {:json-rpc/call
          [{:method     "wakuext_verifiedUntrustworthy"
            :params     [{:id notification-id}]
            :on-success [:activity-center/reconcile-notifications-from-response]
            :on-error   [:activity-center/process-notification-failure notification-id
                         :contact-verification/mark-as-untrustworthy]}]}
         (events/contact-verification-mark-as-untrustworthy {:db {}} notification-id))))

;;;; Notification reconciliation

(deftest notifications-reconcile-test
  (testing "All tab + All filter"
    (let [notif-1     {:id "0x1" :read true :type types/one-to-one-chat}
          notif-2     {:id "0x2" :read false :type types/system}
          new-notif-3 {:id "0x3" :read false :type types/system}
          new-notif-4 {:id "0x4" :read true :type types/system}
          new-notif-2 (assoc notif-2 :read true)
          cofx        {:db {:activity-center
                            {:filter        {:type types/no-type :status :all}
                             :notifications [notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center
                           {:filter        {:type types/no-type :status :all}
                            :notifications [new-notif-4 new-notif-3 new-notif-2]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/notifications-reconcile
              cofx
              [(assoc notif-1 :deleted true) ; will be removed
               new-notif-2
               new-notif-3
               new-notif-4])))))

  (testing "All tab + Unread filter"
    (let [notif-1     {:id "0x1" :read false :type types/one-to-one-chat}
          notif-2     {:id "0x2" :read false :type types/system}
          new-notif-2 (assoc notif-2 :read true)
          new-notif-3 {:id "0x3" :read false :type types/system}
          new-notif-4 {:id "0x4" :read true :type types/system}
          notif-5     {:id "0x5" :type types/system}
          cofx        {:db {:activity-center
                            {:filter        {:type types/no-type :status :unread}
                             :notifications [notif-5 notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center
                           {:filter        {:type types/no-type :status :unread}
                            :notifications [new-notif-3 notif-1]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/notifications-reconcile
              cofx
              [new-notif-2 ; will be removed because it's read
               new-notif-3 ; will be inserted
               new-notif-4 ; will be ignored because it's read
               (assoc notif-5 :deleted true) ; will be removed
              ])))))

  (testing "Contact request tab + All filter"
    (let [notif-1     {:id "0x1" :read true :type types/contact-request}
          notif-2     {:id "0x2" :read false :type types/contact-request}
          new-notif-2 (assoc notif-2 :read true)
          new-notif-3 {:id "0x3" :read false :type types/contact-request}
          new-notif-4 {:id "0x4" :read true :type types/system}
          notif-5     {:id "0x5" :read false :type types/contact-request}
          cofx        {:db {:activity-center
                            {:filter        {:type types/contact-request :status :all}
                             :notifications [notif-5 notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center
                           {:filter        {:type types/contact-request :status :all}
                            :notifications [new-notif-3 new-notif-2 notif-1]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/notifications-reconcile
              cofx
              [new-notif-2 ; will be updated
               new-notif-3 ; will be inserted
               new-notif-4 ; will be ignored because it's not a contact request
               (assoc notif-5 :deleted true) ; will be removed
              ])))))

  (testing "Contact request tab + Unread filter"
    (let [notif-1     {:id "0x1" :read false :type types/contact-request}
          notif-2     {:id "0x2" :read false :type types/contact-request}
          new-notif-2 (assoc notif-2 :read true)
          new-notif-3 {:id "0x3" :read false :type types/contact-request}
          new-notif-4 {:id "0x4" :read true :type types/contact-request}
          new-notif-5 {:id "0x5" :read true :type types/system}
          notif-6     {:id "0x6" :read false :type types/contact-request}
          cofx        {:db {:activity-center
                            {:filter        {:type   types/contact-request
                                             :status :unread}
                             :notifications [notif-6 notif-2 notif-1]}}}]
      (is (= {:db         {:activity-center
                           {:filter        {:type   types/contact-request
                                            :status :unread}
                            :notifications [new-notif-3 notif-1]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/notifications-reconcile
              cofx
              [new-notif-2 ; will be removed because it's read
               new-notif-3 ; will be inserted
               new-notif-4 ; will be ignored because it's read
               new-notif-5 ; will be ignored because it's not a contact request
               (assoc notif-6 :deleted true) ; will be removed
              ])))))

  ;; Sorting by timestamp and ID is compatible with what the backend does when
  ;; returning paginated results.
  (testing "sorts notifications by timestamp and id in descending order"
    (let [notif-1     {:id "0x1" :timestamp 1}
          notif-2     {:id "0x2" :timestamp 1}
          notif-3     {:id "0x3" :timestamp 50}
          notif-4     {:id "0x4" :timestamp 100}
          notif-5     {:id "0x5" :timestamp 100}
          new-notif-1 (assoc notif-1 :last-message {})
          new-notif-4 (assoc notif-4 :last-message {})
          cofx        {:db {:activity-center
                            {:notifications [notif-1 notif-3 notif-4 notif-2
                                             notif-5]}}}]
      (is (= {:db         {:activity-center
                           {:notifications [notif-5
                                            new-notif-4
                                            notif-3
                                            notif-2
                                            new-notif-1]}}
              :dispatch-n [[:activity-center.notifications/fetch-unread-count]
                           [:activity-center.notifications/fetch-pending-contact-requests]]}
             (events/notifications-reconcile cofx [new-notif-1 new-notif-4]))))))

(deftest remove-pending-contact-request-test
  (testing "removes notification from all related filter types and status"
    (let [author  "0x99"
          notif-1 {:id "0x1" :read true :type types/contact-request}
          notif-2 {:id "0x2" :read false :type types/contact-request :author author}
          notif-3 {:id "0x3" :read false :type types/private-group-chat :author author}
          cofx    {:db {:activity-center
                        {:notifications
                         [notif-3 ; will be ignored because it's not a contact
                                  ; request
                          notif-2 ; will be removed
                          notif-1 ; will be ignored because it's not from the
                                  ; same author
                         ]}}}]
      (is (= {:db {:activity-center {:notifications [notif-3 notif-1]}}}
             (events/notifications-remove-pending-contact-request cofx author))))))

;;;; Notifications fetching and pagination

(deftest notifications-fetch-first-page-test
  (testing "fetches first page"
    (let [cofx {:db {}}]
      (is (= {:db            {:activity-center {:filter   {:type   types/one-to-one-chat
                                                           :status :unread}
                                                :loading? true}}
              :json-rpc/call [{:method     "wakuext_activityCenterNotifications"
                               :params     [{:cursor        ""
                                             :limit         (:notifications-per-page events/defaults)
                                             :activityTypes [types/one-to-one-chat]
                                             :readType      events/read-type-unread}]
                               :on-success [:activity-center.notifications/fetch-success true]
                               :on-error   [:activity-center.notifications/fetch-error
                                            types/one-to-one-chat :unread]}]}
             (events/notifications-fetch-first-page cofx {:filter-type types/one-to-one-chat}))))))

(deftest notifications-fetch-next-next-page-test
  (testing "does not fetch next page when pagination cursor reached the end"
    (is (nil? (events/notifications-fetch-next-page
               {:db {:activity-center {:cursor events/start-or-end-cursor}}}))))

  (testing "fetches the next page"
    (let [f-args (atom [])
          cursor "abc"
          cofx   {:db {:activity-center {:cursor cursor
                                         :filter {:type   types/one-to-one-chat
                                                  :status :unread}}}}]
      (with-redefs [events/notifications-fetch
                    (fn [& args]
                      (reset! f-args args)
                      :result)]
        (is (= :result (events/notifications-fetch-next-page cofx)))
        (is (= [cofx
                {:cursor        cursor
                 :filter-type   types/one-to-one-chat
                 :filter-status :unread
                 :reset-data?   false}]
               @f-args))))))

(deftest notifications-fetch-error-test
  (testing "resets loading state"
    (let [cofx {:db {:activity-center
                     {:loading? true
                      :filter   {:status :unread
                                 :type   types/one-to-one-chat}
                      :cursor   ""}}}]
      (is (= {:db {:activity-center {:filter {:status :unread
                                              :type   types/one-to-one-chat}
                                     :cursor ""}}}
             (events/notifications-fetch-error cofx :dummy-error))))))

(ns status-im.activity-center.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [status-im.activity-center.notification-types :as types]
            [status-im.ethereum.json-rpc :as json-rpc]
            status-im.events
            [status-im.test-helpers :as h]
            [status-im.utils.config :as config]))

(defn setup []
  (h/register-helper-events)
  (rf/dispatch [:init/app-started]))

;;;; Contact verification

(deftest contact-verification-decline-test
  (with-redefs [config/new-activity-center-enabled? true]
    (testing "successfully declines and reconciles returned notification"
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue               (atom [])
             contact-verification-id 24
             expected-notification   {:accepted                    false
                                      :author                      "0x04d03f"
                                      :chat-id                     "0x04d03f"
                                      :contact-verification-status 3
                                      :dismissed                   false
                                      :id                          24
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
                                      :type                        types/contact-verification}]
         (h/stub-fx-with-callbacks
          ::json-rpc/call
          :on-success (constantly {:activityCenterNotifications
                                   [{:accepted                  false
                                     :author                    "0x04d03f"
                                     :chatId                    "0x04d03f"
                                     :contactVerificationStatus 3
                                     :dismissed                 false
                                     :id                        contact-verification-id
                                     :message                   {}
                                     :name                      "0x04d03f"
                                     :read                      true
                                     :timestamp                 1666647286000
                                     :type                      types/contact-verification}]}))

         (h/spy-fx spy-queue ::json-rpc/call)

         (rf/dispatch [:activity-center.contact-verification/decline contact-verification-id])

         (is (= {:method "wakuext_declineContactVerificationRequest"
                 :params [contact-verification-id]}
                (-> @spy-queue
                    (get-in [0 :args 0])
                    (select-keys [:method :params]))))

         (is (= {types/no-type
                 {:read   {:data [expected-notification]}
                  :unread {:data []}}
                 types/contact-verification
                 {:read   {:data [expected-notification]}
                  :unread {:data []}}}
                (get-in (h/db) [:activity-center :notifications]))))))

    (testing "logs failure"
      (rf-test/run-test-sync
       (setup)
       (let [contact-verification-id 666]
         (h/using-log-test-appender
          (fn [logs]
            (h/stub-fx-with-callbacks ::json-rpc/call :on-error (constantly :fake-error))

            (rf/dispatch [:activity-center.contact-verification/decline contact-verification-id])

            (is (= {:args  ["Failed to decline contact verification"
                            {:contact-verification-id contact-verification-id
                             :error                   :fake-error}]
                    :level :warn}
                   (last @logs))))))))))

;;;; Notification reconciliation

(deftest notifications-reconcile-test
  (with-redefs [config/new-activity-center-enabled? true]
    (testing "does nothing when there are no new notifications"
      (rf-test/run-test-sync
       (setup)
       (let [notifications {types/one-to-one-chat
                            {:read   {:cursor ""
                                      :data   [{:id   "0x1"
                                                :read true
                                                :type types/one-to-one-chat}
                                               {:id   "0x2"
                                                :read true
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

    (testing "removes dismissed or accepted notifications"
      (rf-test/run-test-sync
       (setup)
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:read   {:cursor ""
                                :data   [{:id "0x1" :read true :type types/one-to-one-chat}
                                         {:id "0x2" :read true :type types/one-to-one-chat}]}
                       :unread {:cursor ""
                                :data   [{:id "0x3" :read false :type types/one-to-one-chat}]}}
                      types/private-group-chat
                      {:unread {:cursor ""
                                :data   [{:id "0x4" :read false :type types/private-group-chat}
                                         {:id "0x6" :read false :type types/private-group-chat}]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile
                     [{:id        "0x1"
                       :read      true
                       :type      types/one-to-one-chat
                       :dismissed true}
                      {:id       "0x3"
                       :read     false
                       :type     types/one-to-one-chat
                       :accepted true}
                      {:id        "0x4"
                       :read      false
                       :type      types/private-group-chat
                       :dismissed true}
                      {:id       "0x5"
                       :read     false
                       :type     types/private-group-chat
                       :accepted true}]])

       (is (= {types/no-type
               {:read   {:data []}
                :unread {:data []}}

               types/one-to-one-chat
               {:read   {:cursor ""
                         :data   [{:id   "0x2"
                                   :read true
                                   :type types/one-to-one-chat}]}
                :unread {:cursor ""
                         :data   []}}
               types/private-group-chat
               {:read   {:data []}
                :unread {:cursor ""
                         :data   [{:id   "0x6"
                                   :read false
                                   :type types/private-group-chat}]}}}
              (get-in (h/db) [:activity-center :notifications])))))

    (testing "replaces old notifications with newly arrived ones"
      (rf-test/run-test-sync
       (setup)
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/no-type
                      {:read   {:cursor ""
                                :data   [{:id   "0x1"
                                          :read true
                                          :type types/one-to-one-chat}]}
                       :unread {:cursor ""
                                :data   [{:id   "0x4"
                                          :read false
                                          :type types/private-group-chat}
                                         {:id   "0x6"
                                          :read false
                                          :type types/private-group-chat}]}}
                      types/one-to-one-chat
                      {:read {:cursor ""
                              :data   [{:id   "0x1"
                                        :read true
                                        :type types/one-to-one-chat}]}}
                      types/private-group-chat
                      {:unread {:cursor ""
                                :data   [{:id   "0x4"
                                          :read false
                                          :type types/private-group-chat}
                                         {:id   "0x6"
                                          :read false
                                          :type types/private-group-chat}]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile
                     [{:id           "0x1"
                       :read         true
                       :type         types/one-to-one-chat
                       :last-message {}}
                      {:id     "0x4"
                       :read   false
                       :type   types/private-group-chat
                       :author "0xabc"}
                      {:id   "0x6"
                       :read false
                       :type types/private-group-chat}]])

       (is (= {types/no-type
               {:read   {:cursor ""
                         :data   [{:id           "0x1"
                                   :read         true
                                   :type         types/one-to-one-chat
                                   :last-message {}}]}
                :unread {:cursor ""
                         :data   [{:id   "0x6"
                                   :read false
                                   :type types/private-group-chat}
                                  {:id     "0x4"
                                   :read   false
                                   :type   types/private-group-chat
                                   :author "0xabc"}]}}
               types/one-to-one-chat
               {:read   {:cursor ""
                         :data   [{:id           "0x1"
                                   :read         true
                                   :type         types/one-to-one-chat
                                   :last-message {}}]}
                :unread {:data []}}
               types/private-group-chat
               {:read   {:data []}
                :unread {:cursor ""
                         :data   [{:id   "0x6"
                                   :read false
                                   :type types/private-group-chat}
                                  {:id     "0x4"
                                   :read   false
                                   :type   types/private-group-chat
                                   :author "0xabc"}]}}}
              (get-in (h/db) [:activity-center :notifications])))))

    (testing "reconciles notifications that switched their read/unread status"
      (rf-test/run-test-sync
       (setup)
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:read {:cursor ""
                              :data   [{:id   "0x1"
                                        :read true
                                        :type types/one-to-one-chat}]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile
                     [{:id   "0x1"
                       :read false
                       :type types/one-to-one-chat}]])

       (is (= {types/no-type
               {:read   {:data []}
                :unread {:data [{:id   "0x1"
                                 :read false
                                 :type types/one-to-one-chat}]}}

               types/one-to-one-chat
               {:read   {:cursor ""
                         :data   []}
                :unread {:data [{:id   "0x1"
                                 :read false
                                 :type types/one-to-one-chat}]}}}
              (get-in (h/db) [:activity-center :notifications])))))

    ;; Sorting by timestamp and ID is compatible with what the backend does when
    ;; returning paginated results.
    (testing "sorts notifications by timestamp and id in descending order"
      (rf-test/run-test-sync
       (setup)
       (rf/dispatch [:test/assoc-in [:activity-center :notifications]
                     {types/one-to-one-chat
                      {:read   {:cursor ""
                                :data   [{:id "0x1" :read true :type types/one-to-one-chat :timestamp 1}
                                         {:id "0x2" :read true :type types/one-to-one-chat :timestamp 1}]}
                       :unread {:cursor ""
                                :data   [{:id "0x3" :read false :type types/one-to-one-chat :timestamp 50}
                                         {:id "0x4" :read false :type types/one-to-one-chat :timestamp 100}
                                         {:id "0x5" :read false :type types/one-to-one-chat :timestamp 100}]}}}])

       (rf/dispatch [:activity-center.notifications/reconcile
                     [{:id "0x1" :read true :type types/one-to-one-chat :timestamp 1 :last-message {}}
                      {:id "0x4" :read false :type types/one-to-one-chat :timestamp 100 :last-message {}}]])

       (is (= {types/no-type
               {:read   {:data [{:id           "0x1"
                                 :read         true
                                 :type         types/one-to-one-chat
                                 :timestamp    1
                                 :last-message {}}]}
                :unread {:data [{:id           "0x4"
                                 :read         false
                                 :type         types/one-to-one-chat
                                 :timestamp    100
                                 :last-message {}}]}}
               types/one-to-one-chat
               {:read   {:cursor ""
                         :data   [{:id        "0x2"
                                   :read      true
                                   :type      types/one-to-one-chat
                                   :timestamp 1}
                                  {:id           "0x1"
                                   :read         true
                                   :type         types/one-to-one-chat
                                   :timestamp    1
                                   :last-message {}}]}
                :unread {:cursor ""
                         :data   [{:id        "0x5"
                                   :read      false
                                   :type      types/one-to-one-chat
                                   :timestamp 100}
                                  {:id           "0x4"
                                   :read         false
                                   :type         types/one-to-one-chat
                                   :timestamp    100
                                   :last-message {}}
                                  {:id        "0x3"
                                   :read      false
                                   :type      types/one-to-one-chat
                                   :timestamp 50}]}}}
              (get-in (h/db) [:activity-center :notifications])))))))

;;;; Notifications fetching and pagination

(deftest notifications-fetch-test
  (with-redefs [config/new-activity-center-enabled? true]
    (testing "fetches first page"
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/stub-fx-with-callbacks
          ::json-rpc/call
          :on-success (constantly {:cursor        "10"
                                   :notifications [{:id     "0x1"
                                                    :type   types/one-to-one-chat
                                                    :read   false
                                                    :chatId "0x9"}]}))
         (h/spy-fx spy-queue ::json-rpc/call)

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
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/spy-fx spy-queue ::json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                       :unread])
         (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                       types/one-to-one-chat])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                       ""])

         (rf/dispatch [:activity-center.notifications/fetch-next-page])

         (is (= [] @spy-queue)))))

    ;; The cursor can be nil sometimes because the reconciliation doesn't care
    ;; about updating the cursor value, but we have to make sure the next page is
    ;; only fetched if the current cursor is valid.
    (testing "does not fetch next page when cursor is nil"
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/spy-fx spy-queue ::json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                       :unread])
         (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                       types/one-to-one-chat])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                       nil])

         (rf/dispatch [:activity-center.notifications/fetch-next-page])

         (is (= [] @spy-queue)))))

    (testing "fetches next page when pagination cursor is not empty"
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/stub-fx-with-callbacks
          ::json-rpc/call
          :on-success (constantly {:cursor        ""
                                   :notifications [{:id     "0x1"
                                                    :type   types/mention
                                                    :read   false
                                                    :chatId "0x9"}]}))
         (h/spy-fx spy-queue ::json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                       :unread])
         (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                       types/mention])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/mention :unread :cursor]
                       "10"])

         (rf/dispatch [:activity-center.notifications/fetch-next-page])

         (is (= "wakuext_unreadActivityCenterNotifications" (get-in @spy-queue [0 :args 0 :method])))
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
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/spy-fx spy-queue ::json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                       :read])
         (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                       types/one-to-one-chat])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :read :cursor]
                       "10"])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :read :loading?]
                       true])

         (rf/dispatch [:activity-center.notifications/fetch-next-page])

         (is (= [] @spy-queue)))))

    (testing "resets loading flag after an error"
      (rf-test/run-test-sync
       (setup)
       (let [spy-queue (atom [])]
         (h/stub-fx-with-callbacks ::json-rpc/call :on-error (constantly :fake-error))
         (h/spy-event-fx spy-queue :activity-center.notifications/fetch-error)
         (h/spy-fx spy-queue ::json-rpc/call)
         (rf/dispatch [:test/assoc-in [:activity-center :filter :status]
                       :unread])
         (rf/dispatch [:test/assoc-in [:activity-center :filter :type]
                       types/one-to-one-chat])
         (rf/dispatch [:test/assoc-in [:activity-center :notifications types/one-to-one-chat :unread :cursor]
                       ""])

         (rf/dispatch [:activity-center.notifications/fetch-first-page])

         (is (nil? (get-in (h/db) [:activity-center :notifications types/one-to-one-chat :unread :loading?])))
         (is (= [:activity-center.notifications/fetch-error
                 types/one-to-one-chat
                 :unread
                 :fake-error]
                (:args (last @spy-queue)))))))))

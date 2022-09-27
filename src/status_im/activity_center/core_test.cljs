(ns status-im.activity-center.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.test-helpers :as h]
            status-im.events))

(defn setup []
  (h/register-helper-events)
  (rf/dispatch [:init/app-started]))

(deftest notifications-reconcile-test
  (testing "does nothing when there are no new notifications"
    (rf-test/run-test-sync
     (setup)
     (let [read   [{:id "0x1" :read true}]
           unread [{:id "0x4" :read false}]]
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-read :data] read])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :data] unread])

       (rf/dispatch [:activity-center.notifications/reconcile nil])

       (is (= read (get-in (h/db) [:activity-center :notifications-read :data])))
       (is (= unread (get-in (h/db) [:activity-center :notifications-unread :data]))))))

  (testing "removes dismissed or accepted notifications"
    (rf-test/run-test-sync
     (setup)
     (rf/dispatch [:test/assoc-in [:activity-center :notifications-read :data]
                   [{:id "0x1" :read true}
                    {:id "0x2" :read true}
                    {:id "0x3" :read true}]])
     (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :data]
                   [{:id "0x4" :read false}
                    {:id "0x5" :read false}
                    {:id "0x6" :read false}]])

     (rf/dispatch [:activity-center.notifications/reconcile
                   [{:id "0x1" :read true :dismissed true}
                    {:id "0x3" :read true :accepted true}
                    {:id "0x4" :read false :dismissed true}
                    {:id "0x5" :read false :accepted true}]])

     (is (= [{:id "0x2" :read true}]
            (get-in (h/db) [:activity-center :notifications-read :data])))
     (is (= [{:id "0x6" :read false}]
            (get-in (h/db) [:activity-center :notifications-unread :data])))))

  (testing "replaces old notifications with newly arrived ones"
    (rf-test/run-test-sync
     (setup)
     (rf/dispatch [:test/assoc-in [:activity-center :notifications-read :data]
                   [{:id "0x1" :read true}
                    {:id "0x2" :read true}]])
     (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :data]
                   [{:id "0x3" :read false}
                    {:id "0x4" :read false}]])

     (rf/dispatch [:activity-center.notifications/reconcile
                   [{:id "0x1" :read true :name "ABC"}
                    {:id "0x3" :read false :name "XYZ"}]])

     (is (= [{:id "0x1" :read true :name "ABC"}
             {:id "0x2" :read true}]
            (get-in (h/db) [:activity-center :notifications-read :data])))
     (is (= [{:id "0x3" :read false :name "XYZ"}
             {:id "0x4" :read false}]
            (get-in (h/db) [:activity-center :notifications-unread :data]))))))

(deftest notifications-fetch-test
  (testing "fetches first page"
    (rf-test/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks ::json-rpc/call
                                 :on-success (constantly {:cursor        "10"
                                                          :notifications [{:chatId "0x1"}]}))
       (h/spy-fx spy-queue ::json-rpc/call)

       (rf/dispatch [:activity-center.notifications/fetch-first-page])

       (is (= :unread (get-in (h/db) [:activity-center :current-status-filter])))
       (is (nil? (get-in (h/db) [:activity-center :notifications-unread :loading?])))
       (is (= "10" (get-in (h/db) [:activity-center :notifications-unread :cursor])))
       (is (= "" (get-in @spy-queue [0 :args 0 :params 0])))
       (is (= [{:chat-id "0x1"}]
              (->> (get-in (h/db) [:activity-center :notifications-unread :data])
                   (map #(select-keys % [:chat-id]))))))))

  (testing "does not fetch next page when pagination cursor reached the end"
    (rf-test/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue ::json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :current-status-filter] :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :cursor] ""])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= [] @spy-queue)))))

  (testing "fetches next page when pagination cursor is not empty"
    (rf-test/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks ::json-rpc/call
                                 :on-success (constantly {:cursor        ""
                                                          :notifications [{:chatId "0x1"}]}))
       (h/spy-fx spy-queue ::json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :current-status-filter] :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :cursor] "10"])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= "wakuext_unreadActivityCenterNotifications" (get-in @spy-queue [0 :args 0 :method])))
       (is (= "10" (get-in @spy-queue [0 :args 0 :params 0])))
       (is (= "" (get-in (h/db) [:activity-center :notifications-unread :cursor])))
       (is (= [{:chat-id "0x1"}]
              (->> (get-in (h/db) [:activity-center :notifications-unread :data])
                   (map #(select-keys % [:chat-id]))))))))

  (testing "does not fetch next page while it is still loading"
    (rf-test/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/spy-fx spy-queue ::json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :current-status-filter] :read])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-read :cursor] "10"])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-read :loading?] true])

       (rf/dispatch [:activity-center.notifications/fetch-next-page])

       (is (= [] @spy-queue)))))

  (testing "resets loading flag after an error"
    (rf-test/run-test-sync
     (setup)
     (let [spy-queue (atom [])]
       (h/stub-fx-with-callbacks ::json-rpc/call :on-error (constantly :fake-error))
       (h/spy-event-fx spy-queue :activity-center.notifications/fetch-error)
       (h/spy-fx spy-queue ::json-rpc/call)
       (rf/dispatch [:test/assoc-in [:activity-center :current-status-filter] :unread])
       (rf/dispatch [:test/assoc-in [:activity-center :notifications-unread :cursor] ""])

       (rf/dispatch [:activity-center.notifications/fetch-first-page])

       (is (nil? (get-in (h/db) [:activity-center :notifications-unread :loading?])))
       (is (= [:activity-center.notifications/fetch-error
               :notifications-unread
               :fake-error]
              (:args (last @spy-queue))))))))

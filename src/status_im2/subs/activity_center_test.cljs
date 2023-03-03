(ns status-im2.subs.activity-center-test
  (:require [cljs.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification-types :as types]
            status-im2.subs.activity-center
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(h/deftest-sub :activity-center/filter-status-unread-enabled?
  [sub-name]
  (testing "returns true when filter status is unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :unread)
    (is (true? (rf/sub [sub-name]))))

  (testing "returns false when filter status is not unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :all)
    (is (false? (rf/sub [sub-name])))))

(h/deftest-sub :activity-center/notification-types-with-unread
  [sub-name]
  (testing "returns an empty set when no types have unread notifications"
    (swap! rf-db/app-db assoc-in
      [:activity-center :unread-counts-by-type]
      {types/one-to-one-chat      0
       types/private-group-chat   0
       types/contact-verification 0
       types/contact-request      0
       types/mention              0
       types/reply                0
       types/admin                0})

    (is (= #{} (rf/sub [sub-name]))))

  (testing "returns a set with all types containing positive unread counts"
    (swap! rf-db/app-db assoc-in
      [:activity-center :unread-counts-by-type]
      {types/one-to-one-chat      1
       types/private-group-chat   0
       types/contact-verification 1
       types/contact-request      0
       types/mention              3
       types/reply                0
       types/admin                2})

    (let [actual (rf/sub [sub-name])]
      (is (= #{types/one-to-one-chat
               types/contact-verification
               types/mention
               types/admin}
             actual))
      (is (set? actual)))))

(h/deftest-sub :activity-center/unread-count
  [sub-name]
  (swap! rf-db/app-db assoc-in
    [:activity-center :unread-counts-by-type]
    {types/one-to-one-chat      1
     types/private-group-chat   2
     types/contact-verification 3
     types/contact-request      4
     types/mention              5
     types/reply                6
     types/admin                7})

  (is (= 28 (rf/sub [sub-name]))))

(h/deftest-sub :activity-center/pending-contact-requests
  [sub-name]
  (testing "returns only contact request notifications in the pending state"
    (let [pending {:id      "0x2"
                   :type    types/contact-request
                   :message {:contact-request-state
                             constants/contact-request-message-state-pending}}]
      (swap! rf-db/app-db assoc-in
        [:activity-center :notifications types/contact-request :unread :data]
        [{:id      "0x1"
          :type    types/contact-request
          :message {:contact-request-state constants/contact-request-message-state-none}}
         pending
         {:id      "0x3"
          :type    types/contact-request
          :message {:contact-request-state constants/contact-request-message-state-accepted}}
         {:id      "0x4"
          :type    types/contact-request
          :message {:contact-request-state constants/contact-request-message-state-declined}}
         {:id   "0x5"
          :type types/mention}])

      (is (= [pending] (rf/sub [sub-name]))))))

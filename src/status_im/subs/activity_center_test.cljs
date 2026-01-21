(ns status-im.subs.activity-center-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.contexts.shell.activity-center.notification-types :as types]
    status-im.subs.activity-center
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
      {types/one-to-one-chat           0
       types/private-group-chat        0
       types/contact-verification      0
       types/contact-request           0
       types/mention                   0
       types/reply                     0
       types/admin                     0
       types/new-installation-received 0
       types/new-installation-created  0})

    (is (= #{} (rf/sub [sub-name]))))

  (testing "returns a set with all types containing positive unread counts"
    (swap! rf-db/app-db assoc-in
      [:activity-center :unread-counts-by-type]
      {types/one-to-one-chat           1
       types/private-group-chat        0
       types/contact-verification      1
       types/contact-request           0
       types/mention                   3
       types/reply                     0
       types/admin                     2
       types/new-installation-received 1
       types/new-installation-created  0})

    (let [actual (rf/sub [sub-name])]
      (is (= #{types/one-to-one-chat
               types/contact-verification
               types/mention
               types/admin
               types/new-installation-received}
             actual))
      (is (set? actual)))))

(h/deftest-sub :activity-center/unread-count
  [sub-name]
  (swap! rf-db/app-db assoc-in
    [:activity-center :unread-counts-by-type]
    {types/one-to-one-chat           1
     types/private-group-chat        2
     types/contact-verification      3
     types/contact-request           4
     types/mention                   5
     types/reply                     6
     types/admin                     7
     types/new-installation-received 8
     types/new-installation-created  9})

  (is (= 45 (rf/sub [sub-name]))))

(h/deftest-sub :activity-center/unread-indicator
  [sub-name]
  (testing "not seen and no unread notifications"
    (swap! rf-db/app-db assoc-in [:activity-center :unread-counts-by-type] {types/one-to-one-chat 0})
    (swap! rf-db/app-db assoc-in [:activity-center :seen?] false)
    (is (= :unread-indicator/none (rf/sub [sub-name]))))

  (testing "not seen and one or more unread notifications"
    (swap! rf-db/app-db assoc-in [:activity-center :unread-counts-by-type] {types/one-to-one-chat 1})
    (swap! rf-db/app-db assoc-in [:activity-center :seen?] false)
    (is (= :unread-indicator/new (rf/sub [sub-name]))))

  (testing "seen and no unread notifications"
    (swap! rf-db/app-db assoc-in [:activity-center :unread-counts-by-type] {types/one-to-one-chat 0})
    (swap! rf-db/app-db assoc-in [:activity-center :seen?] true)
    (is (= :unread-indicator/none (rf/sub [sub-name]))))

  (testing "seen and one or more unread notifications"
    (swap! rf-db/app-db assoc-in [:activity-center :unread-counts-by-type] {types/one-to-one-chat 1})
    (swap! rf-db/app-db assoc-in [:activity-center :seen?] true)
    (is (= :unread-indicator/seen (rf/sub [sub-name])))))

(h/deftest-sub :activity-center/pending-contact-request-from-contact-id
  [sub-name]
  (testing "returns contact request data if it finds a matching contact-id"
    (let [contact-id      "0x01"
          contact-request {:author  contact-id
                           :message {:content {:text "Hey there"}}}]
      (swap! rf-db/app-db assoc-in [:activity-center :contact-requests] [contact-request])
      (is (match? contact-request
                  (rf/sub [sub-name contact-id])))))

  (testing "returns nil if it does not find a matching contact-id"
    (let [contact-id      "0x01"
          contact-request {:author  "0x02"
                           :message {:content {:text "Hey there"}}}]
      (swap! rf-db/app-db assoc-in [:activity-center :contact-requests] [contact-request])
      (is (match? nil
                  (rf/sub [sub-name contact-id]))))))

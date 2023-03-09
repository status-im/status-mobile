(ns status-im2.subs.communities-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            status-im2.subs.communities
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(def community-id "0x1")

(h/deftest-sub :communities
  [sub-name]
  (testing "returns raw communities"
    (let [raw-communities {"0x1" {:id "0x1"}}]
      (swap! rf-db/app-db assoc
        :communities
        raw-communities)
      (is (= raw-communities (rf/sub [sub-name]))))))

(h/deftest-sub :communities/section-list
  [sub-name]
  (testing "builds sections using the first community name char (uppercased)"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:name "civilized monkeys"}
       "0x2" {:name "Civilized rats"}})
    (is (= [{:title "C"
             :data  [{:name "civilized monkeys"}
                     {:name "Civilized rats"}]}]
           (rf/sub [sub-name]))))

  (testing "sorts by section ascending"
    (swap! rf-db/app-db assoc
      :communities
      {"0x3" {:name "Memorable"}
       "0x1" {:name "Civilized monkeys"}})
    (is (= [{:title "C" :data [{:name "Civilized monkeys"}]}
            {:title "M" :data [{:name "Memorable"}]}]
           (rf/sub [sub-name]))))

  (testing "builds default section for communities without a name"
    (swap! rf-db/app-db assoc
      :communities
      {"0x2" {:id "0x2"}
       "0x1" {:id "0x1"}})
    (is (= [{:title ""
             :data  [{:id "0x2"}
                     {:id "0x1"}]}]
           (rf/sub [sub-name])))))

(h/deftest-sub :communities/unviewed-counts
  [sub-name]
  (testing "sums counts for a particular community"
    (swap! rf-db/app-db assoc
      :chats
      {"0x100" {:community-id            community-id
                :unviewed-mentions-count 3
                :unviewed-messages-count 2}
       "0x101" {:community-id            "0x2"
                :unviewed-mentions-count 7
                :unviewed-messages-count 9}
       "0x102" {:community-id            community-id
                :unviewed-mentions-count 5
                :unviewed-messages-count 1}})
    (is (= {:unviewed-messages-count 3
            :unviewed-mentions-count 8}
           (rf/sub [sub-name community-id]))))

  (testing "defaults to zero when count keys are not present"
    (swap! rf-db/app-db assoc
      :chats
      {"0x100" {:community-id community-id}})
    (is (= {:unviewed-messages-count 0
            :unviewed-mentions-count 0}
           (rf/sub [sub-name community-id])))))

(h/deftest-sub :communities/sorted-communities
  [sub-name]
  (testing "Empty communities list"
    (swap! rf-db/app-db assoc
      :communities
      {})
    (is (= []
           (rf/sub [sub-name]))))
  (testing "communities sorted by name"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id "0x1" :name "Civilized monkeys"}
       "0x2" {:id "0x2" :name "Civilized rats"}
       "0x3" {:id "0x3" :name "Civilized dolphins"}})
    (is (= [{:id "0x3" :name "Civilized dolphins"}
            {:id "0x1" :name "Civilized monkeys"}
            {:id "0x2" :name "Civilized rats"}]
           (rf/sub [sub-name])))))

(h/deftest-sub :communities/categorized-channels
  [sub-name]
  (testing "Channels with categories"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1" {:id "0x1" :position 1 :name "chat1" :categoryID "1" :can-post? true}
                           "0x2" {:id "0x2" :position 2 :name "chat2" :categoryID "1" :can-post? false}
                           "0x3" {:id "0x3" :position 3 :name "chat3" :categoryID "2" :can-post? true}}
              :categories {"1" {:id       "1"
                                :position 2
                                :name     "category1"}
                           "2" {:id       "2"
                                :position 1
                                :name     "category2"}}
              :joined     true}})
    (is
     (= [["2"
          {:id         "2"
           :name       "category2"
           :collapsed? nil
           :position   1
           :chats      [{:name             "chat3"
                         :position         3
                         :emoji            nil
                         :locked?          false
                         :id               "0x3"
                         :unread-messages? false
                         :mentions-count   0}]}]
         ["1"
          {:id         "1"
           :name       "category1"
           :collapsed? nil
           :position   2
           :chats      [{:name             "chat1"
                         :emoji            nil
                         :position         1
                         :locked?          false
                         :id               "0x1"
                         :unread-messages? false
                         :mentions-count   0}
                        {:name             "chat2"
                         :emoji            nil
                         :position         2
                         :locked?          true
                         :id               "0x2"
                         :unread-messages? false
                         :mentions-count   0}]}]]
        (rf/sub [sub-name "0x1"]))))
  (testing "Channels without categories"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1" {:id "0x1" :position 1 :name "chat1" :categoryID "1" :can-post? true}
                           "0x2" {:id "0x2" :position 2 :name "chat2" :categoryID "1" :can-post? false}
                           "0x3" {:id "0x3" :position 3 :name "chat3" :can-post? true}}
              :categories {"1" {:id       "1"
                                :position 1
                                :name     "category1"}
                           "2" {:id       "2"
                                :position 2
                                :name     "category2"}}
              :joined     true}})
    (is
     (=
      [[constants/empty-category-id
        {:name       (i18n/label :t/none)
         :collapsed? nil
         :chats      [{:name             "chat3"
                       :emoji            nil
                       :position         3
                       :locked?          false
                       :id               "0x3"
                       :unread-messages? false
                       :mentions-count   0}]}]
       ["1"
        {:name       "category1"
         :id         "1"
         :position   1
         :collapsed? nil
         :chats      [{:name             "chat1"
                       :emoji            nil
                       :position         1
                       :locked?          false
                       :id               "0x1"
                       :unread-messages? false
                       :mentions-count   0}
                      {:name             "chat2"
                       :emoji            nil
                       :position         2
                       :locked?          true
                       :id               "0x2"
                       :unread-messages? false
                       :mentions-count   0}]}]]
      (rf/sub [sub-name "0x1"]))))
  (testing "Unread messages"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1" {:id "0x1" :position 1 :name "chat1" :categoryID "1" :can-post? true}
                           "0x2" {:id "0x2" :position 2 :name "chat2" :categoryID "1" :can-post? false}}
              :categories {"1" {:id "1" :name "category1"}}
              :joined     true}}
      :chats
      {"0x10x1" {:unviewed-messages-count 1 :unviewed-mentions-count 2}
       "0x10x2" {:unviewed-messages-count 0 :unviewed-mentions-count 0}})
    (is
     (= [["1"
          {:name       "category1"
           :id         "1"
           :collapsed? nil
           :chats      [{:name             "chat1"
                         :emoji            nil
                         :position         1
                         :locked?          false
                         :id               "0x1"
                         :unread-messages? true
                         :mentions-count   2}
                        {:name             "chat2"
                         :emoji            nil
                         :position         2
                         :locked?          true
                         :id               "0x2"
                         :unread-messages? false
                         :mentions-count   0}]}]]
        (rf/sub [sub-name "0x1"])))))

(h/deftest-sub :communities/my-pending-requests-to-join
  [sub-name]
  (testing "no requests"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {})
    (is (= {}
           (rf/sub [sub-name]))))
  (testing "users requests to join different communities"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {:community-id-1 {:id :request-id-1}
       :community-id-2 {:id :request-id-2}})
    (is (= {:community-id-1 {:id :request-id-1}
            :community-id-2 {:id :request-id-2}}
           (rf/sub [sub-name])))))

(h/deftest-sub :communities/my-pending-request-to-join
  [sub-name]
  (testing "no request for community"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {})
    (is (= nil
           (rf/sub [sub-name :community-id-1]))))
  (testing "users request to join a specific communities"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {:community-id-1 {:id :request-id-1}
       :community-id-2 {:id :request-id-2}})
    (is (= :request-id-1
           (rf/sub [sub-name :community-id-1])))))

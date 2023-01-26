(ns status-im2.subs.communities-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            status-im2.subs.communities
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {:communities/enabled? true})})

(def community-id "0x1")

(h/deftest-sub :communities
  [sub-name]
  (testing "returns empty vector if flag is disabled"
    (swap! rf-db/app-db assoc :communities/enabled? false)
    (is (= [] (rf/sub [sub-name]))))

  (testing "returns raw communities if flag is enabled"
    (let [raw-communities {"0x1" {:id "0x1"}}]
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :communities          raw-communities)
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
                :unviewed-replies-count  3
                :unviewed-messages-count 2}
       "0x101" {:community-id            "0x2"
                :unviewed-mentions-count 7
                :unviewed-replies-count  2
                :unviewed-messages-count 9}
       "0x102" {:community-id            community-id
                :unviewed-mentions-count 5
                :unviewed-replies-count  7
                :unviewed-messages-count 1}})
    (is (= {:unviewed-messages-count 3
            :unviewed-mentions-count 8
            :unviewed-replies-count  10}
           (rf/sub [sub-name community-id]))))

  (testing "defaults to zero when count keys are not present"
    (swap! rf-db/app-db assoc
      :chats
      {"0x100" {:community-id community-id}})
    (is (= {:unviewed-messages-count 0
            :unviewed-mentions-count 0
            :unviewed-replies-count  0}
           (rf/sub [sub-name community-id])))))

(h/deftest-sub :communities/community-ids-by-user-involvement
  [sub-name]
  (testing "Empty communities list"
    (swap! rf-db/app-db assoc
      :communities
      {})
    (is (= {:joined [] :pending [] :opened []}
           (rf/sub [sub-name]))))
  (testing "Only opened communities"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
      :communities
      {"0x1" {:id "0x1" :name "civilized monkeys"}
       "0x2" {:id "0x2" :name "Civilized rats"}
       "0x3" {:id "0x3" :name "Civilized dolphins"}})
    (is (= {:joined [] :pending [] :opened ["0x1" "0x2" "0x3"]}
           (rf/sub [sub-name]))))
  (testing "One joined community and two opened ones"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
      :communities
      {"0x1" {:id "0x1" :name "civilized monkeys" :joined true}
       "0x2" {:id "0x2" :name "Civilized rats"}
       "0x3" {:id "0x3" :name "Civilized dolphins"}})
    (is (= {:joined ["0x1"] :pending [] :opened ["0x2" "0x3"]}
           (rf/sub [sub-name]))))
  (testing "One joined community, one open and one pending"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
      :communities
      {"0x1" {:id "0x1" :name "civilized monkeys" :joined true}
       "0x2" {:id "0x2" :name "Civilized rats" :requested-to-join-at 1000}
       "0x3" {:id "0x3" :name "Civilized dolphins"}})
    (is (= {:joined ["0x1"] :pending ["0x2"] :opened ["0x3"]}
           (rf/sub [sub-name])))))

(h/deftest-sub :communities/sorted-communities
  [sub-name]
  (testing "Empty communities list"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
      :communities          {})
    (is (= []
           (rf/sub [sub-name]))))
  (testing "communities sorted by name"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
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
      :communities/enabled? true
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1" {:id "0x1" :name "chat1" :categoryID 1 :can-post? true}
                           "0x2" {:id "0x1" :name "chat2" :categoryID 1 :can-post? false}
                           "0x3" {:id "0x1" :name "chat3" :categoryID 2 :can-post? true}}
              :categories {1 {:id 1 :name "category1"}
                           2 {:id 2 :name "category2"}}
              :joined     true}})
    (is (= {:category1 [{:name "chat1" :emoji nil :locked? false :id "0x1"}
                        {:name "chat2" :emoji nil :locked? true :id "0x1"}]
            :category2 [{:name "chat3" :emoji nil :locked? false :id "0x1"}]}
           (rf/sub [sub-name "0x1"]))))
  (testing "Channels without categories"
    (swap! rf-db/app-db assoc
      :communities/enabled? true
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1" {:id "0x1" :name "chat1" :categoryID 1 :can-post? true}
                           "0x2" {:id "0x1" :name "chat2" :categoryID 1 :can-post? false}
                           "0x3" {:id "0x1" :name "chat3" :can-post? true}}
              :categories {1 {:id 1 :name "category1"}
                           2 {:id 2 :name "category2"}}
              :joined     true}})
    (is (= {:category1                     [{:name "chat1" :emoji nil :locked? false :id "0x1"}
                                            {:name "chat2" :emoji nil :locked? true :id "0x1"}]
            (keyword (i18n/label :t/none)) [{:name "chat3" :emoji nil :locked? false :id "0x1"}]}
           (rf/sub [sub-name "0x1"])))))

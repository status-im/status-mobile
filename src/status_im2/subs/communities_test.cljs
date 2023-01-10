(ns status-im2.subs.communities-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            status-im2.subs.communities
            [utils.re-frame :as rf]))

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

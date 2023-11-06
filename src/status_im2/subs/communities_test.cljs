(ns status-im2.subs.communities-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im2.constants :as constants]
    status-im2.subs.communities
    [test-helpers.unit :as h]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

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

(h/deftest-sub :communities/categorized-channels
  [sub-name]
  (testing "Channels with categories"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1"
                           {:id         "0x1"
                            :position   1
                            :name       "chat1"
                            :muted?     nil
                            :categoryID "1"
                            :can-post?  true}
                           "0x2"
                           {:id         "0x2"
                            :position   2
                            :name       "chat2"
                            :muted?     nil
                            :categoryID "1"
                            :can-post?  false}
                           "0x3"
                           {:id         "0x3"
                            :position   3
                            :name       "chat3"
                            :muted?     nil
                            :categoryID "2"
                            :can-post?  true}}
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
                         :muted?           nil
                         :locked?          nil
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
                         :muted?           nil
                         :locked?          nil
                         :id               "0x1"
                         :unread-messages? false
                         :mentions-count   0}
                        {:name             "chat2"
                         :emoji            nil
                         :position         2
                         :muted?           nil
                         :locked?          nil
                         :id               "0x2"
                         :unread-messages? false
                         :mentions-count   0}]}]]
        (rf/sub [sub-name "0x1"]))))
  (testing "Channels with categories and token permissions"
    (swap! rf-db/app-db assoc
      :communities
      {community-id {:id                community-id
                     :token-permissions [[:token-permission-id-01
                                          {:id "token-permission-id-01"
                                           :type constants/community-token-permission-can-view-channel
                                           :token_criteria [{:contract_addresses {:5 "0x0"}
                                                             :type               1
                                                             :symbol             "SNT"
                                                             :amount             "0.0020000000000000"
                                                             :decimals           18}]
                                           :chat_ids [(str community-id "0x100")]}]
                                         [:token-permission-id-02
                                          {:id "token-permission-id-02"
                                           :type constants/community-token-permission-become-member
                                           :token_criteria [{:contract_addresses {:5 "0x0"}
                                                             :type               1
                                                             :symbol             "ETH"
                                                             :amount             "0.0010000000000000"
                                                             :decimals           18}]}]
                                         [:token-permission-id-03
                                          {:id "token-permission-id-03"
                                           :type constants/community-token-permission-can-view-channel
                                           :token_criteria [{:contract_addresses {:5 "0x0"}
                                                             :type               1
                                                             :symbol             "ETH"
                                                             :amount             "0.1"
                                                             :decimals           18}]
                                           :chat_ids [(str community-id "0x300")]}]]
                     :chats             {"0x100" {:id         "0x100"
                                                  :position   1
                                                  :name       "chat1"
                                                  :muted?     nil
                                                  :categoryID "1"
                                                  :can-post?  false}
                                         "0x200" {:id         "0x200"
                                                  :position   2
                                                  :name       "chat2"
                                                  :muted?     nil
                                                  :categoryID "1"
                                                  :can-post?  false}
                                         "0x300" {:id         "0x300"
                                                  :position   3
                                                  :name       "chat3"
                                                  :muted?     nil
                                                  :categoryID "2"
                                                  :can-post?  true}}
                     :categories        {"1" {:id       "1"
                                              :position 2
                                              :name     "category1"}
                                         "2" {:id       "2"
                                              :position 1
                                              :name     "category2"}}
                     :joined            true}})
    (is
     (= [["2"
          {:id         "2"
           :name       "category2"
           :collapsed? nil
           :position   1
           :chats      [{:name             "chat3"
                         :position         3
                         :emoji            nil
                         :muted?           nil
                         :locked?          false
                         :id               "0x300"
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
                         :muted?           nil
                         :locked?          true
                         :id               "0x100"
                         :unread-messages? false
                         :mentions-count   0}
                        {:name             "chat2"
                         :emoji            nil
                         :position         2
                         :muted?           nil
                         :locked?          nil
                         :id               "0x200"
                         :unread-messages? false
                         :mentions-count   0}]}]]
        (rf/sub [sub-name "0x1"]))))
  (testing "Channels without categories"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1"
                           {:id         "0x1"
                            :position   1
                            :name       "chat1"
                            :categoryID "1"
                            :can-post?  true
                            :muted?     nil}
                           "0x2"
                           {:id         "0x2"
                            :position   2
                            :name       "chat2"
                            :categoryID "1"
                            :can-post?  false
                            :muted?     nil}
                           "0x3" {:id "0x3" :position 3 :name "chat3" :can-post? true :muted? nil}}
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
                       :locked?          nil
                       :muted?           nil
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
                       :locked?          nil
                       :muted?           nil
                       :id               "0x1"
                       :unread-messages? false
                       :mentions-count   0}
                      {:name             "chat2"
                       :emoji            nil
                       :position         2
                       :locked?          nil
                       :id               "0x2"
                       :muted?           nil
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
                         :locked?          nil
                         :id               "0x1"
                         :muted?           nil
                         :unread-messages? true
                         :mentions-count   2}
                        {:name             "chat2"
                         :emoji            nil
                         :position         2
                         :locked?          nil
                         :muted?           nil
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

(h/deftest-sub :community/token-gated-overview
  [sub-name]
  (let
    [checking-permissions? true
     token-image-eth "data:image/jpeg;base64,/9j/2w"
     community {:id                      community-id
                :checking-permissions?   checking-permissions?
                :permissions             {:access 3}
                :token-images            {"ETH" token-image-eth}
                :token-permissions       [[:permission-id-01
                                           {:id "permission-id-01"
                                            :type constants/community-token-permission-can-view-channel
                                            :token_criteria [{:contract_addresses {:5 "0x0"}
                                                              :type               1
                                                              :symbol             "SNT"
                                                              :amount             "0.002"
                                                              :decimals           18}]
                                            :chat_ids [(str community-id
                                                            "89f98a1e-6776-4e5f-8626-8ab9f855253f")]}]
                                          [:permission-id-02
                                           {:id "permission-id-02"
                                            :type constants/community-token-permission-become-admin
                                            :token_criteria [{:contract_addresses {:5 "0x0"}
                                                              :type               1
                                                              :symbol             "DAI"
                                                              :amount             "5.0"
                                                              :decimals           18}]
                                            :chat_ids [(str community-id
                                                            "89f98a1e-6776-4e5f-8626-8ab9f855253f")]}]
                                          [:permission-id-03
                                           {:id "permission-id-03"
                                            :type constants/community-token-permission-become-member
                                            :token_criteria [{:contract_addresses {:5 "0x0"}
                                                              :type               1
                                                              :symbol             "ETH"
                                                              :amount             "0.001"
                                                              :decimals           18}]}]]
                :name                    "Community super name"
                :chats                   {"89f98a1e-6776-4e5f-8626-8ab9f855253f"
                                          {:description "x"
                                           :emoji       "🎲"
                                           :permissions {:access 1}
                                           :color       "#88B0FF"
                                           :name        "random"
                                           :categoryID  "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                           :id          "89f98a1e-6776-4e5f-8626-8ab9f855253f"
                                           :position    4
                                           :can-post?   false
                                           :members     {"0x04" {"roles" [1]}}}
                                          "a076358e-4638-470e-a3fb-584d0a542ce6"
                                          {:description "General channel for the community"
                                           :emoji       "🐷 "
                                           :permissions {:access 1}
                                           :color       "#4360DF"
                                           :name        "general"
                                           :categoryID  "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                           :id          "a076358e-4638-470e-a3fb-584d0a542ce6"
                                           :position    0
                                           :can-post?   false
                                           :members     {"0x04" {"roles" [1]}}}}
                :token-permissions-check {:satisfied true
                                          :permissions
                                          {:a3dd5b6b-d93b-452c-b22a-09a8f42ec566 {:criteria [true false
                                                                                             true]}}
                                          :validCombinations
                                          [{:address  "0xd722eaa60dc73e334b588d34ba66a3b27e537783"
                                            :chainIds nil}
                                           {:address  "0x738d3146831c5871fa15872b409e8f360e341784"
                                            :chainIds [5 420]}]}
                :members                 {"0x04" {"roles" [1]}}
                :can-request-access?     false
                :outroMessage            "bla"
                :verified                false}]
    (swap! rf-db/app-db assoc-in [:communities community-id] community)
    (is (= {:can-request-access?   true
            :number-of-hold-tokens 2
            :tokens                [[{:symbol      "ETH"
                                      :amount      "0.001"
                                      :sufficient? nil
                                      :loading?    checking-permissions?
                                      :img-src     token-image-eth}]]}
           (rf/sub [sub-name community-id])))))

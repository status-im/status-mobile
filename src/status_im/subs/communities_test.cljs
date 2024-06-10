(ns status-im.subs.communities-test
  (:require
    [cljs.test :refer [is testing]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    [status-im.constants :as constants]
    status-im.subs.communities
    [status-im.subs.profile-test :as profile-test]
    [test-helpers.unit :as h]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def community-id "0x02b5bdaf5a25fcfe2ee14c501fab1836b8de57f61621080c3d52073d16de0d98d6")

(h/deftest-sub :communities
  [sub-name]
  (testing "returns raw communities"
    (let [raw-communities {"0x1" {:id "0x1"}}]
      (swap! rf-db/app-db assoc
        :communities
        raw-communities)
      (is (match? raw-communities (rf/sub [sub-name]))))))

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
    (is (match? {:unviewed-messages-count 3
                 :unviewed-mentions-count 8}
                (rf/sub [sub-name community-id]))))

  (testing "defaults to zero when count keys are not present"
    (swap! rf-db/app-db assoc
      :chats
      {"0x100" {:community-id community-id}})
    (is (match? {:unviewed-messages-count 0
                 :unviewed-mentions-count 0}
                (rf/sub [sub-name community-id])))))

(h/deftest-sub :communities/categorized-channels
  [sub-name]
  (testing "Channels with categories"
    (swap! rf-db/app-db assoc
      :communities
      {"0x1" {:id         "0x1"
              :chats      {"0x1"
                           {:id           "0x1"
                            :position     1
                            :name         "chat1"
                            :muted?       nil
                            :categoryID   "1"
                            :token-gated? false
                            :can-post?    true}
                           "0x2"
                           {:id           "0x2"
                            :position     2
                            :name         "chat2"
                            :muted?       nil
                            :categoryID   "1"
                            :token-gated? true
                            :can-post?    false}
                           "0x3"
                           {:id           "0x3"
                            :position     3
                            :name         "chat3"
                            :muted?       nil
                            :categoryID   "2"
                            :token-gated? true
                            :can-post?    true}}
              :categories {"1" {:id       "1"
                                :position 2
                                :name     "category1"}
                           "2" {:id       "2"
                                :position 1
                                :name     "category2"}}
              :joined     true}})
    (is
     (match? [["2"
               {:id         "2"
                :name       "category2"
                :collapsed? nil
                :position   1
                :chats      [{:name             "chat3"
                              :position         3
                              :emoji            nil
                              :muted?           nil
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
                              :muted?           nil
                              :locked?          nil
                              :id               "0x1"
                              :unread-messages? false
                              :mentions-count   0}
                             {:name             "chat2"
                              :emoji            nil
                              :position         2
                              :muted?           nil
                              :locked?          true
                              :id               "0x2"
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
     (match?
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
              :chats      {"0x1"
                           {:id "0x1" :position 1 :name "chat1" :categoryID "1" :can-post? true}
                           "0x2"
                           {:id "0x2" :position 2 :name "chat2" :categoryID "1" :can-post? false}}
              :categories {"1" {:id "1" :name "category1"}}
              :joined     true}}
      :chats
      {"0x10x1" {:unviewed-messages-count 1 :unviewed-mentions-count 2}
       "0x10x2" {:unviewed-messages-count 0 :unviewed-mentions-count 0}})
    (is
     (match? [["1"
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
    (is (match? {}
                (rf/sub [sub-name]))))
  (testing "users requests to join different communities"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {:community-id-1 {:id :request-id-1}
       :community-id-2 {:id :request-id-2}})
    (is (match? {:community-id-1 {:id :request-id-1}
                 :community-id-2 {:id :request-id-2}}
                (rf/sub [sub-name])))))

(h/deftest-sub :communities/my-pending-request-to-join
  [sub-name]
  (testing "no request for community"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {})
    (is (match? nil
                (rf/sub [sub-name :community-id-1]))))
  (testing "users request to join a specific communities"
    (swap! rf-db/app-db assoc
      :communities/my-pending-requests-to-join
      {:community-id-1 {:id :request-id-1}
       :community-id-2 {:id :request-id-2}})
    (is (match? :request-id-1
                (rf/sub [sub-name :community-id-1])))))

(h/deftest-sub :community/token-gated-overview
  [sub-name]
  (let
    [checking-permissions? true
     token-image-eth       "data:image/jpeg;base64,/9j/2w"
     checks                {:checking? checking-permissions?
                            :check
                            {:satisfied true
                             :highestRole
                             {:type     constants/community-token-permission-become-admin
                              :criteria [{:tokenRequirement [{:satisfied true
                                                              :criteria  {:contract_addresses
                                                                          {:5 "0x0"}
                                                                          :type 1
                                                                          :symbol "DAI"
                                                                          :amountInWei
                                                                          "5000000000000000000"
                                                                          :amount "5.0"
                                                                          :decimals 18}}]}
                                         {:tokenRequirement [{:satisfied false
                                                              :criteria  {:type        1
                                                                          :symbol      "ETH"
                                                                          :amountInWei "2000000000000000"
                                                                          :amount      "0.002"
                                                                          :decimals    18}}]}]}

                             :permissions
                             {:a3dd5b6b-d93b-452c-b22a-09a8f42ec566 {:criteria [true false
                                                                                true]}}
                             :validCombinations
                             [{:address  "0xd722eaa60dc73e334b588d34ba66a3b27e537783"
                               :chainIds nil}
                              {:address  "0x738d3146831c5871fa15872b409e8f360e341784"
                               :chainIds [5 420]}]}}
     community             {:id                      community-id
                            :checking-permissions?   checking-permissions?
                            :permissions             {:access 3}
                            :highest-permission-role constants/community-token-permission-become-admin
                            :token-images            {"ETH" token-image-eth}
                            :name                    "Community super name"
                            :chats                   {"89f98a1e-6776-4e5f-8626-8ab9f855253f"
                                                      {:description "x"
                                                       :emoji "🎲"
                                                       :permissions {:access 1}
                                                       :color "#88B0FF"
                                                       :name "random"
                                                       :categoryID "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                                       :id "89f98a1e-6776-4e5f-8626-8ab9f855253f"
                                                       :position 4
                                                       :can-post? false
                                                       :members {"0x04" {"roles" [1]}}}
                                                      "a076358e-4638-470e-a3fb-584d0a542ce6"
                                                      {:description "General channel for the community"
                                                       :emoji "🐷 "
                                                       :permissions {:access 1}
                                                       :color "#4360DF"
                                                       :name "general"
                                                       :categoryID "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                                       :id "a076358e-4638-470e-a3fb-584d0a542ce6"
                                                       :position 0
                                                       :can-post? false
                                                       :members {"0x04" {"roles" [1]}}}}
                            :members                 {"0x04" {"roles" [1]}}
                            :can-request-access?     false
                            :outro-message           "bla"
                            :verified                false}]
    (swap! rf-db/app-db assoc-in [:communities community-id] community)
    (swap! rf-db/app-db assoc-in [:communities/permissions-check community-id] checks)
    (is (match? {:can-request-access?     true
                 :networks-not-supported? nil
                 :tokens                  [[{:symbol       "DAI"
                                             :amount       "5"
                                             :collectible? false
                                             :sufficient?  true
                                             :loading?     checking-permissions?}]
                                           [{:symbol       "ETH"
                                             :amount       "0.002"
                                             :collectible? false
                                             :sufficient?  false
                                             :loading?     checking-permissions?
                                             :img-src      token-image-eth}]]}
                (rf/sub [sub-name community-id])))))

(h/deftest-sub :communities/community-color
  [sub-name]
  (testing "returns the community color"
    (let [community-color "#FEFEFE"]
      (swap! rf-db/app-db assoc
        :communities
        {community-id {:color community-color}})
      (is (match? community-color (rf/sub [sub-name community-id]))))))

(h/deftest-sub :communities/token-images-by-symbol
  [sub-name]
  (testing
    "returns a map keyed by the images of tokens/collectibles
            And has data-uri as it's values"
    (swap! rf-db/app-db assoc-in
      [:communities community-id :tokens-metadata]
      [{:contract-addresses {:420 "0x1"}
        :image              "data:image/jpeg;base64,/9j/2wCEAAYEBQYFBAYGBQYH"
        :tokenType          2
        :symbol             "DOGE"
        :name               "Doge Coin coll"}
       {:contract-addresses {:420 "0x1"}
        :image              "data:image/jpeg;base64,/9j/2wCEAAYEBQYFBAYGBQYH"
        :tokenType          2
        :symbol             "BTC"
        :name               "Bitcoin coll"}])
    (is (match? {"DOGE" "data:image/jpeg;base64,/9j/2wCEAAYEBQYFBAYGBQYH"
                 "BTC"  "data:image/jpeg;base64,/9j/2wCEAAYEBQYFBAYGBQYH"}
                (rf/sub [sub-name community-id])))))

(h/deftest-sub :community/token-permissions
  [sub-name]
  (testing
    "with visible permissions"
    (let
      [checking? false
       token-image-eth "data:image/jpeg;base64,/9j/2w"
       checks
       {:checking? checking?
        :check
        {:roles
         [{:type      1
           :satisfied true
           :criteria  [{:roles            1
                        :tokenRequirement [{:satisfied true
                                            :criteria  {:contract_addresses
                                                        {:421614
                                                         0x0000000000000000000000000000000000000000
                                                         :11155111
                                                         0x0000000000000000000000000000000000000000
                                                         :11155420
                                                         0x0000000000000000000000000000000000000000}
                                                        :type 1
                                                        :symbol "ETH"
                                                        :amount 1
                                                        :decimals 18
                                                        :amountInWei 1000000000000000000}}]
                        :criteria         [true]}]}
          {:type      2
           :satisfied false
           :criteria  [{:roles            2
                        :tokenRequirement [{:satisfied false
                                            :criteria  {:contract_addresses
                                                        {:11155111
                                                         0x3e622317f8c93f7328350cf0b56d9ed4c620c5d6}
                                                        :type 1
                                                        :symbol "DAI"
                                                        :amount 10
                                                        :decimals 18
                                                        :amountInWei 10000000000000000000}}]
                        :criteria         [false]}]}]}
       }
       community {:id                    community-id
                  :checking-permissions? checking?
                  :token-images          {"ETH" token-image-eth}
                  :name                  "Community super name"
                  :can-request-access?   false
                  :outro-message         "bla"
                  :verified              false}]
      (swap! rf-db/app-db assoc-in [:communities community-id] community)
      (swap! rf-db/app-db assoc-in [:communities/permissions-check-all community-id] checks)
      (is
       (match? [{:role 1
                 :satisfied? true
                 :tokens
                 [[{:symbol       "ETH"
                    :sufficient?  true
                    :loading?     false
                    :collectible? false
                    :amount       "1"
                    :img-src      token-image-eth}]]}
                {:role       2
                 :satisfied? false
                 :tokens     [[{:symbol       "DAI"
                                :sufficient?  false
                                :collectible? false
                                :loading?     false
                                :amount       "10"
                                :img-src      nil}]]}]
               (rf/sub [sub-name community-id])))))
  (testing
    "without any visible permissions"
    (let
      [checking? false
       token-image-eth "data:image/jpeg;base64,/9j/2w"
       checks
       {:checking? checking?
        :check
        {:roles []}
       }
       community {:id                    community-id
                  :checking-permissions? checking?
                  :token-images          {"ETH" token-image-eth}
                  :name                  "Community super name"
                  :can-request-access?   false
                  :outro-message         "bla"
                  :verified              false}]
      (swap! rf-db/app-db assoc-in [:communities community-id] community)
      (swap! rf-db/app-db assoc-in [:communities/permissions-check-all community-id] checks)
      (is
       (match? []
               (rf/sub [sub-name community-id]))))))

(h/deftest-sub :communities/sorted-community-members-section-list
  [sub-name]
  (testing "returns sorted community members per online status"
    (let [token-image-eth "data:image/jpeg;base64,/9j/2w"
          channel-id-1    "89f98a1e-6776-4e5f-8626-8ab9f855253f"
          channel-id-2    "a076358e-4638-470e-a3fb-584d0a542ce6"
          chat-id-1       (str community-id channel-id-1)
          chat-id-2       (str community-id channel-id-2)
          community       {:id                  community-id
                           :permissions         {:access 3}
                           :token-images        {"ETH" token-image-eth}
                           :name                "Community super name"
                           :chats               {channel-id-1
                                                 {:description "x"
                                                  :emoji       "🎲"
                                                  :permissions {:access 1}
                                                  :color       "#88B0FF"
                                                  :name        "random"
                                                  :categoryID  "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                                  :id          channel-id-1
                                                  :position    4
                                                  :can-post?   false
                                                  :members     nil}
                                                 channel-id-2
                                                 {:description  "General channel for the community"
                                                  :emoji        "🥔"
                                                  :permissions  {:access 1}
                                                  :color        "#4360DF"
                                                  :name         "general"
                                                  :categoryID   "0c3c64e7-d56e-439b-a3fb-a946d83cb056"
                                                  :id           channel-id-2
                                                  :position     0
                                                  :token-gated? true
                                                  :can-post?    false
                                                  :members      {"0x01" {"roles" [1]}
                                                                 "0x02" {"roles" [1]}
                                                                 "0x05" {"roles" [1]}}}}
                           :members             {"0x01" {"roles" [1]}
                                                 "0x02" {"roles" [1]}
                                                 "0x03" {"roles" [1]}
                                                 "0x04" {"roles" [1]}}
                           :can-request-access? false
                           :outroMessage        "bla"
                           :verified            false}]
      (swap! rf-db/app-db assoc-in [:communities community-id] community)
      (swap! rf-db/app-db assoc :profile/profile profile-test/sample-profile)
      (swap! rf-db/app-db assoc
        :visibility-status-updates
        {"0x01" {:status-type constants/visibility-status-always-online}
         "0x02" {:status-type constants/visibility-status-always-online}})
      (testing "a non-token gated community should look at all members of a community"
        (is (= [{:title (i18n/label :t/online)
                 :data  ["0x01" "0x02"]}
                {:title (i18n/label :t/offline)
                 :data  ["0x03" "0x04"]}]
               (rf/sub [sub-name community-id chat-id-1]))))
      (testing "a token gated community should use the members option in the channel"
        (is (= [{:title (i18n/label :t/online)
                 :data  ["0x01" "0x02"]}
                {:title (i18n/label :t/offline)
                 :data  ["0x05"]}]
               (rf/sub [sub-name community-id chat-id-2])))))))

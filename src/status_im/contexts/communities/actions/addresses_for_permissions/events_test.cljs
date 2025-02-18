(ns status-im.contexts.communities.actions.addresses-for-permissions.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.contexts.communities.actions.addresses-for-permissions.events :as sut]))

(def community-id "0x1")

(deftest get-permissioned-balances-test
  (let [cofx {:db {}}]
    (is (match? {:fx [[:json-rpc/call
                       [{:method     :wakuext_getCommunityPermissionedBalances
                         :params     [{:communityId community-id}]
                         :on-success [:communities/get-permissioned-balances-success community-id]
                         :on-error   fn?}]]]}
                (sut/get-permissioned-balances cofx [community-id])))))

(deftest set-permissioned-accounts-test
  (testing "new accounts contain the current airdrop address"
    (let [cofx                         {:db {:communities/all-addresses-to-reveal
                                             {community-id #{"0xA" "0xB"}}}}
          addresses-to-reveal          ["0xA"]
          expected-addresses-to-reveal (set addresses-to-reveal)]
      (is (match?
           {:db {:communities/all-addresses-to-reveal
                 {community-id expected-addresses-to-reveal}}
            :fx [[:dispatch
                  [:communities/check-permissions-to-join-community
                   community-id expected-addresses-to-reveal :based-on-client-selection]]
                 [:dispatch [:hide-bottom-sheet]]]}
           (sut/set-permissioned-accounts cofx [community-id addresses-to-reveal])))))

  (testing "new accounts do not contain the current airdrop address"
    (let [cofx
          {:db {:communities/all-addresses-to-reveal {community-id #{"0xA" "0xB" "0xC"}}
                :communities/all-airdrop-addresses   {community-id "0xB"}
                :wallet                              {:accounts {"0xB" {:address   "0xB"
                                                                        :operable? true
                                                                        :position  0}
                                                                 "0xA" {:address   "0xA"
                                                                        :operable? true
                                                                        :position  1}
                                                                 "0xC" {:address   "0xC"
                                                                        :operable? true
                                                                        :position  2}}}}}
          addresses-to-reveal ["0xA" "0xC"]]
      (is (match?
           {:db {:communities/all-addresses-to-reveal
                 {community-id (set addresses-to-reveal)}
                 :communities/all-airdrop-addresses
                 {community-id "0xA"}}
            :fx [[:dispatch
                  [:communities/check-permissions-to-join-community
                   community-id (set addresses-to-reveal) :based-on-client-selection]]
                 [:dispatch [:hide-bottom-sheet]]]}
           (sut/set-permissioned-accounts cofx [community-id addresses-to-reveal]))))))

(deftest set-share-all-addresses-test
  (testing "sets flag from false -> true will mark all addresses to be revealed"
    (let [cofx                {:db
                               {:wallet
                                {:accounts {"0xB" {:address "0xB" :operable? true :position 0}
                                            "0xA" {:address "0xA" :operable? true :position 1}
                                            "0xC" {:address "0xC" :operable? true :position 2}}}
                                :communities/all-addresses-to-reveal {community-id #{"0xA"}}
                                :communities/selected-share-all-addresses {community-id false}}}
          addresses-to-reveal #{"0xA" "0xB" "0xC"}]
      (is (match?
           {:db {:communities/selected-share-all-addresses {community-id true}
                 :communities/all-addresses-to-reveal
                 {community-id addresses-to-reveal}}
            :fx [[:dispatch
                  [:communities/check-permissions-to-join-during-selection
                   community-id addresses-to-reveal]]]}
           (sut/set-share-all-addresses cofx [community-id true])))))

  (testing "sets flag from true -> false will not change addresses to be revealed"
    (let [cofx {:db {:communities/all-addresses-to-reveal {community-id #{"0xA"}}}}]
      (is (match?
           {:db {:communities/selected-share-all-addresses {community-id false}
                 :communities/all-addresses-to-reveal      {community-id #{"0xA"}}}
            :fx [nil]}
           (sut/set-share-all-addresses cofx [community-id false]))))))

(deftest check-permissions-to-join-for-selection-test
  (testing "when no addresses are passed don't check permissions"
    (let [addresses []
          cofx      {:db {:foo :bar
                          :communities/permissions-checks-for-selection
                          {"0xC"        :another-check
                           community-id :some-check}}}]
      (is (match?
           {:db {:foo                                          :bar
                 :communities/permissions-checks-for-selection {"0xC" :another-check}}}
           (sut/check-permissions-to-join-for-selection cofx [community-id addresses])))))

  (testing "when there are addresses to check permissions and not currently checking"
    (let [addresses ["0xA"]
          cofx      {:db {:communities/permissions-checks-for-selection
                          {"other-comm-id" {}
                           community-id    {:checking? false}}}}]
      (is (match?
           {:db {:communities/permissions-checks-for-selection
                 {"other-comm-id" {}
                  community-id    {:checking? true}}}
            :fx [[:json-rpc/call
                  [{:method     :wakuext_checkPermissionsToJoinCommunity
                    :params     [{:communityId community-id :addresses addresses}]
                    :on-success [:communities/check-permissions-to-join-during-selection-success
                                 community-id]
                    :on-error   [:communities/check-permissions-to-join-during-selection-failure
                                 community-id addresses]}]]]}
           (sut/check-permissions-to-join-for-selection cofx [community-id addresses]))))))

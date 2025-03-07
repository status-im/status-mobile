(ns status-im.contexts.communities.actions.accounts-selection.events-test
  (:require [cljs.test :refer [deftest is testing]]
            matcher-combinators.test
            [status-im.contexts.communities.actions.accounts-selection.events :as sut]))

(def community-id "0x99")
(def password "password")

(def wallet-accounts
  {"0xA" {:address     "0xA"
          :watch-only? true
          :operable?   true
          :position    2
          :color       :red
          :emoji       "🦇"}
   "0xB" {:address   "0xB"
          :operable? true
          :position  0
          :color     :blue
          :emoji     "🐈"}
   "0xC" {:address   "0xC"
          :operable? true
          :position  1
          :color     :orange
          :emoji     "🛏️"}
   "0xD" {:address   "0xD"
          :operable? false
          :position  3
          :color     :pink
          :emoji     "🦩"}})

(deftest initialize-permission-addresses-test
  (testing "fetches revealed accounts when joined"
    (let [cofx {:db {:communities {community-id {:joined true}}}}]
      (is (match?
           {:fx [[:dispatch
                  [:communities/get-revealed-accounts community-id
                   [:communities/do-init-permission-addresses community-id]]]]}
           (sut/initialize-permission-addresses cofx [community-id])))))

  (testing "does not fetch revealed accounts when not joined"
    (let [cofx {:db {:communities                             {community-id {:joined false}}
                     :communities/my-pending-requests-to-join {}}}]
      (is (match?
           {:fx [[:dispatch [:communities/do-init-permission-addresses community-id]]]}
           (sut/initialize-permission-addresses cofx [community-id]))))))

(deftest do-init-permission-addresses-test
  (testing "uses already revealed accounts to initialize shareable accounts"
    (let [cofx                {:db {:wallet {:accounts wallet-accounts}}}
          revealed-accounts   {"0xC" {:address          "0xC"
                                      :airdrop-address? true
                                      :position         1}}
          airdrop-address     "0xC"
          addresses-to-reveal #{"0xC"}]
      (is
       (match?
        {:db (-> (:db cofx)
                 (assoc-in [:communities/selected-share-all-addresses community-id] true)
                 (assoc-in [:communities/all-addresses-to-reveal community-id] addresses-to-reveal)
                 (assoc-in [:communities/all-airdrop-addresses community-id] airdrop-address))
         :fx [[:dispatch
               [:communities/check-permissions-to-join-community
                community-id addresses-to-reveal :based-on-client-selection]]
              ;; Pre-fetch permissions check so that when first opening the
              ;; Addresses for Permissions screen the highest permission role is
              ;; already available and no incorrect information flashes on screen.
              [:dispatch
               [:communities/check-permissions-to-join-during-selection community-id
                addresses-to-reveal]]]}
        (sut/do-init-permission-addresses cofx [community-id revealed-accounts true])))))

  ;; Expect to mark all addresses to be revealed and first one to receive
  ;; airdrops when no addresses were previously revealed.
  (testing "handles case where there are no previously revealed addresses"
    (let [cofx                {:db {:wallet {:accounts wallet-accounts}}}
          addresses-to-reveal #{"0xB" "0xC"}
          revealed-accounts   []]
      (is
       (match?
        {:db (-> (:db cofx)
                 (assoc-in [:communities/selected-share-all-addresses community-id] true)
                 (assoc-in [:communities/all-addresses-to-reveal community-id] addresses-to-reveal))
         :fx [[:dispatch
               [:communities/check-permissions-to-join-community
                community-id addresses-to-reveal :based-on-client-selection]]
              [:dispatch
               [:communities/check-permissions-to-join-during-selection community-id
                addresses-to-reveal]]]}
        (sut/do-init-permission-addresses cofx [community-id revealed-accounts true]))))))

(deftest sign-shared-addresses-test
  (testing
    "when addresses to reveal is not passed, fallback to all wallet addresses"
    (let [pub-key                 "abcdef"
          revealed-addresses      #{"0xB" "0xC"}
          share-future-addresses? true
          cofx                    {:db {:profile/profile                     {:public-key pub-key}
                                        :communities/all-addresses-to-reveal {community-id
                                                                              revealed-addresses}}}
          airdrop-address         "0xB"
          actual                  (sut/sign-shared-addresses
                                   cofx
                                   [{:community-id            community-id
                                     :airdrop-address         airdrop-address
                                     :share-future-addresses? share-future-addresses?
                                     :on-success              identity}])]
      (is (match?
           {:fx [[:effects.community/generate-requests-for-signing
                  {:community-id        community-id
                   :pub-key             pub-key
                   :addresses-to-reveal revealed-addresses
                   :on-success          fn?
                   :on-error            fn?}]]}
           actual)))))

(deftest edit-shared-addresses-test
  (testing
    "signatures and addresses should be derived from signature-data and if airdrop address is not passed,
     should fallback to first operable wallet address."
    (let [pub-key "abcdef"
          cofx    {:db {:profile/profile                     {:public-key pub-key}
                        :wallet                              {:accounts wallet-accounts}
                        ;; 0xA is watch-only
                        :communities/all-addresses-to-reveal {community-id #{"0xA" "0xB" "0xC"}}}}

          actual  (sut/edit-shared-addresses
                   cofx
                   [{:community-id            community-id
                     :share-future-addresses? true
                     :signature-data          [{:address   "0xB"
                                                :signature "sigB"
                                                :message   "mesB"}
                                               {:address   "0xC"
                                                :signature "sigC"
                                                :message   "mesC"}]
                     :on-success              identity}])]
      (is (match?
           {:fx [[:effects.community/edit-shared-addresses
                  {:community-id            community-id
                   :signatures              '("0xsigB" "0xsigC")
                   :pub-key                 pub-key
                   :addresses-to-reveal     #{"0xB" "0xC"}
                   :share-future-addresses? true
                   :airdrop-address         "0xB"
                   :on-success              fn?
                   :on-error                fn?}]]}
           actual))))

  (testing "if share-future-addresses flag is not passed, should use the db value"
    (let [pub-key "abcdef"
          cofx    {:db {:profile/profile                          {:public-key pub-key}
                        :wallet                                   {:accounts wallet-accounts}
                        :communities/selected-share-all-addresses {community-id false}
                        :communities/all-addresses-to-reveal      {community-id #{"0xA" "0xB"}}}}

          actual  (sut/edit-shared-addresses
                   cofx
                   [{:community-id    community-id
                     :airdrop-address "0xB"
                     :signature-data  [{:address   "0xA"
                                        :signature "sigA"
                                        :message   "mesA"}
                                       {:address   "0xB"
                                        :signature "sigB"
                                        :message   "mesB"}]
                     :on-success      identity}])]
      (is (match?
           {:fx [[:effects.community/edit-shared-addresses
                  {:community-id            community-id
                   :signatures              '("0xsigA" "0xsigB")
                   :pub-key                 pub-key
                   :addresses-to-reveal     #{"0xA" "0xB"}
                   :share-future-addresses? false
                   :airdrop-address         "0xB"
                   :on-success              fn?
                   :on-error                fn?}]]}
           actual)))))

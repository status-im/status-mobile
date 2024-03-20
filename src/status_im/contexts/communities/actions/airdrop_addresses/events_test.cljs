(ns status-im.contexts.communities.actions.airdrop-addresses.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.contexts.communities.actions.airdrop-addresses.events :as sut]))

(def community-id "community-id")

(deftest set-airdrop-address-test
  (testing "updates all reveal? flags"
    (let [new-airdrop-address "0xA"
          cofx                {:db {:communities/all-airdrop-addresses
                                    {"another-community" "0xF"
                                     community-id        "0xB"}}}]
      (is (match?
           (assoc-in cofx
            [:db :communities/all-airdrop-addresses community-id]
            new-airdrop-address)
           (sut/set-airdrop-address cofx [community-id new-airdrop-address]))))))

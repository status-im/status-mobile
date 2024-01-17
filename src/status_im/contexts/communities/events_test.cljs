(ns status-im.contexts.communities.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.communities.events :as events]))

(deftest initialize-permission-addresses-test
  (let [initial-db  {:db {:wallet {:accounts {"0x1" {:address  "0x1"
                                                     :position 0}
                                              "0x2" {:address  "0x2"
                                                     :position 1}}}}}
        expected-db {:db (assoc (:db initial-db)
                                :communities/previous-permission-addresses #{"0x1" "0x2"}
                                :communities/selected-permission-addresses #{"0x1" "0x2"}
                                :communities/airdrop-address               "0x1")}]
    (is (match? expected-db (events/initialize-permission-addresses initial-db)))))

(deftest toggle-selected-permission-address-test
  (let [initial-db {:db {:communities/selected-permission-addresses #{"0x1" "0x2"}}}]
    (is (match? {:db {:communities/selected-permission-addresses #{"0x1"}}}
                (events/toggle-selected-permission-address initial-db ["0x2"])))
    (is (match? {:db {:communities/selected-permission-addresses #{"0x1" "0x2" "0x3"}}}
                (events/toggle-selected-permission-address initial-db ["0x3"])))))

(deftest update-previous-permission-addresses-test
  (let [wallet {:accounts {"0x1" {:address  "0x1"
                                  :position 0}
                           "0x2" {:address  "0x2"
                                  :position 1}
                           "0x3" {:address  "0x3"
                                  :position 2}}}]
    (let [initial-db  {:db {:wallet                                    wallet
                            :communities/previous-permission-addresses #{"0x1" "0x2"}
                            :communities/selected-permission-addresses #{"0x1" "0x2" "0x3"}
                            :communities/airdrop-address               "0x1"}}
          expected-db {:db {:wallet                                    wallet
                            :communities/previous-permission-addresses #{"0x1" "0x2" "0x3"}
                            :communities/selected-permission-addresses #{"0x1" "0x2" "0x3"}
                            :communities/airdrop-address               "0x1"}}]
      (is
       (match? expected-db
               (events/update-previous-permission-addresses initial-db))))
    (let [initial-db  {:db {:wallet                                    wallet
                            :communities/previous-permission-addresses #{"0x1" "0x2"}
                            :communities/selected-permission-addresses #{"0x2" "0x3"}
                            :communities/airdrop-address               "0x1"}}
          expected-db {:db {:wallet                                    wallet
                            :communities/previous-permission-addresses #{"0x2" "0x3"}
                            :communities/selected-permission-addresses #{"0x2" "0x3"}
                            :communities/airdrop-address               "0x2"}}]
      (is
       (match? expected-db
               (events/update-previous-permission-addresses initial-db))))))

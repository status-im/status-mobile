(ns status-im.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.events :as collectible-events]
    [status-im.contexts.wallet.db :as db]
    [status-im.contexts.wallet.events :as events]))

(def address "0x2f88d65f3cb52605a54a833ae118fb1363acccd2")

(deftest scan-address-success-test
  (let [db {}]
    (testing "scan-address-success"
      (let [expected-db {:wallet {:ui {:scanned-address address}}}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest clean-scanned-address-test
  (let [db {:wallet {:ui {:scanned-address address}}}]
    (testing "clean-scanned-address"
      (let [expected-db {:wallet {:ui {:send            nil
                                       :scanned-address nil}}}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest store-collectibles-test
  (testing "flush-collectibles"
    (let [collectible-1 {:collectible-data {:image-url "https://..." :animation-url "https://..."}
                         :ownership        [{:address "0x1"
                                             :balance "1"}]}
          collectible-2 {:collectible-data {:image-url "" :animation-url "https://..."}
                         :ownership        [{:address "0x1"
                                             :balance "1"}]}
          collectible-3 {:collectible-data {:image-url "" :animation-url nil}
                         :ownership        [{:address "0x2"
                                             :balance "1"}]}
          db            {:wallet {:ui       {:collectibles {:pending-requests 0
                                                            :fetched          {"0x1" [collectible-1
                                                                                      collectible-2]
                                                                               "0x2" [collectible-3]}}}
                                  :accounts {"0x1" {}
                                             "0x3" {}}}}
          expected-db   {:wallet {:ui       {:collectibles {}}
                                  :accounts {"0x1" {:collectibles (list collectible-1 collectible-2)}
                                             "0x2" {:collectibles (list collectible-3)}
                                             "0x3" {}}}}
          result-db     (:db (collectible-events/flush-collectibles {:db db}))]

      (is (match? result-db expected-db)))))

(deftest clear-stored-collectibles-test
  (let [db {:wallet {:accounts {"0x1" {:collectibles [{:id 1} {:id 2}]}
                                "0x2" {"some other stuff" "with any value"
                                       :collectibles      [{:id 3}]}
                                "0x3" {}}}}]
    (testing "clear-stored-collectibles"
      (let [expected-db {:wallet {:accounts {"0x1" {}
                                             "0x2" {"some other stuff" "with any value"}
                                             "0x3" {}}}}
            effects     (collectible-events/clear-stored-collectibles {:db db})
            result-db   (:db effects)]

        (is (match? result-db expected-db))))))

(deftest store-last-collectible-details-test
  (testing "store-last-collectible-details"
    (let [db               {:wallet {}}
          last-collectible {:description "Pandaria"
                            :image-url   "https://..."}
          expected-db      {:wallet {:last-collectible-details {:description "Pandaria"
                                                                :image-url   "https://..."}}}
          effects          (collectible-events/store-last-collectible-details {:db db}
                                                                              [last-collectible])
          result-db        (:db effects)]
      (is (match? result-db expected-db)))))

(deftest reset-selected-networks-test
  (testing "reset-selected-networks"
    (let [db          {:wallet {}}
          expected-db {:wallet db/defaults}
          effects     (events/reset-selected-networks {:db db})
          result-db   (:db effects)]
      (is (match? result-db expected-db)))))

(deftest update-selected-networks-test
  (testing "update-selected-networks"
    (let [db           {:wallet {:ui {:network-filter {:selected-networks
                                                       #{constants/optimism-network-name}
                                                       :selector-state :changed}}}}
          network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks
                                                       #{constants/optimism-network-name
                                                         network-name}
                                                       :selector-state :changed}}}}
          props        [network-name]
          effects      (events/update-selected-networks {:db db} props)
          result-db    (:db effects)]
      (is (match? result-db expected-db))))

  (testing "update-selected-networks > if all networks is already selected, update to incoming network"
    (let [db           {:wallet db/defaults}
          network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks #{network-name}
                                                       :selector-state    :changed}}}}
          props        [network-name]
          effects      (events/update-selected-networks {:db db} props)
          result-db    (:db effects)]
      (is (match? result-db expected-db))))

  (testing "update-selected-networks > reset on removing last network"
    (let [db          {:wallet {:ui {:network-filter {:selected-networks
                                                      #{constants/optimism-network-name}
                                                      :selector-state :changed}}}}
          expected-fx [[:dispatch [:wallet/reset-selected-networks]]]
          props       [constants/optimism-network-name]
          effects     (events/update-selected-networks {:db db} props)
          result-fx   (:fx effects)]
      (is (match? result-fx expected-fx)))))

(ns status-im.contexts.wallet.collectible.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.wallet.collectible.events :as events]))

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
          result-db     (:db (events/flush-collectibles {:db db}))]

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
            effects     (events/clear-stored-collectibles {:db db})
            result-db   (:db effects)]

        (is (match? result-db expected-db))))))

(deftest request-new-collectibles-for-account-from-signal-test
  (testing "request new collectibles for account from signal"
    (let [db       {:wallet {}}
          address  "0x1"
          expected {:db {:wallet {:ui {:collectibles {:pending-requests 1}}}}
                    :fx [[:dispatch
                          [:wallet/request-new-collectibles-for-account
                           {:request-id 0
                            :account    address
                            :amount     events/collectibles-request-batch-size}]]]}
          effects  (events/request-new-collectibles-for-account-from-signal {:db db}
                                                                            [address])]
      (is (match? expected effects)))))

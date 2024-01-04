(ns status-im.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.wallet.events :as events]
    [status-im.contexts.wallet.events.collectibles :as collectibles]))

(def address "0x2f88d65f3cb52605a54a833ae118fb1363acccd2")

(deftest scan-address-success
  (let [db {}]
    (testing "scan-address-success"
      (let [expected-db {:wallet/scanned-address address}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest clean-scanned-address
  (let [db {:wallet/scanned-address address}]
    (testing "clean-scanned-address"
      (let [expected-db {:wallet {:ui {:send nil}}}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest store-collectibles
  (testing "(displayable-collectible?) helper function"
    (let [expected-results [[true
                             {:collectible-data {:image-url "https://..." :animation-url "https://..."}}]
                            [true {:collectible-data {:image-url "" :animation-url "https://..."}}]
                            [true {:collectible-data {:image-url nil :animation-url "https://..."}}]
                            [true {:collectible-data {:image-url "https://..." :animation-url ""}}]
                            [true {:collectible-data {:image-url "https://..." :animation-url nil}}]
                            [false {:collectible-data {:image-url "" :animation-url nil}}]
                            [false {:collectible-data {:image-url nil :animation-url nil}}]
                            [false {:collectible-data {:image-url nil :animation-url ""}}]
                            [false {:collectible-data {:image-url "" :animation-url ""}}]]]
      (doseq [[result collection] expected-results]
        (is (match? result (collectibles/displayable-collectible? collection))))))

  (testing "save-collectibles-request-details"
    (let [db            {:wallet {:accounts {"0x1" {}
                                             "0x3" {}}}}
          collectible-1 {:collectible-data {:image-url "https://..." :animation-url "https://..."}
                         :ownership        [{:address "0x1"
                                             :balance "1"}]}
          collectible-2 {:collectible-data {:image-url "" :animation-url "https://..."}
                         :ownership        [{:address "0x1"
                                             :balance "1"}]}
          collectible-3 {:collectible-data {:image-url "" :animation-url nil}
                         :ownership        [{:address "0x2"
                                             :balance "1"}]}
          collectibles  [collectible-1 collectible-2 collectible-3]
          expected-db   {:wallet {:accounts {"0x1" {:collectibles (list collectible-2 collectible-1)}
                                             "0x2" {:collectibles (list collectible-3)}
                                             "0x3" {}}}}
          effects       (collectibles/store-collectibles {:db db} [collectibles])
          result-db     (:db effects)]
      (is (match? result-db expected-db)))))

(deftest clear-stored-collectibles
  (let [db {:wallet {:accounts {"0x1" {:collectibles [{:id 1} {:id 2}]}
                                "0x2" {"some other stuff" "with any value"
                                       :collectibles      [{:id 3}]}
                                "0x3" {}}}}]
    (testing "clear-stored-collectibles"
      (let [expected-db {:wallet {:accounts {"0x1" {}
                                             "0x2" {"some other stuff" "with any value"}
                                             "0x3" {}}}}
            effects     (collectibles/clear-stored-collectibles {:db db})
            result-db   (:db effects)]

        (is (match? result-db expected-db))))))

(deftest store-last-collectible-details
  (testing "store-last-collectible-details"
    (let [db               {:wallet {}}
          last-collectible {:description "Pandaria"
                            :image-url   "https://..."}
          expected-db      {:wallet {:last-collectible-details {:description "Pandaria"
                                                                :image-url   "https://..."}}}
          effects          (collectibles/store-last-collectible-details {:db db} [last-collectible])
          result-db        (:db effects)]
      (is (match? result-db expected-db)))))

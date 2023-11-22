(ns status-im2.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im2.contexts.wallet.events :as events]))

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
        (is (match? result (events/displayable-collectible? collection))))))

  (testing "save-collectibles-request-details"
    (let [db           {:wallet {}}
          collectibles [{:collectible-data {:image-url "https://..." :animation-url "https://..."}}
                        {:collectible-data {:image-url "" :animation-url "https://..."}}
                        {:collectible-data {:image-url "" :animation-url nil}}]
          expected-db  {:wallet {:collectibles [{:collectible-data
                                                 {:image-url "https://..." :animation-url "https://..."}}
                                                {:collectible-data
                                                 {:image-url "" :animation-url "https://..."}}]}}
          effects      (events/store-collectibles {:db db} [collectibles])
          result-db    (:db effects)]
      (is (match? result-db expected-db)))))

(deftest clear-stored-collectibles
  (let [db {:wallet {:collectibles [{:id 1} {:id 2}]}}]
    (testing "clear-stored-collectibles"
      (let [expected-db {:wallet {}}
            effects     (events/clear-stored-collectibles {:db db})
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest store-last-collectible-details
  (testing "store-last-collectible-details"
    (let [db               {:wallet {}}
          last-collectible {:description "Pandaria"
                            :image-url   "https://..."}
          expected-db      {:wallet {:last-collectible-details {:description "Pandaria"
                                                                :image-url   "https://..."}}}
          effects          (events/store-last-collectible-details {:db db} [last-collectible])
          result-db        (:db effects)]
      (is (match? result-db expected-db)))))

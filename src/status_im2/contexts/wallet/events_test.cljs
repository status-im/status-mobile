(ns status-im2.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im2.contexts.wallet.events :as events]))

(def address "0x2f88d65f3cb52605a54a833ae118fb1363acccd2")

(deftest scan-address-success
  (let [db {}]
    (testing "scan-address-success"
      (let [expected-db {:wallet/scanned-address address}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (= result-db expected-db))))))

(deftest clean-scanned-address
  (let [db {:wallet/scanned-address address}]
    (testing "clean-scanned-address"
      (let [expected-db {}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (= result-db expected-db))))))

(deftest store-collectibles
  (testing "(displayable-collectible?) helper function"
    (let [expected-results [[true {:image-url "https://..." :animation-url "https://..."}]
                            [true {:image-url "" :animation-url "https://..."}]
                            [true {:image-url nil :animation-url "https://..."}]
                            [true {:image-url "https://..." :animation-url ""}]
                            [true {:image-url "https://..." :animation-url nil}]
                            [false {:image-url "" :animation-url nil}]
                            [false {:image-url nil :animation-url nil}]
                            [false {:image-url nil :animation-url ""}]
                            [false {:image-url "" :animation-url ""}]]]
      (doseq [[result collection] expected-results]
        (is (= result (events/displayable-collectible? collection))))))
  (testing "save-collectibles-request-details"
    (let [db           {:wallet {}}
          collectibles [{:image-url "https://..." :animation-url "https://..."}
                        {:image-url "" :animation-url "https://..."}
                        {:image-url "" :animation-url nil}]
          expected-db  {:wallet {:collectibles [{:image-url "https://..." :animation-url "https://..."}
                                                {:image-url "" :animation-url "https://..."}]}}
          effects      (events/store-collectibles {:db db} [collectibles])
          result-db    (:db effects)]
      (is (= result-db expected-db)))))

(deftest clear-stored-collectibles
  (let [db {:wallet {:collectibles [{:id 1} {:id 2}]}}]
    (testing "clear-stored-collectibles"
      (let [expected-db {:wallet {}}
            effects     (events/clear-stored-collectibles {:db db})
            result-db   (:db effects)]
        (is (= result-db expected-db))))))

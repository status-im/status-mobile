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
      (let [expected-db {:wallet {:ui {:scanned-address address}}}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest clean-scanned-address
  (let [db {:wallet {:ui {:scanned-address address}}}]
    (testing "clean-scanned-address"
      (let [expected-db {:wallet {:ui {:send            nil
                                       :scanned-address nil}}}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest store-secret-phrase
  (let [db          {}
        props       [{:secret-phrase "test-secret" :random-phrase "random-test"}]
        expected-db {:wallet {:ui {:create-account {:secret-phrase "test-secret"
                                                    :random-phrase "random-test"}}}}
        effects     (events/store-secret-phrase {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest new-keypair-created
  (let [db          {}
        props       [{:new-keypair "test-keypair"}]
        expected-db {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        effects     (events/new-keypair-created {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest new-keypair-continue
  (let [db               {:wallet {:ui {:create-account {:secret-phrase "test-secret"}}}}
        props            [{:keypair-name "test-keypair"}]
        expected-effects [[:effects.wallet/create-account-from-mnemonic
                           {:secret-phrase "test-secret" :keypair-name "test-keypair"}]]
        effects          (events/new-keypair-continue {:db db} props)]
    (is (match? effects {:fx expected-effects}))))

(deftest clear-new-keypair
  (let [db          {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        expected-db {:wallet {:ui {:create-account {}}}}
        effects     (events/clear-new-keypair {:db db})]
    (is (match? (:db effects) expected-db))))

(deftest store-collectibles
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
          result-db     (:db (collectibles/flush-collectibles {:db db}))]

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

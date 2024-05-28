(ns status-im.contexts.settings.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.settings.wallet.events :as sut]))

(def mock-key-uid "key-1")
(defn mock-db
  [keypairs accounts]
  {:wallet  {:keypairs keypairs
             :accounts accounts}
   :profile {:profile {:key-uid "test-key-uid"}}})

(deftest test-rename-keypair
  (let [new-keypair-name "key pair new"
        cofx             {:db {}}
        expected         {:fx [[:json-rpc/call
                                [{:method     "accounts_updateKeypairName"
                                  :params     [mock-key-uid new-keypair-name]
                                  :on-success [:wallet/rename-keypair-success mock-key-uid
                                               new-keypair-name]
                                  :on-error   fn?}]]]}]
    (is (match? expected
                (sut/rename-keypair cofx
                                    [{:key-uid      mock-key-uid
                                      :keypair-name new-keypair-name}])))))

(deftest test-get-keypair-export-connection
  (let [db              (mock-db [] {})
        sha3-pwd        "test-password"
        keypair-key-uid "test-keypair-uid"
        callback        (fn [connect-string] (println "callback" connect-string))]
    (testing "test-get-keypair-export-connection"
      (let [effects (sut/get-keypair-export-connection
                     {:db db}
                     [{:sha3-pwd sha3-pwd :keypair-key-uid keypair-key-uid :callback callback}])
            fx      (:fx effects)]
        (is (some? fx))))))

(deftest test-remove-keypair
  (let [db (mock-db [{:key-uid mock-key-uid :name "Key 1"}] {})]
    (testing "remove-keypair"
      (let [effects (sut/remove-keypair {:db db} [{:key-uid mock-key-uid}])
            fx      (:fx effects)]
        (is (some? fx))))))

(deftest test-make-keypairs-accounts-fully-operable
  (let [db                 (mock-db [{:key-uid  mock-key-uid
                                      :accounts [{:key-uid mock-key-uid :operable "no"}]}]
                                    {"0x1" {:key-uid mock-key-uid :operable "no"}})
        key-uids-to-update [mock-key-uid]]
    (testing "make-keypairs-accounts-fully-operable"
      (let [effects         (sut/make-keypairs-accounts-fully-operable {:db db} [key-uids-to-update])
            result-db       (:db effects)
            updated-keypair (some #(when (= (:key-uid %) mock-key-uid) %)
                                  (get-in result-db [:wallet :keypairs]))
            updated-account (get-in result-db [:wallet :accounts "0x1"])]
        (is (= (keyword (-> updated-keypair :accounts first :operable)) :fully))
        (is (= (keyword (:operable updated-account)) :fully))))))

(deftest test-connection-string-for-import-keypair
  (let [db                (mock-db [] {})
        sha3-pwd          "test-password"
        keypairs-key-uids ["test-keypair-uid"]
        connection-string "test-connection-string"]
    (testing "connection-string-for-import-keypair"
      (let [effects (sut/connection-string-for-import-keypair
                     {:db db}
                     [{:sha3-pwd          sha3-pwd
                       :keypairs-key-uids keypairs-key-uids
                       :connection-string connection-string}])
            fx      (:fx effects)]
        (is (some? fx))))))

(deftest test-success-keypair-qr-scan
  (let [connection-string "valid-connection-string"
        keypairs-key-uids ["keypair-uid"]]
    (testing "success-keypair-qr-scan"
      (let [effects (sut/success-keypair-qr-scan nil [connection-string keypairs-key-uids])
            fx      (:fx effects)]
        (is (some? fx))))))

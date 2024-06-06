(ns status-im.contexts.settings.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [native-module.core :as native-module]
    [status-im.contexts.settings.wallet.events :as sut]
    [utils.security.core :as security]))

(def mock-key-uid "key-1")
(defn mock-db
  [keypairs accounts]
  {:wallet          {:keypairs keypairs
                     :accounts accounts}
   :profile/profile {:key-uid "test-key-uid"}})

(deftest rename-keypair-test
  (let [new-keypair-name "key pair new"
        cofx             {:db {}}]
    (testing "rename-keypair"
      (let [expected {:fx [[:json-rpc/call
                            [{:method     "accounts_updateKeypairName"
                              :params     [mock-key-uid new-keypair-name]
                              :on-success [:wallet/rename-keypair-success mock-key-uid
                                           new-keypair-name]
                              :on-error   fn?}]]]}]
        (is (match? expected
                    (sut/rename-keypair cofx
                                        [{:key-uid      mock-key-uid
                                          :keypair-name new-keypair-name}])))))))

(deftest get-keypair-export-connection-test
  (let [cofx         {:db (mock-db [] {})}
        sha3-pwd     "test-password"
        user-key-uid "test-key-uid"
        callback     (fn [connect-string] (println "callback" connect-string))]
    (testing "get-keypair-export-connection"
      (let [expected {:fx [[:effects.syncing/export-keypairs-keystores
                            {:key-uid         user-key-uid
                             :sha3-pwd        sha3-pwd
                             :keypair-key-uid mock-key-uid
                             :on-success      fn?
                             :on-fail         fn?}]]}]
        (is (match? expected
                    (sut/get-keypair-export-connection
                     cofx
                     [{:sha3-pwd sha3-pwd :keypair-key-uid mock-key-uid :callback callback}])))))))

(deftest remove-keypair-test
  (let [cofx {:db {}}]
    (testing "remove-keypair"
      (let [expected {:fx [[:json-rpc/call
                            [{:method     "accounts_deleteKeypair"
                              :params     [mock-key-uid]
                              :on-success [:wallet/remove-keypair-success mock-key-uid]
                              :on-error   fn?}]]]}]
        (is (match? expected
                    (sut/remove-keypair cofx [mock-key-uid])))))))

(deftest make-keypairs-accounts-fully-operable-test
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

(deftest connection-string-for-import-keypair-test
  (let [cofx              {:db (mock-db [] {})}
        sha3-pwd          "test-password"
        user-key-uid      "test-key-uid"
        connection-string "test-connection-string"]
    (testing "connection-string-for-import-keypair"
      (let [expected {:fx [[:effects.syncing/import-keypairs-keystores
                            {:key-uid           user-key-uid
                             :sha3-pwd          sha3-pwd
                             :keypairs-key-uids [mock-key-uid]
                             :connection-string connection-string
                             :on-success        fn?
                             :on-fail           fn?}]]}]
        (is (match? expected
                    (sut/connection-string-for-import-keypair cofx
                                                              [{:sha3-pwd sha3-pwd
                                                                :keypairs-key-uids [mock-key-uid]
                                                                :connection-string
                                                                connection-string}])))))))

(deftest success-keypair-qr-scan-test
  (let [connection-string "valid-connection-string"
        keypairs-key-uids ["keypair-uid"]]
    (testing "success-keypair-qr-scan"
      (let [effects (sut/success-keypair-qr-scan nil [connection-string keypairs-key-uids])
            fx      (:fx effects)]
        (is (some? fx))))))

(deftest wallet-validate-seed-phrase-test
  (let [cofx               {:db {}}
        seed-phrase-masked (security/mask-data "seed phrase")
        on-success         #(prn "success")
        on-error           #(prn "error")
        expected           {:fx [[:multiaccount/validate-mnemonic
                                  [seed-phrase-masked on-success on-error]]]}]
    (is (= expected
           (sut/wallet-validate-seed-phrase
            cofx
            [seed-phrase-masked on-success on-error])))))

(deftest make-seed-phrase-keypair-fully-operable-test
  (let [cofx            {:db {}}
        mnemonic        "seed phrase"
        password        "password"
        mnemonic-masked (security/mask-data mnemonic)
        password-masked (security/mask-data password)
        on-success      #(prn "success")
        on-error        #(prn "error")
        expected        {:fx [[:json-rpc/call
                               [{:method     "accounts_makeSeedPhraseKeypairFullyOperable"
                                 :params     [mnemonic (native-module/sha3 password)]
                                 :on-success fn?
                                 :on-error   fn?}]]]}]
    (is (match? expected
                (sut/make-seed-phrase-keypair-fully-operable
                 cofx
                 [mnemonic-masked password-masked on-success on-error])))))

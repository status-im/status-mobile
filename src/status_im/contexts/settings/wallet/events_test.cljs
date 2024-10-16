(ns status-im.contexts.settings.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    matcher-combinators.test
    [native-module.core :as native-module]
    [status-im.contexts.settings.wallet.events :as sut]
    [utils.security.core :as security]))

(def test-profile {:key-uid "test-key-uid"})
(def test-keypair-key-uid "key-1")

(deftest rename-keypair-test
  (let [new-keypair-name "key pair new"
        cofx             {:db {}}
        expected         {:fx [[:json-rpc/call
                                [{:method     "accounts_updateKeypairName"
                                  :params     [test-keypair-key-uid new-keypair-name]
                                  :on-success [:wallet/rename-keypair-success test-keypair-key-uid
                                               new-keypair-name]
                                  :on-error   fn?}]]]}]
    (is (match? expected
                (sut/rename-keypair cofx
                                    [{:key-uid      test-keypair-key-uid
                                      :keypair-name new-keypair-name}])))))

(deftest get-keypair-export-connection-test
  (let [cofx         {:db {:profile/profile test-profile}}
        sha3-pwd     "test-password"
        user-key-uid "test-key-uid"
        callback     (fn [connect-string] (println "callback" connect-string))
        expected     {:fx [[:effects.syncing/export-keypairs-keystores
                            {:key-uid         user-key-uid
                             :sha3-pwd        sha3-pwd
                             :keypair-key-uid test-keypair-key-uid
                             :on-success      fn?
                             :on-fail         fn?}]]}]
    (is (match?
         expected
         (sut/get-keypair-export-connection
          cofx
          [{:sha3-pwd sha3-pwd :keypair-key-uid test-keypair-key-uid :callback callback}])))))

(deftest remove-keypair-test
  (let [cofx     {:db {}}
        expected {:fx [[:json-rpc/call
                        [{:method     "accounts_deleteKeypair"
                          :params     [test-keypair-key-uid]
                          :on-success [:wallet/remove-keypair-success test-keypair-key-uid]
                          :on-error   fn?}]]]}]
    (is (match? expected
                (sut/remove-keypair cofx [test-keypair-key-uid])))))

(deftest make-keypairs-accounts-fully-operable-test
  (let [db                 {:wallet          {:keypairs {test-keypair-key-uid {:key-uid
                                                                               test-keypair-key-uid
                                                                               :lowest-operability :no
                                                                               :accounts
                                                                               [{:key-uid
                                                                                 test-keypair-key-uid
                                                                                 :operable :no}]}}
                                              :accounts {"0x1" {:key-uid  test-keypair-key-uid
                                                                :operable :no}}}
                            :profile/profile test-profile}
        key-uids-to-update [test-keypair-key-uid]
        effects            (sut/make-keypairs-accounts-fully-operable {:db db} [key-uids-to-update])
        result-db          (:db effects)
        updated-keypair    (get-in result-db [:wallet :keypairs test-keypair-key-uid])
        updated-account    (get-in result-db [:wallet :accounts "0x1"])]
    (is (= (-> updated-keypair :accounts first :operable) :fully))
    (is (= (:operable updated-account) :fully))
    (is (= (:lowest-operability updated-keypair) :fully))))

(deftest connection-string-for-import-keypair-test
  (let [cofx              {:db {:profile/profile test-profile}}
        sha3-pwd          "test-password"
        user-key-uid      "test-key-uid"
        connection-string "test-connection-string"
        expected          {:fx [[:effects.syncing/import-keypairs-keystores
                                 {:key-uid           user-key-uid
                                  :sha3-pwd          sha3-pwd
                                  :keypairs-key-uids [test-keypair-key-uid]
                                  :connection-string connection-string
                                  :on-success        fn?
                                  :on-fail           fn?}]]}]
    (is (match? expected
                (sut/connection-string-for-import-keypairs cofx
                                                           [{:sha3-pwd sha3-pwd
                                                             :keypairs-key-uids [test-keypair-key-uid]
                                                             :connection-string
                                                             connection-string}])))))

(deftest success-keypair-qr-scan-test
  (let [connection-string "valid-connection-string"
        keypairs-key-uids [test-keypair-key-uid]
        effects           (sut/success-keypair-qr-scan nil [connection-string keypairs-key-uids])
        fx                (:fx effects)]
    (is (some? fx))))

(deftest wallet-validate-seed-phrase-test
  (let [cofx               {:db {}}
        seed-phrase-masked (security/mask-data "seed phrase")
        on-success         #(prn "success")
        on-error           #(prn "error")
        expected           {:fx [[:effects.profile/validate-recovery-phrase
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

(deftest make-partly-operable-accounts-fully-operable-test
  (let [cofx            {:db {}}
        password-masked (security/mask-data "password")
        on-success      #(prn "success")
        on-error        #(prn "error")
        expected        {:fx [[:json-rpc/call
                               [{:method     "accounts_makePartiallyOperableAccoutsFullyOperable"
                                 :params     [(security/safe-unmask-data password-masked)]
                                 :on-success fn?
                                 :on-error   fn?}]]]}]
    (is (match? expected
                (sut/make-partially-operable-accounts-fully-operable
                 cofx
                 [{:password   password-masked
                   :on-success on-success
                   :on-error   on-error}])))))

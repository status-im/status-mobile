(ns status-im.common.standard-authentication.events-test
  (:require [cljs.test :refer [deftest is are testing]]
            matcher-combinators.test
            [status-im.common.standard-authentication.events :as sut]))

(def profile-keypair
  {:key-uid            "profile-key-uid"
   :name               "profile"
   :type               :profile
   :lowest-operability :fully
   :accounts           [{:path     "m/44'/60'/0'/0/0"
                         :key-uid  "profile-key-uid"
                         :address  "0x1"
                         :wallet   true
                         :type     "generated"
                         :operable :fully}
                        {:path     "m/44'/60'/0'/0/1"
                         :key-uid  "profile-key-uid"
                         :address  "0x2"
                         :wallet   true
                         :type     "generated"
                         :operable :fully}]})

(def second-keypair
  {:key-uid            "second-key-uid"
   :name               "second"
   :type               :seed
   :lowest-operability :fully
   :accounts           [{:path     "m/44'/60'/0'/0/0"
                         :key-uid  "second-key-uid"
                         :address  "0x3"
                         :wallet   true
                         :type     "generated"
                         :operable :fully}]})

(def default-cofx
  {:db {:wallet {:keypairs {"profile-key-uid" profile-keypair}}}})

(deftest authorize-and-sign-test
  (testing "sign-payload not passing schema validation"
    (are [args] (thrown? js/Error (sut/authorize-and-sign default-cofx [args]))
     {:sign-payload    []
      :on-sign-success identity}
     {:sign-payload    [{:address "0x0"}]
      :on-sign-success identity}
     {:sign-payload    [{:message "0x0"}]
      :on-sign-success identity}
     {:sign-payload [{:message "0x0"
                      :address "0x1"}]}
     {:on-sign-success identity}))

  (testing "sign-payload is passing schema validation"
    (let [args {:sign-payload    [{:message "0x0" :address "0x1"}
                                  {:message "0x0" :address "0x2"}]
                :on-sign-success identity}]
      (is (match? {} (sut/authorize-and-sign default-cofx [args])))))

  (testing "signing-payload with addresses from multiple keypairs is unsupported for keycard"
    (let [cofx     {:db {:wallet          {:keypairs {"profile-key-uid" profile-keypair
                                                      "second-key-uid"  second-keypair}}
                         :profile/profile {:keycard-pairing "pairing-key"}}}
          args     {:sign-payload    [{:message "0x0" :address "0x1"}
                                      {:message "0x0" :address "0x2"}
                                      {:message "0x0" :address "0x3"}]
                    :on-sign-success identity}
          expected {:fx [[:dispatch [:keycard/feature-unavailable-show {}]]]}]
      (is (match? expected (sut/authorize-and-sign cofx [args])))))

  (testing "signing with the keycard"
    (let [keycard-keypair (assoc profile-keypair :keycards [{:key-uid "profile-key-uid"}])
          cofx            {:db {:wallet          {:keypairs {"profile-key-uid" keycard-keypair}}
                                :profile/profile {:keycard-pairing "pairing-key"}}}
          args            {:sign-payload    [{:message "0x0" :address "0x1"}
                                             {:message "0x0" :address "0x2"}]
                           :on-sign-success identity}
          expected        {:fx [[:dispatch [:standard-auth/authorize-with-keycard {}]]]}]
      (is (match? expected (sut/authorize-and-sign cofx [args])))))

  (testing "signing locally with encryption key stored on keycard"
    (let [cofx     {:db {:wallet          {:keypairs {"profile-key-uid" profile-keypair}}
                         :profile/profile {:keycard-pairing "pairing-key"}}}
          args     {:sign-payload    [{:message "0x0" :address "0x1"}
                                      {:message "0x0" :address "0x2"}]
                    :on-sign-success identity}
          expected {:fx [[:dispatch [:standard-auth/authorize {}]]]}]
      (is (match? expected (sut/authorize-and-sign cofx [args])))))

  (testing "signing locally with password/biometric"
    (let [args     {:sign-payload    [{:message "0x0" :address "0x1"}
                                      {:message "0x0" :address "0x2"}]
                    :on-sign-success identity}
          expected {:fx [[:dispatch [:standard-auth/authorize {}]]]}]
      (is (match? expected (sut/authorize-and-sign default-cofx [args]))))))

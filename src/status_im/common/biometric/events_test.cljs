(ns status-im.common.biometric.events-test
  (:require [cljs.test :refer [deftest is testing]]
            matcher-combinators.test
            [status-im.common.biometric.events :as sut]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.security.core :as security]))

(deftest set-supported-biometrics-type-test
  (testing "successfully setting supported biometrics type"
    (let [cofx           {:db {}}
          supported-type constants/biometrics-type-face-id
          expected       {:db (assoc-in (:db cofx) [:biometrics :supported-type] supported-type)}]
      (is (match? expected (sut/set-supported-type cofx [supported-type]))))))

(deftest show-message-test
  (testing "informs the user to enable biometrics from settings"
    (let [cofx     {:db {}}
          expected {:fx [[:effects.utils/show-popup
                          {:title   (i18n/label :t/biometric-auth-login-error-title)
                           :content (i18n/label :t/grant-fingerprints-permissions)}]]}]
      (is (match? expected
                  (sut/show-message cofx
                                    [:biometrics/not-available-error])))))

  (testing "shows a generic error message"
    (let [cofx        {:db {}}
          error-cause :test-error
          expected    {:fx [[:effects.utils/show-popup
                             {:title   (i18n/label :t/biometric-auth-login-error-title)
                              :content (i18n/label :t/biometric-auth-error {:code error-cause})}]]}]
      (is (match? expected
                  (sut/show-message cofx
                                    [error-cause]))))))

(deftest authenticate-biometrics-test
  (testing "passing the right args to authenticate"
    (let [cofx   {:db {}}
          args   {:on-success     identity
                  :on-fail        identity
                  :prompt-message "test"}
          result (sut/authenticate cofx [args])]
      (is (= (:prompt-message args) (get-in result [:fx 0 1 :prompt-message])))
      (is (not (nil? (get-in result [:fx 0 1 :on-success]))))
      (is (not (nil? (get-in result [:fx 0 1 :on-fail]))))))

  (testing "skips biometric check if another one pending"
    (let [cofx   {:db {:biometrics {:auth-pending? true}}}
          result (sut/authenticate cofx [{}])]
      (is (nil? result)))))

(deftest enable-biometrics-test
  (testing "successfully enabling biometrics"
    (let [key-uid     "test-uid"
          password    (security/mask-data "test-pw")
          cofx        {:db {:profile/profile {:key-uid key-uid}}}
          expected-db (assoc (:db cofx) :auth-method constants/auth-method-biometric)
          result      (sut/enable-biometrics cofx [password])]
      (is (match? expected-db (:db result)))
      (is (= password (get-in result [:fx 0 1 1 :masked-password]))))))

(deftest disable-biometrics-test
  (testing "successfully disabling biometrics"
    (let [key-uid  "test-uid"
          cofx     {:db {:profile/profile {:key-uid key-uid}}}
          expected {:db (assoc (:db cofx) :auth-method constants/auth-method-none)
                    :fx [[:keychain/clear-user-password key-uid]]}]
      (is (match? expected (sut/disable-biometrics cofx))))))

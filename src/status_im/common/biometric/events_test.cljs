(ns status-im.common.biometric.events-test
  (:require [cljs.test :refer [deftest testing is]]
            matcher-combinators.test
            [react-native.biometrics :as biometrics]
            [status-im.common.biometric.events :as sut]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.security.core :as security]))

(deftest set-supported-biometrics-type-test
  (testing "successfully setting supported biometrics type"
    (let [cofx           {:db {}}
          supported-type constants/biometrics-type-face-id
          expected       {:db (assoc (:db cofx) :biometric/supported-type supported-type)}]
      (is (match? expected (sut/set-supported-type cofx [supported-type])))))

  (testing "throws error when setting unsupported biometrics type"
    (let [cofx           {:db {}}
          supported-type :unsupported-type]
      (is (thrown? js/Error (sut/set-supported-type cofx [supported-type]))))))

(deftest show-message-test
  (testing "informs the user to enable biometrics from settings"
    (let [cofx     {:db {}}
          expected {:fx [[:effects.utils/show-popup
                          {:title   (i18n/label :t/biometric-auth-login-error-title)
                           :content (i18n/label :t/grant-face-id-permissions)}]]}]
      (is (match? expected
                  (sut/show-message cofx
                                    [::biometrics/not-available])))))

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
    (let [cofx     {:db {}}
          args     {:on-success     identity
                    :on-fail        identity
                    :prompt-message "test"}
          expected [:fx [[:biometric-authenticate args]]]]
      (is (match? expected (sut/authenticate cofx [args]))))))

(deftest enable-biometrics-test
  (testing "successfully enabling biometrics"
    (let [key-uid     "test-uid"
          password    (security/mask-data "test-pw")
          cofx        {:db {:profile/profile {:key-uid key-uid}}}
          expected-db (assoc (:db cofx) :auth-method constants/auth-method-biometric)
          result      (sut/enable-biometrics cofx [password])]
      (is (match? expected-db (:db result)))
      (is (= password (get-in (:fx result) [0 1 1 :masked-password])))))

  (testing "throws error if raw password is passed"
    (let [key-uid  "test-uid"
          password "test-password"
          cofx     {:db {:profile/profile {:key-uid key-uid}}}]
      (is (thrown? js/Error (sut/enable-biometrics cofx [password]))))))

(deftest disable-biometrics-test
  (testing "successfully disabling biometrics"
    (let [key-uid  "test-uid"
          cofx     {:db {:profile/profile {:key-uid key-uid}}}
          expected {:db (assoc (:db cofx) :auth-method constants/auth-method-none)
                    :fx [[:keychain/clear-user-password key-uid]]}]
      (is (match? expected (sut/disable-biometrics cofx))))))

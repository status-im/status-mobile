(ns status-im.common.biometric.events-test
  (:require [cljs.test :refer [deftest testing is test-var]]
            matcher-combinators.test
            [react-native.biometrics :as biometrics]
            [status-im.common.biometric.events :as sut]
            [utils.i18n :as i18n]))

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

(comment
  (test-var #'show-message-test))

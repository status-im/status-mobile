(ns status-im.test.multiaccounts.login.core
  (:require [cljs.test :as test]
            [status-im.multiaccounts.login.core :as login]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.fx :as fx]))

(test/deftest save-password-test
  (test/testing "check save password, biometric unavailable"
    (let [initial-cofx {:db {:auth-method              keychain/auth-method-none
                             :supported-biometric-auth false}}
          {:keys [db]} (login/save-password initial-cofx true)]
      (test/is (= false (contains? db :popover/popover)))
      (test/is (= true (get-in db [:multiaccounts/login :save-password?])))
      (test/testing "uncheck save password"
        (let [{:keys [db]} (login/save-password {:db db} false)]
          (test/is (= false (contains? db :popover/popover)))
          (test/is (= false (get-in db [:multiaccounts/login :save-password?])))))))
  (test/testing "check save password, biometric available"
    (let [initial-cofx {:db {:auth-method              keychain/auth-method-none
                             :supported-biometric-auth true}}
          {:keys [db]} (login/save-password initial-cofx true)]
      (test/is (= :secure-with-biometric
                  (get-in db [:popover/popover :view])))
      (test/is (= true (get-in db [:multiaccounts/login :save-password?])))
      (test/testing "enable biometric auth"
        (let [{:keys [db] :as res} (biometric/enable {:db db})]
          (test/is (contains? res :biometric-auth/authenticate))
          (test/is (= false (contains? db :popover/popover)))
          (test/testing "biometric auth successfully enabled"
            (let [{:keys [db]} (biometric/setup-done
                                {:db db}
                                {:bioauth-success true
                                 :bioauth-message nil
                                 :bioauth-code    nil})]
              (test/is (= keychain/auth-method-biometric-prepare
                          (:auth-method db)))))
          (test/testing "biometric auth canceled"
            (let [{:keys [db]} (biometric/setup-done
                                {:db db}
                                {:bioauth-success false
                                 :bioauth-message nil
                                 :bioauth-code    "USER_CANCELED"})]
              (test/is (= nil db) "no db changes")))))))
  (test/testing (str "check save password, enable biometric auth,"
                     "uncheck save password")
    (let [initial-cofx {:db {:auth-method              keychain/auth-method-none
                             :supported-biometric-auth true}}
          {:keys [db]} (fx/merge
                        initial-cofx
                        (login/save-password true)
                        (biometric/enable)
                        (biometric/setup-done
                         {:bioauth-success true
                          :bioauth-message nil
                          :bioauth-code    nil})
                        (login/save-password false))]
      (test/is (= true (get-in db [:multiaccounts/login :save-password?])))
      ;; case 2 from https://github.com/status-im/status-react/issues/9573
      (test/is (= keychain/auth-method-biometric-prepare (:auth-method db)))
      (test/testing "disable biometric"
        (let [{:keys [db]} (biometric/disable {:db db})]
          (test/is (= false (get-in db [:multiaccounts/login :save-password?])))
          (test/is (= keychain/auth-method-none) (:auth-method db))))))
  (test/testing (str "check save password, skip biometric auth"
                     "uncheck save password, check again")
    (let [initial-cofx {:db {:auth-method              keychain/auth-method-none
                             :supported-biometric-auth true}}
          {:keys [db]} (fx/merge
                        initial-cofx
                        (login/save-password true)
                        (login/save-password false)
                        (login/save-password false))]
      ;; case 3 from https://github.com/status-im/status-react/issues/9573
      (test/is (= :secure-with-biometric (get-in db [:popover/popover :view]))))))

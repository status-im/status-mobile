(ns status-im.test.sign-in.flow
  "The main purpose of these tests is to signal that some steps of the sign in
  flow has been changed. Such changes should be reflected in both these tests
  and documents which describe the whole \"sign in\" flow."
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.events :as events]
            [status-im.data-store.core :as data-store]
            [status-im.multiaccounts.login.core :as login.core]
            [status-im.signals.core :as signals]
            [status-im.test.sign-in.data :as data]))

(deftest on-password-input-submitted
  (testing
   "handling :multiaccounts.login.ui/password-input-submitted event"
    (let [cofx {:db {:multiaccounts/login {:address  "address"
                                           :password "password"
                                           :name "user"
                                           :photo-path "photo"}}}
          efx (login.core/login cofx)]
      (testing "Change multiaccount."
        (is (= (::login.core/login efx)
               ["{\"name\":\"user\",\"address\":\"address\",\"photo-path\":\"photo\"}" "password"])))
      (testing "start activity indicator"
        (is (= (get-in efx [:db :multiaccounts/login :processing]) true))))))

(deftest login-success
  (testing ":accounts.login.callback/login-success event received."
    (let [db           {:multiaccounts/login  {:address  "address"
                                               :password "password"}
                        :multiaccount data/multiaccount}
          cofx         {:db                           db
                        :data-store/mailservers       []
                        :data-store/mailserver-topics data/topics}
          login-result "{\"error\":\"\"}"
          efx          (login.core/multiaccount-login-success cofx)
          new-db       (:db efx)
          json-rpc     (into #{} (map :method (::json-rpc/call efx)))]
      (testing ":accounts/login cleared."
        (is (not (contains? new-db :multiaccounts/login))))
      (testing "Check the rest of effects."
        (is (contains? efx ::data-store/change-multiaccount))
        (is (json-rpc "web3_clientVersion"))))))

;;TODO re-enable when keycard is fixed
#_(deftest login
    (testing "login with keycard"
      (let [wpk "c56c7ac797c27b3790ce02c2459e9957c5d20d7a2c55320535526ce9e4dcbbef"
            epk "04f43da85ff1c333f3e7277b9ac4df92c9120fbb251f1dede7d41286e8c055acfeb845f6d2654821afca25da119daff9043530b296ee0e28e202ba92ec5842d617"
            db {:hardwallet {:multiaccount {:encryption-public-key epk
                                            :whisper-private-key   wpk
                                            :wallet-address        "83278851e290d2488b6add2a257259f5741a3b7d"
                                            :whisper-public-key    "0x04491c1272149d7fa668afa45968c9914c0661641ace7dbcbc585c15070257840a0b4b1f71ce66c2147e281e1a44d6231b4731a26f6cc0a49e9616bbc7fc2f1a93"
                                            :whisper-address       "b8bec30855ff20c2ddab32282e2b2c8c8baca70d"}}}
            result (login.core/login {:db db})]
        (is (= (-> result (get :hardwallet/login-with-keycard) keys count)
               3))
        (is (= (get-in result [:hardwallet/login-with-keycard :whisper-private-key wpk])))
        (is (= (get-in result [:hardwallet/login-with-keycard :encryption-public-key epk])))
        (is (fn? (get-in result [:hardwallet/login-with-keycard :on-result]))))))

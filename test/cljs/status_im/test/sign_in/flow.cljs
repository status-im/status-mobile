(ns status-im.test.sign-in.flow
  "The main purpose of these tests is to signal that some steps of the sign in
  flow has been changed. Such changes should be reflected in both these tests
  and documents which describe the whole \"sign in\" flow."
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.events :as events]
            [status-im.multiaccounts.login.core :as login.core]
            [status-im.signals.core :as signals]
            [status-im.test.sign-in.data :as data]))

(deftest on-password-input-submitted
  (testing
   "handling :multiaccounts.login.ui/password-input-submitted event"
    (let [cofx             {:db {:multiaccounts/multiaccounts {"address" {:settings {:fleet "fleet"}}}
                                 :multiaccounts/login {:address  "address"
                                                       :password "password"}}}
          create-database? false
          efx              (login.core/user-login cofx create-database?)]
      (testing "Web data cleared."
        (is (contains? efx :multiaccounts.login/clear-web-data)))
      (testing "Change multiaccount."
        (is (= (:data-store/change-multiaccount efx)
               ["address" "password" false "fleet"])))
      (testing "set `node/on-ready` handler"
        (is (= (get-in efx [:db :node/on-ready]) :login)))
      (testing "start activity indicator"
        (is (= (get-in efx [:db :multiaccounts/login :processing]) true))))))

(deftest on-successful-multiaccount-change
  (testing
   "Multiaccount changed successfully: :init.callback/multiaccount-change-success
   event is handled."
    (let [db     {:multiaccounts/login    {:address  "address"
                                           :password "password"}
                  :node/on-ready     :login
                  :multiaccounts/multiaccounts data/multiaccounts}
          cofx   {:db                   db
                  :web3                 :web3
                  :all-contacts         data/all-contacts
                  :all-installations    []
                  :get-all-stored-chats data/get-chats}
          efx    (events/multiaccount-change-success cofx [nil "address"])
          new-db (:db efx)]
      (testing "Starting node."
        (is (contains? efx :node/start)))
      (testing "Get fcm token."
        (is (contains? efx :notifications/get-fcm-token)))
      (testing "Request notifications permissions."
        (is (contains? efx :notifications/request-notifications-permissions)))
      (testing "Navigate to :home."
        (is (= [:home nil] (efx :status-im.ui.screens.navigation/navigate-to))))
      (testing "Multiaccount selected."
        (is (contains? new-db :multiaccount)))
      (testing "Chats initialized."
        (is (= 3 (count (:chats new-db)))))
      (testing "Contacts initialized."
        (is (= 2 (count (:contacts/contacts new-db))))))))

(deftest decryption-failure-on-multiaccount-change
  (testing ":init.callback/multiaccount-change-error event received."
    (let [cofx   {:db {}}
          error  {:error :decryption-failed}
          efx    (login.core/handle-change-multiaccount-error cofx error)
          new-db (:db efx)]
      (testing "Init multiaccount's password verification"
        (is (= :verify-multiaccount (new-db :node/on-ready))))
      (testing "Init multiaccount's password verification"
        (is (= :decryption-failed (get-in new-db [:realm-error :error]))))
      (testing "Start node."
        (is (contains? efx :node/start))))))

(deftest database-does-not-exist-on-multiaccount-change
  (testing ":init.callback/multiaccount-change-error event received."
    (let [cofx   {:db {}}
          error  {:error :database-does-not-exist}
          efx    (login.core/handle-change-multiaccount-error cofx error)
          new-db (:db efx)]
      (testing "Init multiaccount's password verification"
        (is (= :verify-multiaccount (new-db :node/on-ready))))
      (testing "Init multiaccount's password verification"
        (is (= :database-does-not-exist (get-in new-db [:realm-error :error]))))
      (testing "Start node."
        (is (contains? efx :node/start))))))

(deftest migrations-failed-on-multiaccount-change
  (testing ":init.callback/multiaccount-change-error event received."
    (let [cofx  {:db {}}
          error {:error :migrations-failed}
          efx   (login.core/handle-change-multiaccount-error cofx error)]
      (testing "Show migrations dialog."
        (is (contains? efx :ui/show-confirmation))))))

(deftest unknown-realm-error-on-multiaccount-change
  (testing ":init.callback/multiaccount-change-error event received."
    (let [cofx  {:db {}}
          error {:error :unknown-error}
          efx   (login.core/handle-change-multiaccount-error cofx error)]
      (testing "Show unknown error dialog."
        (is (contains? efx :ui/show-confirmation))))))

(deftest on-node-started
  (testing "node.ready signal received"
    (let [cofx {:db {:multiaccounts/login    {:address  "address"
                                              :password "password"}
                     :node/on-ready     :login
                     :multiaccounts/multiaccounts data/multiaccounts
                     :multiaccount   data/multiaccounts}}
          efx  (signals/status-node-started cofx)]
      (testing "Change node's status to started."
        (is (= :started (get-in efx [:db :node/status])))))))

(deftest on-node-started-for-verification
  (testing "node.ready signal received"
    (let [cofx {:db {:multiaccounts/login    {:address  "address"
                                              :password "password"}
                     :node/on-ready     :verify-multiaccount
                     :multiaccounts/multiaccounts data/multiaccounts
                     :multiaccount   data/multiaccounts
                     :realm-error       {:error :database-does-not-exist}}}
          efx  (signals/status-node-started cofx)]
      (testing "Init VerifyAccountPassword call."
        (is (= ["address" "password" {:error :database-does-not-exist}]
               (:multiaccounts.login/verify efx))))
      (testing "Change node's status to started."
        (is (= :started (get-in efx [:db :node/status])))))))

(deftest on-verify-multiaccount-success-after-decryption-failure
  (testing ":multiaccounts.login.callback/verify-success event received."
    (let [cofx          {:db {}}
          verify-result "{\"error\":\"\"}"
          realm-error   {:error :decryption-failed}
          efx           (login.core/verify-callback cofx verify-result realm-error)]
      (testing "Show dialog."
        (is (contains? efx :ui/show-confirmation)))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

(deftest on-verify-multiaccount-success-after-database-does-not-exist
  (testing ":multiaccounts.login.callback/verify-success event received."
    (let [cofx          {:db {:multiaccounts/multiaccounts {"address" {:settings {:fleet "fleet"}}}
                              :multiaccounts/login {:address  "address"
                                                    :password "password"}}}
          verify-result "{\"error\":\"\"}"
          realm-error   {:error :database-does-not-exist}
          efx           (login.core/verify-callback
                         cofx verify-result realm-error)]
      (testing "Change multiaccount."
        (is (= ["address" "password" true "fleet"]
               (:data-store/change-multiaccount efx))))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

(deftest on-verify-multiaccount-failed
  (testing ":multiaccounts.login.callback/verify-success event received."
    (let [cofx          {:db {:multiaccounts/login {:address  "address"
                                                    :password "password"}}}
          verify-result "{\"error\":\"some error\"}"
          realm-error   {:error :database-does-not-exist}
          efx           (login.core/verify-callback
                         cofx verify-result realm-error)
          new-db        (:db efx)]
      (testing "Show error in sign in form."
        (is (= "some error" (get-in new-db [:multiaccounts/login :error]))))
      (testing "Hide activity indicator."
        (is (= false (get-in new-db [:multiaccounts/login :processing]))))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

#_(deftest login-success
    (testing ":accounts.login.callback/login-success event received."
      (let [db           {:accounts/login  {:address  "address"
                                            :password "password"}
                          :account/account data/account
                          :semaphores      #{}}
            cofx         {:db                           db
                          :data-store/mailservers       []
                          :data-store/transport         data/transport
                          :data-store/mailserver-topics data/topics}
            login-result "{\"error\":\"\"}"
            efx          (login.core/user-login-callback cofx login-result)
            new-db       (:db efx)
            json-rpc     (into #{} (map :method (:json-rpc/call efx)))]
        (testing ":accounts/login cleared."
          (is (not (contains? new-db :accounts/login))))
        (testing "Check messaging related effects."
          (is (contains? efx :filters/load-filters))
          (is (contains? efx :mailserver/add-peer))
          (is (contains? efx :mailserver/update-mailservers))
          (is (= #{{:ms       10000
                    :dispatch [:mailserver/check-connection-timeout]}
                   {:ms       10000
                    :dispatch [:protocol/state-sync-timed-out]}}
                 (set (:utils/dispatch-later efx)))))
        (testing "Check the rest of effects."
          (is (contains? efx :web3/set-default-account))
          (is (contains? efx :web3/fetch-node-version))
          (is (json-rpc "net_version"))
          (is (json-rpc "eth_syncing"))
          (is (contains? efx :wallet/get-balance))
          (is (contains? efx :wallet/get-tokens-balance))
          (is (contains? efx :wallet/get-prices))))))

(deftest login-failed
  (testing
   ":multiaccounts.login.callback/login-success event received with error."
    (let [db           {:multiaccounts/login  {:address  "address"
                                               :password "password"}
                        :multiaccount data/multiaccount
                        :semaphores      #{}}
          cofx         {:db                           db
                        :data-store/mailservers       []
                        :data-store/transport         data/transport
                        :data-store/mailserver-topics data/topics}
          login-result "{\"error\":\"Something went wrong!\"}"
          efx          (login.core/user-login-callback cofx login-result)
          new-db       (:db efx)]
      (testing "Prevent saving of the password."
        (is (= false (get-in new-db [:multiaccounts/login :save-password?]))))
      (testing "Show error in sign in form."
        (is (contains? (:multiaccounts/login new-db) :error)))
      (testing "Stop activity indicator."
        (is (= false (get-in new-db [:multiaccounts/login :processing]))))
      (testing "Show error in sign in form."
        (is (contains? (:multiaccounts/login new-db) :error)))
      (testing "Show error popup."
        (is (contains? efx :utils/show-popup)))
      (testing "Logout."
        (is (= [:multiaccounts.logout.ui/logout-confirmed] (:dispatch efx)))))))

(deftest login
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

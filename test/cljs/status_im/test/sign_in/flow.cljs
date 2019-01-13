(ns status-im.test.sign-in.flow
  "The main purpose of these tests is to signal that some steps of the sign in
  flow has been changed. Such changes should be reflected in both these tests
  and documents which describe the whole \"sign in\" flow."
  (:require [cljs.test :refer-macros [deftest is are testing]]
            [status-im.accounts.login.core :as login.core]
            [status-im.events :as events]
            [status-im.ui.screens.browser.default-dapps :as default-dapps]
            [status-im.test.sign-in.data :as data]
            [status-im.signals.core :as signals]))

(deftest on-password-input-submitted
  (testing
   "handling :accounts.login.ui/password-input-submitted event"
    (let [cofx             {:db {:accounts/login {:address  "address"
                                                  :password "password"}}}
          create-database? false
          efx              (login.core/user-login cofx create-database?)]
      (testing "Web data cleared."
        (is (contains? efx :accounts.login/clear-web-data)))
      (testing "Change account."
        (is (= (:data-store/change-account efx)
               ["address" "password" false])))
      (testing "set `node/on-ready` handler"
        (is (= (get-in efx [:db :node/on-ready]) :login)))
      (testing "start activity indicator"
        (is (= (get-in efx [:db :accounts/login :processing]) true))))))

(deftest on-successful-account-change
  (testing
   "Account changed successfully: :init.callback/account-change-success
   event is handled."
    (let [db     {:accounts/login    {:address  "address"
                                      :password "password"}
                  :node/on-ready     :login
                  :accounts/accounts data/accounts}
          cofx   {:db                   db
                  :web3                 :web3
                  :all-contacts         data/all-contacts
                  :all-installations    []
                  :all-stored-browsers  []
                  :all-dapp-permissions []
                  :default-dapps        default-dapps/all
                  :get-all-stored-chats data/get-chats}
          efx    (events/account-change-success cofx [nil "address"])
          new-db (:db efx)]
      (testing "Starting node."
        (is (contains? efx :node/start)))
      (testing "Get fcm token."
        (is (contains? efx :notifications/get-fcm-token)))
      (testing "Request notifications permissions."
        (is (contains? efx :notifications/request-notifications-permissions)))
      (testing "Navigate to :home."
        (is (= :home (efx :status-im.ui.screens.navigation/navigate-to))))
      (testing "Account selected."
        (is (contains? new-db :account/account)))
      (testing "Chats initialized."
        (is (= 3 (count (:chats new-db)))))
      (testing "Contacts initialized."
        (is (= 2 (count (:contacts/contacts new-db))))))))

(deftest decryption-failure-on-account-change
  (testing ":init.callback/account-change-error event received."
    (let [cofx   {:db {}}
          error  {:error :decryption-failed}
          efx    (login.core/handle-change-account-error cofx error)
          new-db (:db efx)]
      (testing "Init account's password verification"
        (is (= :verify-account (new-db :node/on-ready))))
      (testing "Init account's password verification"
        (is (= :decryption-failed (get-in new-db [:realm-error :error]))))
      (testing "Start node."
        (is (contains? efx :node/start))))))

(deftest database-does-not-exist-on-account-change
  (testing ":init.callback/account-change-error event received."
    (let [cofx   {:db {}}
          error  {:error :database-does-not-exist}
          efx    (login.core/handle-change-account-error cofx error)
          new-db (:db efx)]
      (testing "Init account's password verification"
        (is (= :verify-account (new-db :node/on-ready))))
      (testing "Init account's password verification"
        (is (= :database-does-not-exist (get-in new-db [:realm-error :error]))))
      (testing "Start node."
        (is (contains? efx :node/start))))))

(deftest migrations-failed-on-account-change
  (testing ":init.callback/account-change-error event received."
    (let [cofx  {:db {}}
          error {:error :migrations-failed}
          efx   (login.core/handle-change-account-error cofx error)]
      (testing "Show migrations dialog."
        (is (contains? efx :ui/show-confirmation))))))

(deftest unknown-realm-error-on-account-change
  (testing ":init.callback/account-change-error event received."
    (let [cofx  {:db {}}
          error {:error :unknown-error}
          efx   (login.core/handle-change-account-error cofx error)]
      (testing "Show unknown error dialog."
        (is (contains? efx :ui/show-confirmation))))))

(deftest on-node-started
  (testing "node.ready signal received"
    (let [cofx {:db {:accounts/login    {:address  "address"
                                         :password "password"}
                     :node/on-ready     :login
                     :accounts/accounts data/accounts
                     :account/account   data/accounts}}
          efx  (signals/status-node-started cofx)]
      (testing "Init Login call."
        (is (= ["address" "password"] (:accounts.login/login efx))))
      (testing "Change node's status to started."
        (is (= :started (get-in efx [:db :node/status])))))))

(deftest on-node-started-for-verification
  (testing "node.ready signal received"
    (let [cofx {:db {:accounts/login    {:address  "address"
                                         :password "password"}
                     :node/on-ready     :verify-account
                     :accounts/accounts data/accounts
                     :account/account   data/accounts
                     :realm-error       {:error :database-does-not-exist}}}
          efx  (signals/status-node-started cofx)]
      (testing "Init VerifyAccountPassword call."
        (is (= ["address" "password" {:error :database-does-not-exist}]
               (:accounts.login/verify efx))))
      (testing "Change node's status to started."
        (is (= :started (get-in efx [:db :node/status])))))))

(deftest on-verify-account-success-after-decryption-failure
  (testing ":accounts.login.callback/verify-success event received."
    (let [cofx          {:db {}}
          verify-result "{\"error\":\"\"}"
          realm-error   {:error :decryption-failed}
          efx           (login.core/verify-callback cofx verify-result realm-error)]
      (testing "Show dialog."
        (is (contains? efx :ui/show-confirmation)))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

(deftest on-verify-account-success-after-database-does-not-exist
  (testing ":accounts.login.callback/verify-success event received."
    (let [cofx          {:db {:accounts/login {:address  "address"
                                               :password "password"}}}
          verify-result "{\"error\":\"\"}"
          realm-error   {:error :database-does-not-exist}
          efx           (login.core/verify-callback
                         cofx verify-result realm-error)]
      (testing "Change account."
        (is (= ["address" "password" true]
               (:data-store/change-account efx))))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

(deftest on-verify-account-failed
  (testing ":accounts.login.callback/verify-success event received."
    (let [cofx          {:db {:accounts/login {:address  "address"
                                               :password "password"}}}
          verify-result "{\"error\":\"some error\"}"
          realm-error   {:error :database-does-not-exist}
          efx           (login.core/verify-callback
                         cofx verify-result realm-error)
          new-db        (:db efx)]
      (testing "Show error in sign in form."
        (is (= "some error" (get-in new-db [:accounts/login :error]))))
      (testing "Hide activity indicator."
        (is (= false (get-in new-db [:accounts/login :processing]))))
      (testing "Stop node."
        (is (contains? efx :node/stop))))))

(deftest login-success
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
          new-db       (:db efx)]
      (testing ":accounts/login cleared."
        (is (not (contains? new-db :accounts/login))))
      (testing "Check messaging related effects."
        (is (= 1 (count (get-in efx [:shh/restore-sym-keys-batch :transport]))))
        (is (contains? efx :shh/generate-sym-key-from-password))
        (is (contains? efx :shh/add-discovery-filters))
        (is (contains? efx :mailserver/add-peer))
        (is (contains? efx :mailserver/update-mailservers))
        (is (contains? efx :protocol/assert-correct-network))
        (is (= #{{:ms       10000
                  :dispatch [:mailserver/check-connection-timeout]}
                 {:ms       10000
                  :dispatch [:protocol/state-sync-timed-out]}}
               (set (:utils/dispatch-later efx)))))
      (testing "Check the rest of effects."
        (is (contains? efx :web3/set-default-account))
        (is (contains? efx :web3/get-block-number))
        (is (contains? efx :web3/fetch-node-version))
        (is (contains? efx :get-balance))
        (is (contains? efx :web3/get-syncing))
        (is (contains? efx :get-tokens-balance))
        (is (contains? efx :get-prices))
        (is (contains? efx :status-im.models.transactions/start-sync-transactions))))))

(deftest login-failed
  (testing
   ":accounts.login.callback/login-success event received with error."
    (let [db           {:accounts/login  {:address  "address"
                                          :password "password"}
                        :account/account data/account
                        :semaphores      #{}}
          cofx         {:db                           db
                        :data-store/mailservers       []
                        :data-store/transport         data/transport
                        :data-store/mailserver-topics data/topics}
          login-result "{\"error\":\"Something went wrong!\"}"
          efx          (login.core/user-login-callback cofx login-result)
          new-db       (:db efx)]
      (testing "Prevent saving of the password."
        (is (= false (get-in new-db [:accounts/login :save-password?]))))
      (testing "Show error in sign in form."
        (is (contains? (:accounts/login new-db) :error)))
      (testing "Stop activity indicator."
        (is (= false (get-in new-db [:accounts/login :processing]))))
      (testing "Show error in sign in form."
        (is (contains? (:accounts/login new-db) :error)))
      (testing "Show error popup."
        (is (contains? efx :utils/show-popup)))
      (testing "Logout."
        (is (= [:accounts.logout.ui/logout-confirmed] (:dispatch efx)))))))

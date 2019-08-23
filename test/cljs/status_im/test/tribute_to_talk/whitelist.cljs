(ns status-im.test.tribute-to-talk.whitelist
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.tribute-to-talk.whitelist :as whitelist]))

(def user-contacts
  {"not whitelisted"
   {:public-key "not whitelisted"
    :system-tags #{}}
   "not whitelisted2"
   {:public-key "not whitelisted2"
    :system-tags #{:contact/request-received}}
   "whitelisted because tribute paid"
   {:public-key "whitelisted because tribute paid"
    :system-tags #{:tribute-to-talk/paid}}
   "whitelisted because tribute received"
   {:public-key "whitelisted because tribute received"
    :system-tags #{:tribute-to-talk/received}}
   "whitelisted because added"
   {:public-key "whitelisted because added"
    :system-tags #{:contact/added}}})

(deftest get-contact-whitelist
  (is (= 3
         (count (whitelist/get-contact-whitelist
                 (vals user-contacts))))))

(deftest add-to-whitelist
  (testing "adding contact to whitelist"
    (is (= #{"bob" "bob2"}
           (get-in (whitelist/add-to-whitelist
                    {:db {:contacts/whitelist #{"bob2"}}} "bob")
                   [:db :contacts/whitelist])))
    (testing "when there is no whitelist yet"
      (is (= #{"bob"}
             (get-in (whitelist/add-to-whitelist
                      {:db {}} "bob")
                     [:db :contacts/whitelist]))
          (whitelist/add-to-whitelist {:db {}} "bob")))
    (testing "when already added"
      (is (= #{"bob"}
             (get-in (whitelist/add-to-whitelist
                      {:db {:contacts/whitelist #{"bob"}}} "bob")
                     [:db :contacts/whitelist]))))))

(deftest mark-tribute-paid
  (let [result (whitelist/mark-tribute-paid {:db {}} "bob")]
    (testing "contact was added to whitelist"
      (is (= (get-in result
                     [:db :contacts/whitelist])
             #{"bob"})))
    (testing "contact was tagged as tribute paid"
      (is (= (get-in result
                     [:db :contacts/contacts "bob" :system-tags])
             #{:tribute-to-talk/paid})))))

(deftest mark-tribute-received
  (let [result (whitelist/mark-tribute-received {:db {}} "bob")]
    (testing "contact was added to whitelist"
      (is (= (get-in result
                     [:db :contacts/whitelist])
             #{"bob"})))
    (testing "contact was tagged as tribute paid"
      (is (= (get-in result
                     [:db :contacts/contacts "bob" :system-tags])
             #{:tribute-to-talk/received})))))

(def sender-pk "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97")
(def sender-address "0xdff1a5e4e57d9723b3294e0f4413372e3ea9a8ff")

(def ttt-enabled-multiaccount
  {:db {:multiaccount {:settings {:tribute-to-talk {:testnet {:snt-amount "1000000000000000000"}}}}
        :networks/current-network "testnet_rpc"
        :networks/networks {"testnet_rpc" {:config {:NetworkId 3}}}
        :contacts/contacts user-contacts
        :wallet {:transactions
                 {"transaction-hash-1"
                  {:value "1000000000000000000"
                   :block "5"
                   :from sender-address}}}
        :ethereum/current-block 8}})

(def ttt-disabled-multiaccount
  {:db {:multiaccount {:settings {:tribute-to-talk {}}}
        :networks/current-network "testnet_rpc"
        :networks/networks {"testnet_rpc" {:config {:NetworkId 3}}}
        :contacts/contacts user-contacts}})

(deftest enable-whitelist
  (testing "ttt enabled multiaccount"
    (is (= (get-in (whitelist/enable-whitelist ttt-enabled-multiaccount)
                   [:db :contacts/whitelist]))))
  (testing "ttt disabled multiaccount"
    (is (not (get-in (whitelist/enable-whitelist ttt-disabled-multiaccount)
                     [:db :contacts/whitelist])))))

(deftest filter-message
  (testing "not a user message"
    (whitelist/filter-message
     ttt-enabled-multiaccount
     :unfiltered-fx
     :not-user-message
     nil
     "public-key"))
  (testing "user is whitelisted"
    (whitelist/filter-message
     (whitelist/enable-whitelist ttt-enabled-multiaccount)
     :unfiltered-fx
     :user-message
     nil
     "whitelisted because added"))
  (testing "tribute to talk is disabled"
    (whitelist/filter-message
     ttt-disabled-multiaccount
     :unfiltered-fx
     :user-message
     nil
     "public-key"))
  (testing "user is not whitelisted but transaction is valid"
    (let [result (whitelist/filter-message
                  ttt-enabled-multiaccount
                  #(assoc % :message-received true)
                  :user-message
                  "transaction-hash-1"
                  sender-pk)]
      (is (contains? (get-in result [:db :contacts/whitelist])
                     sender-pk))
      (is (:message-received result)))))

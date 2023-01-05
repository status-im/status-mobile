(ns status-im2.subs.subs-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.subs.onboarding :as onboarding]
            [status-im2.subs.wallet.transactions :as wallet.transactions]
            [status-im2.subs.chat.messages :as messages]
            [status-im2.common.constants :as constants]))

(def transactions
  [{:timestamp "1505912551000"}
   {:timestamp "1505764322000"}
   {:timestamp "1505750000000"}])

(def grouped-transactions
  '({:title "20 Sep"
     :key   :20170920
     :data
     ({:timestamp "1505912551000"})}
    {:title "18 Sep"
     :key   :20170918
     :data
     ({:timestamp "1505764322000"}
      {:timestamp "1505750000000"})}))

(deftest group-transactions-by-date
  (testing "Check if transactions are sorted by date"
    (is (= (wallet.transactions/group-transactions-by-date transactions)
           grouped-transactions))))

(deftest login-ma-keycard-pairing
  (testing "returns nil when no :multiaccounts/login"
    (let [res (onboarding/login-ma-keycard-pairing
                {:multiaccounts/login nil
                 :multiaccounts/multiaccounts
                 {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
                {})]
      (is (nil? res))))

  (testing "returns :keycard-pairing when :multiaccounts/login is present"
    (let [res (onboarding/login-ma-keycard-pairing
                {:multiaccounts/login {:key-uid "0x1"}
                 :multiaccounts/multiaccounts
                 {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
                {})]
      (is (= res "keycard-pairing-code")))))

(def messages-state
  [{:message-id "0x111" :album-id "abc" :albumize? true}
   {:message-id "0x222" :album-id "abc" :albumize? true}
   {:message-id "0x333" :album-id "abc" :albumize? true}
   {:message-id "0x444" :album-id "abc" :albumize? true}
   {:message-id "0x555" :album-id "efg" :albumize? true}])

(def messages-albumized-state
  [{:album        [{:message-id "0x444" :album-id "abc" :albumize? true}
                   {:message-id "0x333" :album-id "abc" :albumize? true}
                   {:message-id "0x222" :album-id "abc" :albumize? true}
                   {:message-id "0x111" :album-id "abc" :albumize? true}]
    :album-id     "abc"
    :message-id   "abc"
    :content-type constants/content-type-album}
   {:message-id "0x555" :album-id "efg" :albumize? true}])

(deftest find-albums
  (testing "Finding albums in the messages list"
    (is (= (messages/find-albums messages-state) messages-albumized-state))))

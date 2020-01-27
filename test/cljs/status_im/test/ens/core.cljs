(ns status-im.test.ens.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ens.core :as ens]))

(deftest should-be-verified-test?
  (testing "valid cases"
    (testing "stateofus.eth name"
      (let [cofx {:db {:contacts/contacts {}}}]
        (is (ens/should-be-verified? cofx "user.stateofus.eth" "user" 1))))
    (testing ".eth name"
      (let [cofx {:db {:contacts/contacts {}}}]
        (is (ens/should-be-verified? cofx "user.eth" "user" 1))))
    (testing "clock value is greater and name is different"
      (let [cofx {:db {:contacts/contacts {"user" {:ens-verified-at 1
                                                   :name "user.eth"
                                                   :ens-verified true}}}}]
        (is (ens/should-be-verified? cofx "user2.eth" "user" 2)))))
  (testing "invalid cases"
    (testing "invalid name"
      (let [cofx {:db {:contacts/contacts {}}}]
        (is (not (ens/should-be-verified? cofx "user.stateofus.ethanol" "user" 1)))))
    (testing "already verified"
      (let [cofx {:db {:contacts/contacts {"user" {:ens-verified-at 0
                                                   :name "user.eth"
                                                   :ens-verified true}}}}]
        (is (not (ens/should-be-verified? cofx "user.eth" "user" 0)))))
    (testing "already verified, name identical"
      (let [cofx {:db {:contacts/contacts {"user" {:ens-verified-at 0
                                                   :name "user.eth"
                                                   :ens-verified true}}}}]
        (is (not (ens/should-be-verified? cofx "user.eth" "user" 1)))))))


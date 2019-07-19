(ns status-im.test.multiaccounts.create.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.utils :as utils]
            [status-im.multiaccounts.create.core :as models]))

(deftest intro-step-back
  (testing "Back from choose-key"
    (let [db {:intro-wizard {:step :choose-key}}
          result (get-in (models/intro-step-back {:db db}) [:db :intro-wizard])]
      (is (= result  {:step :generate-key}))))

  (testing "Back from create-code"
    (let [db {:intro-wizard {:step :create-code :key-code "qwerty"}}
          result (get-in (models/intro-step-back {:db db}) [:db :intro-wizard])]
      (is (= result  {:step :select-key-storage :key-code nil :weak-password? true}))))

  (testing "Back from confirm-code"
    (let [db {:intro-wizard {:step :confirm-code :confirm-failure? true}}
          result (get-in (models/intro-step-back {:db db}) [:db :intro-wizard])]
      (is (= result  {:step :create-code :key-code nil :confirm-failure? false :weak-password? true})))))

(deftest intro-step-forward
  (testing "Forward from choose-key"
    (let [db {:intro-wizard {:step :choose-key}}
          ;; In this case intro-step-forward returns fx/merge result which is an fn
          ;; to be invoked on cofx
          result (get-in ((models/intro-step-forward {:db db}) {:db db}) [:db :intro-wizard])]
      (is (= result {:step :select-key-storage}))))

  (testing "Forward from generate-key"
    (let [db {:intro-wizard {:step :generate-key}}
          result ((models/intro-step-forward {:db db}) {:db db})]
      (is (= (select-keys (:db result) [:intro-wizard :node/on-ready]) {:intro-wizard {:step :generate-key :generating-keys? true}
                                                                        :node/on-ready :start-onboarding}))))

  (testing "Forward from create-code"
    (let [db {:intro-wizard {:step :create-code :key-code "qwerty"}}
          result (get-in ((models/intro-step-forward {:db db}) {:db db}) [:db :intro-wizard])]
      (is (= result {:step :confirm-code :key-code nil :stored-key-code "qwerty"}))))

  (testing "Forward from confirm-code (failure case)"
    (with-redefs [utils/vibrate (fn [] "vibrating")]
      (let [db {:intro-wizard {:step :confirm-code :key-code "abcdef" :encrypt-with-password? true :stored-key-code "qwerty"}}
            result (get-in ((models/intro-step-forward {:db db}) {:db db}) [:db :intro-wizard])]
        (is (= result {:step :confirm-code :key-code "abcdef" :confirm-failure? true
                       :encrypt-with-password? true
                       :stored-key-code "qwerty"}))))))

(deftest on-keys-generated
  (testing "Test merging of generated keys into app-db"
    (let [db {:intro-wizard {:step :generate-key :generating-keys true}}
          multiaccounts [{:id "0x01"}
                         {:id "0x02"}
                         {:id "0x03"}
                         {:id "0x04"}
                         {:id "0x05"}]
          result (get-in (models/on-keys-generated {:db db} {:multiaccounts multiaccounts}) [:db :intro-wizard])]
      (is (= result) {:step :choose-key :multiaccounts multiaccounts :selected-storage-type :default :selected-id (-> multiaccounts first :id)}))))

(deftest get-new-key-code
  (testing "Add new character to keycode"
    (is (= "abcd" (models/get-new-key-code "abc" "d" true))))
  (testing "Remove trailing character from keycode"
    (is (= "ab" (models/get-new-key-code "abc" :remove true)))))

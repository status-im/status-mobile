(ns status-im.multiaccounts.key-storage.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [clojure.string :as string]
            [status-im.multiaccounts.key-storage.core :as models]
            [status-im.utils.security :as security]))

(deftest move-keystore-checked
  (testing "Checks checkbox on-press"
    (let [res (models/move-keystore-checked {:db {}} true)]
      (is (= true (get-in res [:db :multiaccounts/key-storage :move-keystore-checked?]))))))

(deftest seed-phrase-input-changed
  (testing "nil seed phrase shape is invalid"
    (let [res (models/seed-phrase-input-changed {:db {}} (security/mask-data nil))]
      (is (get-in res [:db :multiaccounts/key-storage :seed-shape-invalid?]))))

  (let [sample-phrase "h h h h h h h h h h h H" ;; 12 characters
        res (models/seed-phrase-input-changed {:db {}} (security/mask-data sample-phrase))]
    (testing "Seed shape for 12 letter seed phrase is valid"
      (is (false? (get-in res [:db :multiaccounts/key-storage :seed-shape-invalid?]))))

    (testing "Seed words counted correctly"
      (is (= 12 (get-in res [:db :multiaccounts/key-storage :seed-word-count]))))

    (testing "Seed phrase is lowercased"
      (is (= (get-in res [:db :multiaccounts/key-storage :seed-phrase])
             (string/lower-case sample-phrase))))))

(def seed-key-uid-pair
  {:seed-phrase "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo"
   :key-uid "0x3831d0f22996a65970a214f0a94bfa9a63a21dac235d8dadb91be8e32e7d3ab7"})

(defn mock-import-mnemonic-fn [_ _ _]
  ;; return json with keyUid, the real world will have more info in the response
  (str "{\"keyUid\": \"" (:key-uid seed-key-uid-pair) "\"}"))

(deftest validate-seed-against-key-uid
  (testing "Success event is triggered if correct seed is entered for selected multiaccount (key-uid)"
    (models/validate-seed-against-key-uid
     {:import-mnemonic-fn mock-import-mnemonic-fn
      :on-success #(is true) ; this callback should be called
      :on-error #(is false)}
     {:seed-phrase (:seed-phrase seed-key-uid-pair)
      :key-uid (:key-uid seed-key-uid-pair)}))

  (testing "Error event is triggered if incorrect seed is entered for selected multiaccount"
    (models/validate-seed-against-key-uid
     {:import-mnemonic-fn mock-import-mnemonic-fn
      :on-success #(is false)
      :on-error #(is true)}
     {:seed-phrase (:seed-phrase seed-key-uid-pair)
      :key-uid "0xInvalid-Will-make-the-function-fail"})))

(deftest handle-multiaccount-import
  (testing "Sets correct state for Keycard onboarding after multiaccounts seeds are derived"
    (let [res (models/handle-multiaccount-import {:db {}} :passed-root-data :passed-derived-data)]
      (is (= :passed-root-data (get-in res [:db :intro-wizard :root-key])))
      (is (= :passed-derived-data (get-in res [:db :intro-wizard :derived])))
      (is (= :advanced (get-in res [:db :intro-wizard :selected-storage-type]))) ; :advanced storage type means Keycard
      (is (= :recovery (get-in res [:db :keycard :flow])))
      (is (get-in res [:db :keycard :from-key-storage-and-migration?])))))

(comment
  (security/safe-unmask-data (security/mask-data nil)))


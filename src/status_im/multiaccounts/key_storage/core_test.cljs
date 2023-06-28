(ns status-im.multiaccounts.key-storage.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.key-storage.core :as models]
            [utils.security.core :as security]))

(def seed-key-uid-pair
  {:seed-phrase "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo"
   :key-uid     "0x3831d0f22996a65970a214f0a94bfa9a63a21dac235d8dadb91be8e32e7d3ab7"})

(defn mock-import-mnemonic-fn
  [_ _ _]
  ;; return json with keyUid, the real world will have more info in the response
  (str "{\"keyUid\": \"" (:key-uid seed-key-uid-pair) "\"}"))

(deftest validate-seed-against-key-uid
  (testing "Success event is triggered if correct seed is entered for selected multiaccount (key-uid)"
    (models/validate-seed-against-key-uid
     {:import-mnemonic-fn mock-import-mnemonic-fn
      :on-success         #(is true) ; this callback should be called
      :on-error           #(is false)}
     {:seed-phrase (:seed-phrase seed-key-uid-pair)
      :key-uid     (:key-uid seed-key-uid-pair)}))

  (testing "Error event is triggered if incorrect seed is entered for selected multiaccount"
    (models/validate-seed-against-key-uid
     {:import-mnemonic-fn mock-import-mnemonic-fn
      :on-success         #(is false)
      :on-error           #(is true)}
     {:seed-phrase (:seed-phrase seed-key-uid-pair)
      :key-uid     "0xInvalid-Will-make-the-function-fail"})))

(deftest handle-multiaccount-import
  (testing "Sets correct state for Keycard onboarding after multiaccounts seeds are derived"
    (let [res (models/handle-multiaccount-import {:db {}} :passed-root-data :passed-derived-data)]
      (is (= :passed-root-data (get-in res [:db :intro-wizard :root-key])))
      (is (= :passed-derived-data (get-in res [:db :intro-wizard :derived])))
      (is (= :advanced (get-in res [:db :intro-wizard :selected-storage-type]))) ; :advanced storage type
                                                                                 ; means Keycard
      (is (= :recovery (get-in res [:db :keycard :flow])))
      (is (get-in res [:db :keycard :from-key-storage-and-migration?])))))

(comment
  (security/safe-unmask-data (security/mask-data nil)))


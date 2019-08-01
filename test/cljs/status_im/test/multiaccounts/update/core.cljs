(ns status-im.test.multiaccounts.update.core
  (:require [clojure.test :refer-macros [deftest is]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.multiaccounts.update.core :as multiaccounts.update]))

(deftest test-multiaccount-update
  (let [efx (multiaccounts.update/multiaccount-update {:db {:multiaccount {}}} nil {})
        json-rpc (into #{} (map :method (::json-rpc/call efx)))]
    (is (json-rpc "settings_saveConfig"))
    (is (= (get-in efx [:db :multiaccount]) {}))))

(deftest test-clean-seed-phrase
  (let [efx (multiaccounts.update/clean-seed-phrase {:db {:multiaccount {:mnemonic "lalalala"}}})
        json-rpc (into #{} (map :method (::json-rpc/call efx)))]
    (is (json-rpc "settings_saveConfig"))
    (is (= (get-in efx [:db :multiaccount]) {:seed-backed-up? true, :mnemonic nil}))))

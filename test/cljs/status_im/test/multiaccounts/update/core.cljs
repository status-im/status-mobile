(ns status-im.test.multiaccounts.update.core
  (:require [clojure.test :refer-macros [deftest is]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.multiaccounts.update.core :as multiaccounts.update]))

(deftest test-multiaccount-update
  ;;TODO this test case actually shows that we are doing a needless rpc call when
  ;;there is no changes, but it is an edge case that shouldn't really happen
  (let [efx (multiaccounts.update/multiaccount-update
             {:db {:multiaccount {:not-empty "would throw an error if was empty"}}}
             nil {})
        json-rpc (into #{} (map :method (::json-rpc/call efx)))]
    (is (json-rpc "settings_saveConfig"))
    (is (= (get-in efx [:db :multiaccount]) {:not-empty "would throw an error if was empty"}))))

(deftest test-clean-seed-phrase
  (let [efx (multiaccounts.update/clean-seed-phrase {:db {:multiaccount {:mnemonic "lalalala"}}})
        json-rpc (into #{} (map :method (::json-rpc/call efx)))]
    (is (json-rpc "settings_saveConfig"))
    (is (= (get-in efx [:db :multiaccount]) {:seed-backed-up? true, :mnemonic nil}))))

(ns status-im.test.transport.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.protocol.core :as protocol]
            [status-im.transport.core :as transport]))

(deftest init-whisper
  (let [cofx {:db {:account/account {:public-key "1"}
                   :semaphores #{}}}]
    (testing "it adds the discover filter"
      (is (= (:shh/add-discovery-filter (protocol/initialize-protocol "user-address" cofx)))))
    (testing "it restores the sym-keys"
      (is (= (:shh/restore-sym-keys (protocol/initialize-protocol  "user-address" cofx)))))
    (testing "custom mailservers"
      (let [ms-1            {:id "1"
                             :fleet :eth.beta
                             :name "name-1"
                             :address "address-1"
                             :password "password-1"}
            ms-2            {:id "2"
                             :fleet :eth.beta
                             :name "name-2"
                             :address "address-2"
                             :password "password-2"}
            ms-3            {:id "3"
                             :fleet :eth.test
                             :name "name-3"
                             :address "address-3"
                             :password "password-3"}
            expected-wnodes {:eth.beta {"1" (-> ms-1
                                                (dissoc :fleet)
                                                (assoc :user-defined true))
                                        "2" (-> ms-2
                                                (dissoc ms-2 :fleet)
                                                (assoc :user-defined true))}
                             :eth.test {"3" (-> ms-3
                                                (dissoc :fleet)
                                                (assoc :user-defined true))}}
            cofx-with-ms    (assoc cofx
                                   :data-store/mailservers
                                   [ms-1
                                    ms-2
                                    ms-3])]
        (is (= expected-wnodes
               (get-in
                (protocol/initialize-protocol "user-address" cofx-with-ms)
                [:db :inbox/wnodes])))))))

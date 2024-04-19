(ns legacy.status-im.multiaccounts.update.core-test
  (:require
    [clojure.test :refer-macros [deftest is testing]]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]))

(deftest test-multiaccount-update
  ;;TODO this test case actually shows that we are doing a needless rpc call when
  ;;there is no changes, but it is an edge case that shouldn't really happen
  (let [efx      (multiaccounts.update/multiaccount-update
                  {:db {:profile/profile {:not-empty "would throw an error if was empty"}}}
                  nil
                  nil
                  {})
        json-rpc (into #{} (map :method (:json-rpc/call efx)))]
    (is (json-rpc "settings_saveSetting"))
    (is (= (get-in efx [:db :profile/profile]) {:not-empty "would throw an error if was empty"}))))

(deftest test-clean-seed-phrase
  (let [efx      (multiaccounts.update/clean-seed-phrase
                  {:db {:profile/profile {:mnemonic "lalalala"}}}
                  {})
        json-rpc (into #{} (map :method (:json-rpc/call efx)))]
    (is (json-rpc "settings_saveSetting"))
    (is (nil? (get-in efx [:db :profile/profile :mnemonic])))))

(deftest test-update-multiaccount-account-name
  (let [cofx                             {:db {:profile/profile {:key-uid        1
                                                                 :name           "name"
                                                                 :preferred-name "preferred-name"
                                                                 :display-name   "display-name"}}}
        raw-multiaccounts-from-status-go [{:key-uid 1 :name "old-name"}]]
    (testing "wrong account"
      (is (nil? (multiaccounts.update/update-multiaccount-account-name cofx []))))
    (testing "name priority preferred-name > display-name > name"
      (let [new-account-name= (fn [efx new-name]
                                (-> efx
                                    :json-rpc/call
                                    first
                                    :params
                                    first
                                    :name
                                    (= new-name)))]
        (is (new-account-name=
             (multiaccounts.update/update-multiaccount-account-name
              cofx
              raw-multiaccounts-from-status-go)
             "preferred-name"))
        (is (new-account-name=
             (multiaccounts.update/update-multiaccount-account-name
              (update-in cofx [:db :profile/profile] dissoc :preferred-name)
              raw-multiaccounts-from-status-go)
             "display-name"))
        (is (new-account-name=
             (multiaccounts.update/update-multiaccount-account-name
              (update-in cofx [:db :profile/profile] dissoc :preferred-name :display-name)
              raw-multiaccounts-from-status-go)
             "name"))))))

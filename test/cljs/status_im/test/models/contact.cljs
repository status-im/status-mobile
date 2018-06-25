(ns status-im.test.models.contact
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.contact :as model]))

(deftest can-add-to-contact-test
  (testing "a user is already in contacts"
    (is (not (model/can-add-to-contacts? {:pending? false}))))
  (testing "a user is pending"
    (testing "a normal user"
      (is (model/can-add-to-contacts? {:pending? true})))
    (testing "a dapp"
      (is (not (model/can-add-to-contacts? {:pending? true
                                            :dapp?    true})))))
  (testing "the user is not in the contacts"
    (testing "a normal user"
      (is (model/can-add-to-contacts? {})))
    (testing "a dapp"
      (is (not (model/can-add-to-contacts? {:dapp? true}))))))

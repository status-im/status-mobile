(ns status-im.test.models.account
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.accounts.model :as accounts.db]))

(deftest logged-in-test
  (testing "account/account is defined"
    (is (accounts.db/logged-in? {:db {:account/account {}}})))
  (testing "account/account is not there"
    (is (not (accounts.db/logged-in? {:db {}})))))

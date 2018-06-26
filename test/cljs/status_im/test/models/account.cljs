(ns status-im.test.models.account
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.account :as model]))

(deftest logged-in-test
  (testing "account/account is defined"
    (is (model/logged-in? {:db {:account/account {}}})))
  (testing "account/account is not there"
    (is (not (model/logged-in? {:db {}})))))

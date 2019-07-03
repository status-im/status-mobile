(ns status-im.test.multiaccounts.model
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.model :as multiaccounts.model]))

(deftest logged-in-test
  (testing "multiaccount is defined"
    (is (multiaccounts.model/logged-in? {:db {:multiaccount {}}})))
  (testing "multiaccount is not there"
    (is (not (multiaccounts.model/logged-in? {:db {}})))))

(ns legacy.status-im.multiaccounts.model-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.multiaccounts.model :as multiaccounts.model]))

(deftest logged-in-test
  (testing "multiaccount is defined"
    (is (multiaccounts.model/logged-in? {:profile/profile {}})))
  (testing "multiaccount is not there"
    (is (not (multiaccounts.model/logged-in? {})))))

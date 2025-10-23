(ns tests.contract-test.core-test
  (:require
    [cljs.test :refer [deftest]]
    legacy.status-im.events
    legacy.status-im.subs.root
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]))

(deftest initialize-app-test
  (h/test-app-initialization))

(deftest create-account-test
  (h/test-account-creation))

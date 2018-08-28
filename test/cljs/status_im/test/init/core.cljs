(ns status-im.test.init.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.init.core :as init]))

(deftest initialize-account-db
  (testing "it preserves universal-links/url"
    (is (= "some-url" (get-in (init/initialize-account-db
                               "address"
                               {:db {:universal-links/url "some-url"}})
                              [:db :universal-links/url])))))

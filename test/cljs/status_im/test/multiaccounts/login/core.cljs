(ns status-im.test.multiaccounts.login.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.login.core :as login]
            [clojure.string :as string]))

(deftest initialize-multiaccount-db
  (testing "it preserves universal-links/url"
    (is (= "some-url" (get-in (login/initialize-multiaccount-db
                               {:db {:universal-links/url "some-url"}}
                               "address")
                              [:db :universal-links/url])))))

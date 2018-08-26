(ns status-im.test.init.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.init.core :as init]))

(deftest initialize-db
  (testing "it preserves universal-links/url"
    (is (= "some-url" (get-in (init/initialize-db {:db
                                                   {:universal-links/url "some-url"}})
                              [:db :universal-links/url])))))

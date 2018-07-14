(ns status-im.test.ui.screens.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.events :as events]))

(deftest initialize-db
  (testing "it preserves universal-links/url"
    (is (= "some-url" (get-in (events/initialize-db {:db
                                                     {:universal-links/url "some-url"}})
                              [:db :universal-links/url])))))

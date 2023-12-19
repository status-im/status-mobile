(ns status-im2.integration-test.core-test
  (:require
    [cljs.test :refer [deftest]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [legacy.status-im.utils.test :as utils.test]
    [re-frame.core :as rf]
    status-im2.events
    status-im2.navigation.core
    status-im2.subs.root
    [test-helpers.integration :as h]))

(deftest initialize-app-test
  (h/log-headline :initialize-app-test)
  (rf-test/run-test-async
   (utils.test/init!)
   (rf/dispatch [:app-started])
   (rf-test/wait-for
     ;; use initialize-view because it has the longest avg. time and
     ;; is dispatched by initialize-multiaccounts (last non-view event)
     [:profile/get-profiles-overview-success]
     (rf-test/wait-for
       [:font/init-font-file-for-initials-avatar]
       (h/assert-app-initialized)))))

(deftest create-account-test
  (h/log-headline :create-account-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (h/logout)
     (rf-test/wait-for [::logout/logout-method])))))

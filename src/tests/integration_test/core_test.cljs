(ns tests.integration-test.core-test
  (:require
    [cljs.test :refer [deftest]]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [promesa.core :as p]
    [re-frame.core :as rf]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.test-utils :as test-utils]))

(deftest initialize-app-test
  (h/integration-test ::initialize-app
    (fn []
      (p/do
        (test-utils/init!)
        (rf/dispatch [:app-started])
        ;; Use initialize-view because it has the longest avg. time and is
        ;; dispatched by initialize-multiaccounts (last non-view event).
        (h/wait-for [:profile/get-profiles-overview-success
                     :font/init-font-file-for-initials-avatar])
        (h/assert-app-initialized)))))

(deftest create-account-test
  (h/integration-test ::create-account
    (fn []
      (p/do
        (h/setup-app)
        (h/setup-account)
        (h/logout)
        (h/wait-for [::logout/logout-method])))))

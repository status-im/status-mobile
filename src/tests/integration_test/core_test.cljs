(ns tests.integration-test.core-test
  (:require
    [cljs.test :refer [deftest]]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [legacy.status-im.utils.test :as utils.test]
    [re-frame.core :as rf]
    [status-im.common.log :as log]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]))

(deftest initialize-app-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::initialize-app-test)
     ;; (log/setup "ERROR")
     (utils.test/init!)
     (rf/dispatch [:app-started])
     ;; Use initialize-view because it has the longest avg. time and is
     ;; dispatched by initialize-multiaccounts (last non-view event).
     (-> (h/wait-for [:profile/get-profiles-overview-success
                      :font/init-font-file-for-initials-avatar])
         (.then #(h/assert-app-initialized))))))

(deftest create-account-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::create-account-test)
     (-> (h/with-app-initialized)
         (.then h/with-account)
         (.then h/logout)
         (.then #(h/wait-for [::logout/logout-method]))))))

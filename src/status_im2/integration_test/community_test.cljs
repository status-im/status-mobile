(ns status-im2.integration-test.community-test
  (:require [cljs.test :refer [deftest]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [status-im.multiaccounts.logout.core :as logout]
            [status-im2.integration-test.constants :as constants]
            [test-helpers.integration :as h]))

(deftest create-community-test
  (h/log-headline :create-community-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch-sync [:legacy-only-for-e2e/open-create-community])
     (doseq [[k v] (dissoc constants/community :membership)]
       (rf/dispatch-sync [:status-im.communities.core/create-field k v]))
     (rf/dispatch [:status-im.communities.core/create-confirmation-pressed])
     (rf-test/wait-for
       [:status-im.communities.core/community-created]
       (h/assert-community-created)
       (h/logout)
       (rf-test/wait-for [::logout/logout-method]))))))

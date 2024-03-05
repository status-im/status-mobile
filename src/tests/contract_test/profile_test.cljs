(ns tests.contract-test.profile-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]))

(deftest profile-set-bio-contract
  (h/log-headline :contract/wakuext_setBio)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (contract-utils/call-rpc-endpoint
      {:rpc-endpoint "wakuext_setBio"
       :params       ["new bio"]
       :on-error     #(is (nil? %) "Set bio RPC call should have succeeded")})
     (h/logout)
     (rf-test/wait-for
       [::logout/logout-method])))))

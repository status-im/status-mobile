(ns tests.contract-test.profile-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [promesa.core :as promesa]
    [status-im.common.json-rpc.events :as rpc-events]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]))

(use-fixtures :each (h/fixture-session))

(deftest profile-set-bio-contract-test
  (h/test-async :contract/wakuext_setBio
    (fn []
      (-> (rpc-events/call-async "wakuext_setBio" false "new bio")
          (promesa/catch #(is (nil? %) "Set bio RPC call should have succeeded"))))))

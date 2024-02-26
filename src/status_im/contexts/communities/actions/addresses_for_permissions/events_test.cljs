(ns status-im.contexts.communities.actions.addresses-for-permissions.events-test
  (:require
    [cljs.test :refer [deftest is]]
    [status-im.contexts.communities.actions.addresses-for-permissions.events :as sut]))

(def community-id "0x1")

(deftest get-permissioned-balances-test
  (let [cofx {:db {}}]
    (is (match? {:fx [[:json-rpc/call
                       [{:method     :wakuext_getCommunityPermissionedBalances
                         :params     [{:communityId community-id}]
                         :on-success [:communities/get-permissioned-balances-success community-id]
                         :on-error   fn?}]]]}
                (sut/get-permissioned-balances cofx [community-id])))))

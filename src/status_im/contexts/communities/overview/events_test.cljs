(ns status-im.contexts.communities.overview.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [native-module.core :as native-module]
            [status-im.contexts.communities.overview.events :as sut]))

(def password (native-module/sha3 "password123"))
(def community-id "0x99")
(def account-pub-key "0x1")

(deftest request-to-join-test
  (let [cofx     {:db {:profile/profile {:public-key account-pub-key}}}
        expected {:fx [[:json-rpc/call
                        [{:method     "wakuext_generateJoiningCommunityRequestsForSigning"
                          :params     [account-pub-key community-id []]
                          :on-success [:communities/sign-data community-id password]
                          :on-error   [:communities/requested-to-join-error community-id]}]]]}]
    (is (match? expected
                (sut/request-to-join cofx
                                     [{:community-id community-id
                                       :password     password}])))))

(deftest sign-data-test
  (let [cofx                {:db {}}
        sign-params         [{:data "123" :account account-pub-key}
                             {:data "456" :account "0x2"}]
        addresses-to-reveal [account-pub-key "0x2"]
        expected            {:fx
                             [[:json-rpc/call
                               [{:method     "wakuext_signData"
                                 :params     [[{:data "123" :account account-pub-key :password password}
                                               {:data "456" :account "0x2" :password password}]]
                                 :on-success [:communities/request-to-join-with-signatures
                                              community-id addresses-to-reveal]
                                 :on-error   [:communities/requested-to-join-error community-id]}]]]}]
    (is (match? expected
                (sut/sign-data cofx [community-id password sign-params])))))

(deftest request-to-join-with-signatures-test
  (let [cofx                    {:db {}}
        addresses-to-reveal     [account-pub-key "0x2"]
        share-future-addresses? true
        signatures              ["11111" "222222"]
        expected                {:fx [[:json-rpc/call
                                       [{:method      "wakuext_requestToJoinCommunity"
                                         :params      [{:communityId          community-id
                                                        :signatures           signatures
                                                        :addressesToReveal    addresses-to-reveal
                                                        :shareFutureAddresses share-future-addresses?
                                                        :airdropAddress       "0x1"}]
                                         :js-response true
                                         :on-success  [:communities/requested-to-join]
                                         :on-error    [:communities/requested-to-join-error
                                                       community-id]}]]]}]
    (is (match? expected
                (sut/request-to-join-with-signatures cofx
                                                     [community-id addresses-to-reveal signatures
                                                      share-future-addresses?])))))

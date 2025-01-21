(ns status-im.contexts.profile.edit.header.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.common.avatar-picture-picker.view :as profile-picture-picker]
            [status-im.contexts.profile.edit.header.events :as sut]))

(deftest edit-picture-test
  (let [picture  "new-picture"
        key-uid  "key-uid"
        cofx     {:db {:profile/profile {:key-uid key-uid}}}
        expected {:fx [[:json-rpc/call
                        [{:method     "multiaccounts_storeIdentityImage"
                          :params     [key-uid picture 0 0 profile-picture-picker/crop-size
                                       profile-picture-picker/crop-size]
                          :on-success [:profile/edit-profile-picture-success {:show-toast? true}]}]]]}]
    (is (match? expected
                (sut/edit-profile-picture cofx [{:picture picture}])))))

(deftest delete-picture-test
  (let [key-uid  "key-uid"
        cofx     {:db {:profile/profile {:key-uid key-uid}}}
        expected {:fx [[:json-rpc/call
                        [{:method     "multiaccounts_deleteIdentityImage"
                          :params     [key-uid]
                          :on-success [:profile/delete-profile-picture-success {:show-toast? true}]}]]]}]
    (is (match? expected
                (sut/delete-profile-picture cofx [{}])))))

(ns status-im.contexts.profile.edit.header.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.common.profile-picture-picker.view :as photo-picker]
            [status-im.contexts.profile.edit.header.events :as sut]))

(deftest edit-picture-test
  (let [picture  "new-picture"
        key-uid  "key-uid"
        cofx     {:db {:profile/profile {:key-uid key-uid}}}
        expected {:json-rpc/call
                  [{:method     "multiaccounts_storeIdentityImage"
                    :params     [key-uid picture 0 0 photo-picker/crop-size photo-picker/crop-size]
                    :on-success fn?}]}]
    (is (match? expected
                (sut/edit-profile-picture cofx [picture])))))

(deftest delete--picture-test
  (let [key-uid  "key-uid"
        cofx     {:db {:profile/profile {:key-uid key-uid}}}
        expected {:json-rpc/call
                  [{:method     "multiaccounts_deleteIdentityImage"
                    :params     [key-uid]
                    :on-success fn?}]}]
    (is (match? expected
                (sut/delete-profile-picture cofx)))))

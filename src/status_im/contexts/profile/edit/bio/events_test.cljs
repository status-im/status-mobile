(ns status-im.contexts.profile.edit.bio.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.profile.edit.bio.events :as sut]))

(deftest edit-bio-test
  (let [new-bio  "New Bio text"
        cofx     {:db {:profile/profile {:bio "Old Bio text"}}}
        expected {:db {:profile/profile {:bio new-bio}}
                  :fx [[:json-rpc/call
                        [{:method     "wakuext_setBio"
                          :params     [new-bio]
                          :on-success [:profile/edit-profile-bio-success]}]]]}]
    (is (match? expected
                (sut/edit-profile-bio cofx [new-bio])))))

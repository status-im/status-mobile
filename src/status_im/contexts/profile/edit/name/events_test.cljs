(ns status-im.contexts.profile.edit.name.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.profile.edit.name.events :as sut]))

(deftest edit-name-test
  (let [new-name "John Doe"
        cofx     {:db {:profile/profile {:display-name "Old name"}}}
        expected {:fx [[:json-rpc/call
                        [{:method     "wakuext_setDisplayName"
                          :params     [name]
                          :on-success [:profile/edit-profile-name-success new-name true true]}]]]}]
    (is (match? expected
                (sut/edit-profile-name cofx [{:display-name new-name}])))))

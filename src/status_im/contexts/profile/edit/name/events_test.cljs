(ns status-im.contexts.profile.edit.name.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.profile.edit.name.events :as sut]))

(def profile-name "John Doe")

(deftest edit-name-test
  (let [cofx     {:db {:profile/profile {:display-name "test"}}}
        expected {:db            {:profile/profile {:display-name profile-name}}
                  :json-rpc/call [{:method     "wakuext_setDisplayName"
                                   :params     [profile-name]
                                   :on-success fn?}]}]
    (is (match? expected
                (sut/edit-profile-name cofx [profile-name])))))

(ns status-im.contexts.profile.edit.ens.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.profile.edit.ens.events :as sut]))

(deftest edit-name-test
  (let [new-name "test.eth"
        cofx     {:db {:ens/names {"test.eth"  {:name "test.eth"}
                                   "test2.eth" {:name "test2.eth"}}}}
        expected {:db {:ens/names {"test2.eth" {:name "test2.eth"}}}}]
    (is (match? expected
                (sut/remove-ens-name cofx [new-name])))))

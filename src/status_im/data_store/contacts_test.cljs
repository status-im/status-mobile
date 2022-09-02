(ns status-im.data-store.contacts-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.contacts :as c]))

(deftest contact<-rpc
  (let [contact {:id "pk"
                 :address "address"
                 :name "name"
                 :identicon "identicon"
                 :lastUpdated 1}
        expected-contact {:public-key "pk"
                          :address "address"
                          :mutual? nil
                          :name "name"
                          :identicon "identicon"
                          :last-updated 1}]
    (testing "<-rpc"
      (is (= expected-contact (c/<-rpc contact))))))

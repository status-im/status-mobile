(ns status-im.data-store.contacts-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.contacts :as c]))

(deftest contact->rpc
  (let [contact {:public-key "pk"
                 :address "address"
                 :name "name"
                 :identicon "identicon"
                 :last-updated 1
                 :system-tags #{:a :b}}
        expected-contact {:id "pk"
                          :address "address"
                          :name "name"
                          :identicon "identicon"

                          :lastUpdated 1
                          :systemTags #{":a" ":b"}}]
    (testing "->rpc"
      (is (= expected-contact (update
                               (c/->rpc contact)
                               :systemTags
                               #(into #{} %)))))))

(deftest contact<-rpc
  (let [contact {:id "pk"
                 :address "address"
                 :name "name"
                 :identicon "identicon"
                 :lastUpdated 1
                 :systemTags [":a" ":b"]}
        expected-contact {:public-key "pk"
                          :address "address"
                          :name "name"
                          :identicon "identicon"
                          :last-updated 1
                          :system-tags #{:a :b}}]
    (testing "<-rpc"
      (is (= expected-contact (c/<-rpc contact))))))

(ns status-im.test.data-store.contacts
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.contacts :as c]))

(deftest contact->rpc
  (let [contact {:public-key "pk"
                 :address "address"
                 :name "name"
                 :photo-path "photo-path"
                 :tribute-to-talk "tribute-to-talk"
                 :device-info {"1" {:id "1"
                                    :timestamp 1
                                    :fcm-token "1"}
                               "2" {:id "2"
                                    :timestamp 2
                                    :fcm-token 3}}
                 :last-updated 1
                 :system-tags #{:a :b}}
        expected-contact {:id "pk"
                          :address "address"
                          :name "name"
                          :photoPath "photo-path"
                          :tributeToTalk "[\"~#'\",\"tribute-to-talk\"]"
                          :deviceInfo [{:installationId "1"
                                        :timestamp 1
                                        :fcmToken "1"}
                                       {:installationId "2"
                                        :timestamp 2
                                        :fcmToken 3}]
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
                 :photoPath "photo-path"
                 :tributeToTalk "[\"~#'\",\"tribute-to-talk\"]"

                 :deviceInfo [{:installationId "1"
                               :timestamp 1
                               :fcmToken "1"}
                              {:installationId "2"
                               :timestamp 2
                               :fcmToken 3}]
                 :lastUpdated 1
                 :systemTags [":a" ":b"]}
        expected-contact {:public-key "pk"
                          :address "address"
                          :name "name"
                          :photo-path "photo-path"
                          :tribute-to-talk "tribute-to-talk"
                          :device-info {"1" {:id "1"
                                             :timestamp 1
                                             :fcm-token "1"}
                                        "2" {:id "2"
                                             :timestamp 2
                                             :fcm-token 3}}
                          :last-updated 1
                          :system-tags #{:a :b}}]
    (testing "<-rpc"
      (is (= expected-contact (c/<-rpc contact))))))


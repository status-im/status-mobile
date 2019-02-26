(ns status-im.test.contacts.device-info
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.contact.device-info :as device-info]))

(def device-list
  {"2" {:installation-id "2"
        :fcm-token "token-2"
        :enabled? true}
   "3" {:installation-id "3"
        :fcm-token "token-3"
        :enabled? false}
   "4" {:installation-id "4"
        :enabled? true}
   "5" {:installation-id "5"
        :fcm-token "token-5"
        :enabled? true}})

(deftest device-info-all
  (testing "no devices"
    (is (= [{:id "1"
             :fcm-token "token-1"}]
           (device-info/all {:db {:account/account {:installation-id "1"}
                                  :notifications   {:fcm-token "token-1"}}})))

    (testing "some devices"
      (is (= [{:id "1"
               :fcm-token "token-1"}
              {:id "2"
               :fcm-token "token-2"}
              {:id "5"
               :fcm-token "token-5"}]
             (device-info/all {:db {:account/account {:installation-id "1"}
                                    :pairing/installations device-list
                                    :notifications   {:fcm-token "token-1"}}}))))))

(ns status-im.test.chat.events
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.chat.events :as chat-events]))

(deftest show-profile-test
  (testing "Dafault behaviour: navigate to profile"
    (let [{:keys [db]} (chat-events/show-profile
                        "a"
                        {:db {:navigation-stack '(:home)}})]
      (is (= "a" (:contacts/identity db))))))

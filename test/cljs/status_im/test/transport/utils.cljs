(ns status-im.test.transport.utils
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.fx :as fx]
            [status-im.transport.utils :as transport]))

(deftest test-message-id
  (testing "test"
    (let [pk "0x03d0370306168850aa1f06a2f22c9a756c7dd00e35dd797fcdf351e53ff6ae7b9f"
          payload "0x74657374"
          expected-message-id "0x642b7f39873aab69d5aee686f4ed0ca02f82e025242ea57569a70640a94aea34"]
      (is (= expected-message-id (transport/message-id pk payload))))))

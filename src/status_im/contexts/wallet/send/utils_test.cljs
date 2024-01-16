(ns status-im.contexts.wallet.send.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.send.utils :as utils]))

(deftest test-amount-in-hex
  (testing "Test amount-in-hex function"
    (let [amount  1
          decimal 18]
      (is (= (utils/amount-in-hex amount decimal)
             "0xde0b6b3a7640000")))))

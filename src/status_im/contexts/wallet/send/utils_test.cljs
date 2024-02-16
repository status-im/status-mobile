(ns status-im.contexts.wallet.send.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.send.utils :as utils]))

(deftest test-amount-in-hex
  (testing "Test amount-in-hex function"
    (let [amount  1
          decimal 18]
      (is (= (utils/amount-in-hex amount decimal)
             "0xde0b6b3a7640000")))))

(def multichain-transacation
  {:id     61
   :hashes {:5   ["0x5"]
            :420 ["0x12" "0x11"]}})

(deftest test-map-multitransaction-by-ids
  (testing "test map-multitransaction-by-ids formats to right data structure"
    (let [{:keys [id hashes]} multichain-transacation]
      (is (= (utils/map-multitransaction-by-ids id hashes)
             {"0x5"  {:status   :pending
                      :id       61
                      :chain-id :5}
              "0x12" {:status   :pending
                      :id       61
                      :chain-id :420}
              "0x11" {:status   :pending
                      :id       61
                      :chain-id :420}})))))

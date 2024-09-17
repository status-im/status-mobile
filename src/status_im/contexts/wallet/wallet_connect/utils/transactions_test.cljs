(ns status-im.contexts.wallet.wallet-connect.utils.transactions-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.wallet-connect.utils.transactions :as sut]
    [utils.transforms :as transforms]))

(deftest strip-hex-prefix-test
  (testing "passed value with extra 0 hex prefix"
    (is (= (sut/strip-hex-prefix "0x0123")
           "0x123")))

  (testing "passed empty 0x value"
    (is (= (sut/strip-hex-prefix "0x")
           "0x0")))

  (testing "passed value without 0 in prefix"
    (is (= (sut/strip-hex-prefix "0x123")
           "0x123"))))

(deftest format-tx-hex-values-test
  (testing "applies f only on the tx keys that are hex numbers"
    (let [tx          {:to    "0x0123"
                       :from  "0x0456"
                       :value "0x0fff"
                       :gas   "0x01"}
          expected-tx {:to    "0x0123"
                       :from  "0x0456"
                       :value "0xfff"
                       :gas   "0x1"}]
      (is (= (sut/format-tx-hex-values tx sut/strip-hex-prefix)
             expected-tx)))))

(cljs.test/test-var #'format-tx-hex-values-test)

(deftest prepare-transaction-for-rpc-test
  (testing "original transaction nonce is removed"
    (let [tx {:to    "0x123"
              :from  "0x456"
              :value "0xfff"
              :nonce "0x1"}]
      (is (-> (sut/prepare-transaction-for-rpc tx)
              (transforms/json->clj)
              :nonce
              nil?))))
  (testing "transaction hex numbers are formatted"
    (let [tx {:to    "0x123"
              :from  "0x456"
              :value "0x0fff"
              :nonce "0x1"}]
      (is (-> (sut/prepare-transaction-for-rpc tx)
              (transforms/json->clj)
              :value
              (= "0xfff"))))))

(deftest beautify-transaction-test
  (testing "hex number values are converted to utf-8"
    (let [tx {:to    "0x123"
              :from  "0x456"
              :value "0xfff"}]
      (is (-> (sut/beautify-transaction tx)
              (transforms/json->clj)
              :value
              (= 4095))))))

(cljs.test/test-var #'beautify-transaction-test)

(deftest gwei->hex-test
  (testing "gwei amount is converted to wei as hex"
    (is (-> (sut/gwei->hex "1000000000")
            (= "0xde0b6b3a7640000")))))

(deftest dynamic-fee-tx?-test
  (testing "correctly asserts tx as dynamic"
    (is (-> {:to                   "0x123"
             :from                 "0x123"
             :value                "0x123"
             :maxFeePerGas         "0x123"
             :maxPriorityFeePerGas "0x1"}
            sut/dynamic-fee-tx?)))

  (testing "correnctly asserts tx as not dynamic (legacy)"
    (is (-> {:to    "0x123"
             :from  "0x123"
             :value "0x123"}
            sut/dynamic-fee-tx?
            not)))

  (testing "asserts tx as not dynamic (legacy) when required keys are only partially present"
    (is (-> {:to           "0x123"
             :from         "0x123"
             :value        "0x123"
             :maxFeePerGas "0x123"}
            sut/dynamic-fee-tx?
            not))))

(deftest prepare-transaction-fees-test
  (let [suggested-fees {:eip1559Enabled       true
                        :maxFeePerGasLow      1
                        :maxFeePerGasMedium   2
                        :maxFeePerGasHigh     3
                        :maxPriorityFeePerGas 100}]

    (testing "correctly prepares eip1559 gas values"
      (let [tx          {:to    "0x123"
                         :from  "0x123"
                         :value "0x123"}
            tx-priority :high]
        (is (= (sut/prepare-transaction-fees tx
                                             tx-priority
                                             suggested-fees)
               {:to                   (:to tx)
                :from                 (:from tx)
                :value                (:value tx)
                :maxFeePerGas         "0xb2d05e00"
                :maxPriorityFeePerGas "0x174876e800"}))))

    (testing "renames gasLimit key to gas and removed gasPrice"
      (let [gasLimit    "0x1234"
            prepared-tx (sut/prepare-transaction-fees {:to       "0x123"
                                                       :from     "0x123"
                                                       :value    "0x123"
                                                       :gasPrice "0x123"
                                                       :gasLimit gasLimit}
                                                      :high
                                                      suggested-fees)]
        (is (and (= (:gas prepared-tx) gasLimit)
                 (nil? (:gasLimit prepared-tx))
                 (nil? (:gasPrice prepared-tx))))))

    (testing "returns original tx for non-dynamic transactions"
      (let [tx             {:to       "0x123"
                            :from     "0x123"
                            :value    "0x123"
                            :gasPrice "0x123"}
            suggested-fees (assoc suggested-fees :eip1559Enabled false)]
        (is (= (sut/prepare-transaction-fees tx :high suggested-fees)
               tx))))

    (testing "throws a schema exception due wrong arguments"
      (let [tx                   {:to    "0x123"
                                  :from  "0x123"
                                  :value "0x123"}
            wrong-tx-priority    :wrong
            wrong-suggested-fees {}]
        (is (thrown? js/Error
                     (sut/prepare-transaction-fees tx
                                                   wrong-tx-priority
                                                   wrong-suggested-fees)))))))

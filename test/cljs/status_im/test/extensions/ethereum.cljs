(ns status-im.test.extensions.ethereum
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.extensions.ethereum :as ethereum]))

; ethereum/logs

(deftest test-parse-topic-events
  (testing "topic parsing check - events"
    (is (= ["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
            "0x29f31b934498b0deabbe211b01cc30eee6475abf0f0d553e7eb8ca71deeb24b3"]
           (ethereum/generate-topic [{:event "Transfer" :params ["address" "address" "uint256"]}
                                     {:event "drawBet" :params ["uint256" "uint8" "int8" "address" "uint256"]}])))))

(deftest test-parse-topic-indexed-values-address
  (testing "topic parsing check - indexed values address"
    (is (= ["0x000000000000000000000000299b18709d4aacbda99048721448f65893a0c73a"
            "0x00000000000000000000000094eaa5fa6b313968b2abd6da375ef28077d95d53"]
           (ethereum/generate-topic {:type "address" :values ["0x299b18709d4aacbda99048721448f65893a0c73a" "0x94eaa5fa6b313968b2abd6da375ef28077d95d53"]})))))

(deftest test-parse-topic-indexed-values-uint256
  (testing "topic parsing check - indexed values uint256"
    (is (= ["0x00000000000000000000000000000000000000000000000000000000000003fd"
            "0x0000000000000000000000000000000000000000000000000000000000000078"]
           (ethereum/generate-topic {:type "uint256" :values ["0x3fd" "120"]})))))

(deftest test-parse-topic-indexed-values-direct
  (testing "topic parsing check - indexed values direct"
    (is (= ["0x00000000000000000000000000000000000000000000000000000000000003fd"
            "0x0000000000000000000000000000000000000000000000000000000000000078"]
           (ethereum/generate-topic ["0x00000000000000000000000000000000000000000000000000000000000003fd"
                                     "0x0000000000000000000000000000000000000000000000000000000000000078"])))))

(deftest test-parse-topic-indexed-nil
  (testing "topic parsing check - nil"
    (is (= ["0x00000000000000000000000000000000000000000000000000000000000003fd"
            nil]
           (ethereum/generate-topic ["0x00000000000000000000000000000000000000000000000000000000000003fd"
                                     nil])))))

(deftest test-ensure-hex-bn-nonnumber
  (is (= "latest" (ethereum/ensure-hex-bn "latest"))))

(deftest test-ensure-hex-bn-int
  (is (= "0xa" (ethereum/ensure-hex-bn "10"))))

(deftest test-ensure-hex-bn-hex
  (is (= "0xf" (ethereum/ensure-hex-bn "0xf"))))

(deftest test-ensure-hex-bn-nil
  (is (= nil (ethereum/ensure-hex-bn nil))))

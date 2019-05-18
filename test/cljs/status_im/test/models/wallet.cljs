(ns status-im.test.models.wallet
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]))

(deftest valid-min-gas-price-test
  (testing "not an number"
    (is (= :invalid-number (wallet/invalid-send-parameter? :gas-price nil))))
  (testing "a number less than the minimum"
    (is (= :not-enough-wei (wallet/invalid-send-parameter? :gas-price (money/bignumber "0.0000000001")))))
  (testing "a number greater than the mininum"
    (is (not (wallet/invalid-send-parameter? :gas-price 3))))
  (testing "the minimum"
    (is (not (wallet/invalid-send-parameter? :gas-price (money/bignumber "0.000000001"))))))

(deftest valid-gas
  (testing "not an number"
    (is (= :invalid-number (wallet/invalid-send-parameter? :gas nil))))
  (testing "0"
    (is (= :invalid-number (wallet/invalid-send-parameter? :gas 0))))
  (testing "a number"
    (is (not (wallet/invalid-send-parameter? :gas 1)))))

(deftest build-edit-test
  (testing "an invalid edit"
    (let [actual (-> {}
                     (wallet/build-edit :gas "invalid")
                     (wallet/build-edit :gas-price "0.00000000001"))]
      (testing "it marks gas-price as invalid"
        (is (get-in actual [:gas-price :invalid?])))
      (testing "it does not change value"
        (is (= "0.00000000001" (get-in actual [:gas-price :value]))))
      (testing "it marks gas as invalid"
        (is (get-in actual [:gas :invalid?])))
      (testing "it does not change gas value"
        (is (= "invalid" (get-in actual [:gas :value]))))
      (testing "it sets max-fee to 0"
        (is (= "0" (:max-fee actual))))))
  (testing "gas price in wei should be round"
    (let [actual (-> {}
                     (wallet/build-edit :gas "21000")
                     (wallet/build-edit :gas-price "0.0000000023"))]
      (is (get-in actual [:gas-price :invalid?]))))
  (testing "an valid edit"
    (let [actual (-> {}
                     (wallet/build-edit :gas "21000")
                     (wallet/build-edit :gas-price "10"))]
      (testing "it does not mark gas-price as invalid"
        (is (not (get-in actual [:gas-price :invalid?]))))
      (testing "it sets the value in number for gas-price, in gwei"
        (is (= "10000000000" (str (get-in actual [:gas-price :value-number])))))
      (testing "it does not mark gas as invalid"
        (is (not (get-in actual [:gas :invalid?]))))
      (testing "it sets the value in number for gasi"
        (is (= "21000" (str (get-in actual [:gas :value-number])))))
      (testing "it calculates max-fee"
        (is (= "0.00021" (:max-fee actual)))))))

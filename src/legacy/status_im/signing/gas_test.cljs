(ns legacy.status-im.signing.gas-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.signing.gas :as signing.gas]))

(deftest build-edit-test
  (testing "an invalid edit"
    (let [actual (-> {}
                     (signing.gas/build-edit :gas "invalid")
                     (signing.gas/build-edit :gasPrice "0.00000000001"))]
      (testing "it marks gasPrice as invalid"
        (is (get-in actual [:gasPrice :error])))
      (testing "it does not change value"
        (is (= "0.00000000001" (get-in actual [:gasPrice :value]))))
      (testing "it marks gas as invalid"
        (is (get-in actual [:gas :error])))
      (testing "it does not change gas value"
        (is (= "invalid" (get-in actual [:gas :value]))))
      (testing "it sets max-fee to 0"
        (is (= "0" (:max-fee actual))))))
  (testing "gas price in wei should be round"
    (let [actual (-> {}
                     (signing.gas/build-edit :gas "21000")
                     (signing.gas/build-edit :gasPrice "0.0000000023"))]
      (is (get-in actual [:gasPrice :error]))))
  (testing "an valid edit"
    (let [actual (-> {}
                     (signing.gas/build-edit :gas "21000")
                     (signing.gas/build-edit :gasPrice "10"))]
      (testing "it does not mark gas-price as invalid"
        (is (not (get-in actual [:gasPrice :error]))))
      (testing "it sets the value in number for gas-price, in gwei"
        (is (= "10000000000" (str (get-in actual [:gasPrice :value-number])))))
      (testing "it does not mark gas as invalid"
        (is (not (get-in actual [:gas :error]))))
      (testing "it sets the value in number for gasi"
        (is (= "21000" (str (get-in actual [:gas :value-number])))))
      (testing "it calculates max-fee"
        (is (= "0.00021" (:max-fee actual)))))))

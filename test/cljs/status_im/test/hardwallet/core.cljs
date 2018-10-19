(ns status-im.test.hardwallet.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.hardwallet.core :as hardwallet]))

(deftest process-pin-input
  (testing "start entering PIN"
    (is (= {:db {:hardwallet {:pin {:original     [1]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :original}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     []
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}
                                         1
                                         :original))))
  (testing "first 6 numbers entered"
    (is (= {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :confirmation}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1 2 3 4 5]
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}
                                         6
                                         :original))))
  (testing "confirmation entered"
    (is (= {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                    :confirmation [1 2 3 4 5 6]
                                    :enter-step   :confirmation
                                    :status       :validating}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation [1 2 3 4 5]
                                                                  :enter-step   :confirmation}}}}
                                         6
                                         :confirmation))))
  (testing "confirmation doesn't match"
    (is (= {:db {:hardwallet {:pin {:original     []
                                    :confirmation []
                                    :enter-step   :original
                                    :error        :t/pin-mismatch
                                    :status       :error}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation [1 2 3 4 5]
                                                                  :enter-step   :confirmation}}}}
                                         7
                                         :confirmation)))))

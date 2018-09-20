(ns status-im.test.hardwallet.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.hardwallet.core :as hardwallet]))

(deftest process-pin-input
  (testing "start entering PIN"
    (is (= {:db {:hardwallet {:pin {:original     [1]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :original}}}}
           (hardwallet/process-pin-input 1
                                         :original
                                         {:db {:hardwallet {:pin {:original     []
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}))))
  (testing "first 6 numbers entered"
    (is (= {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :confirmation}}}}
           (hardwallet/process-pin-input 6
                                         :original
                                         {:db {:hardwallet {:pin {:original     [1 2 3 4 5]
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}))))
  (testing "confirmation entered"
    (is (= {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                    :confirmation [1 2 3 4 5 6]
                                    :enter-step   :confirmation
                                    :status       :validating}}}}
           (hardwallet/process-pin-input 6
                                         :confirmation
                                         {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation [1 2 3 4 5]
                                                                  :enter-step   :confirmation}}}}))))
  (testing "confirmation doesn't match"
    (is (= {:db {:hardwallet {:pin {:original     []
                                    :confirmation []
                                    :enter-step   :original
                                    :error        :t/pin-mismatch
                                    :status       :error}}}}
           (hardwallet/process-pin-input 7
                                         :confirmation
                                         {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation [1 2 3 4 5]
                                                                  :enter-step   :confirmation}}}})))))

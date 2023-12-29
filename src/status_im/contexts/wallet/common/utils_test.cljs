(ns status-im.contexts.wallet.common.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.common.utils :as utils]))

(deftest test-get-wallet-qr
  (testing "Test get-wallet-qr function"
    (let [wallet-multichain  {:wallet-type       :wallet-multichain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}
          wallet-singlechain {:wallet-type       :wallet-singlechain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}]

      (is (= (utils/get-wallet-qr wallet-multichain)
             "eth:opt:x000"))

      (is (= (utils/get-wallet-qr wallet-singlechain)
             "x000")))))

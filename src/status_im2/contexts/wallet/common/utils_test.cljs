(ns status-im2.contexts.wallet.common.utils-test
  (:require [clojure.test :refer :all]
            [status-im2.contexts.wallet.common.utils :as utils]))

(defn test-get-wallet-qr
  (testing "Test get-wallet-qr function"
           (let [wallet-multichain {:wallet-type :wallet-multichain
                                    :selected-networks [:ethereum :optimism]
                                    :address "x000"}
                 wallet-singlechain {:wallet-type :wallet-singlechain
                                     :selected-networks [:ethereum :optimism]
                                     :address "x000"}]

             (is (= (utils/get-wallet-qr wallet-multichain)
                    "eth:opt:x000"))

             (is (= (utils/get-wallet-qr wallet-singlechain)
                    "x000")))))

(ns status-im.wallet.transactions-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.ethereum.transactions.core :as transactions]))

(deftest get-transaction-details-url
  (is (= "https://etherscan.io/tx/asdfasdf"
         (transactions/get-transaction-details-url 1 "asdfasdf")))
  (is (= "https://rinkeby.etherscan.io/tx/asdfasdfg"
         (transactions/get-transaction-details-url 4 "asdfasdfg")))
  (is (= "https://ropsten.etherscan.io/tx/asdfasdfgg"
         (transactions/get-transaction-details-url 3 "asdfasdfgg")))
  (is (nil? (transactions/get-transaction-details-url 7787878 "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url nil "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url 676868 1))))

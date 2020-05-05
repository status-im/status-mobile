(ns status-im.wallet.transactions-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.ethereum.transactions.core :as transactions]))

(deftest get-transaction-details-url
  (is (= "https://etherscan.io/tx/asdfasdf"
         (transactions/get-transaction-details-url :mainnet "asdfasdf")))
  (is (= "https://rinkeby.etherscan.io/tx/asdfasdfg"
         (transactions/get-transaction-details-url :rinkeby "asdfasdfg")))
  (is (= "https://ropsten.etherscan.io/tx/asdfasdfgg"
         (transactions/get-transaction-details-url :testnet "asdfasdfgg")))
  (is (nil? (transactions/get-transaction-details-url :not-a-net "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url nil "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url :asdf 1))))

(ns legacy.status-im.wallet.transactions-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.ethereum.transactions.core :as transactions]))

(deftest get-transaction-details-url
  (is (= "https://etherscan.io/tx/asdfasdf"
         (transactions/get-transaction-details-url 1 "asdfasdf")))
  (is (nil? (transactions/get-transaction-details-url 7787878 "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url nil "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url 676868 1))))

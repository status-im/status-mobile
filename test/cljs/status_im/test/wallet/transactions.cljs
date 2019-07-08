(ns status-im.test.wallet.transactions
  (:require [cljs.test :refer-macros [deftest is]]
            [goog.Uri :as goog-uri]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.utils.http :as http]))

(defn- uri-query-data [uri]
  (let [uri' (goog-uri/parse uri)
        accum (atom {})]
    (.forEach (.getQueryData uri')
              #(swap! accum assoc (keyword %2) %1))
    {:scheme (.getScheme uri')
     :domain (.getDomain uri')
     :path (.getPath uri')
     :query @accum}))

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

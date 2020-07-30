(ns status-im.router.core-test
  (:require [status-im.router.core :as router]
            [cljs.test :refer  [deftest are] :include-macros true]))

(def public-key "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")

(deftest parse-uris
  (are [uri expected] (= (router/match-uri uri) {:handler (first expected)
                                                 :route-params (second expected)
                                                 :uri uri})

    "status-im://status" [:public-chat {:chat-id "status"}]

    "status-im://u/statuse2e" [:user {:user-id "statuse2e"}]

    (str "status-im://user/" public-key) [:user {:user-id public-key}]

    "status-im://b/www.cryptokitties.co" [:browser {:domain "www.cryptokitties.c"}]

    "https://join.status.im/status" [:public-chat {:chat-id "status"}]

    "https://join.status.im/u/statuse2e" [:user {:user-id "statuse2e"}]

    (str "https://join.status.im/user/" public-key) [:user {:user-id public-key}]

    ;; Last char removed by: https://github.com/juxt/bidi/issues/104
    "https://join.status.im/b/www.cryptokitties.co" [:browser {:domain "www.cryptokitties.c"}]

    "https://join.status.im/b/https://www.google.com/" [:browser {:domain "https://www.google.co"}]

    "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" [:ethereum {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

    "ethereum:pay-0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" [:ethereum {:prefix  "pay"
                                                                          :address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]
    "ethereum:foo-0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" [:ethereum {:prefix  "foo"
                                                                          :address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

    ;; FIXME: Should handle only first line
    "ethereum:foo-state-of-us.eth" [:ethereum {:prefix  "foo-state-of"
                                               :address "us.eth"}]

    "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@42" [:ethereum {:address  "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                                                                         :chain-id "42"}]

    "ethereum:0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x12345&uint256=1" [:ethereum {:address  "0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                                                                                                           :function "transfer"}]

    "ethereum:0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=2.014e18&gas=10&gasLimit=21000&gasPrice=50" [:ethereum {:address "0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

    "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@1/transfer?uint256=1" [:ethereum {:address  "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                                                                                           :chain-id "1"
                                                                                           :function "transfer"}]))

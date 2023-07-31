(ns status-im.router.core-test
  (:require [cljs.test :refer [are deftest]]
            [status-im.router.core :as router]))

(def public-key
  "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")
(def chat-id
  "59eb36e6-9d4d-4724-9d3a-8a3cdc5e8a8e-0x04f383daedc92a66add4c90d8884004ef826cba113183a0052703c8c77fed1522f88f44550498d20679af98907627059a295e43212a1cd3c1f21a157704d608c13")
(def chat-name-url "Test%20group%20chat")
(def chat-name "Test group chat")

(deftest parse-uris
  (are [uri expected] (= (cond-> (router/match-uri uri)
                           (< (count expected) 3)
                           (assoc :query-params nil))
                         {:handler      (first expected)
                          :route-params (second expected)
                          :query-params (when (= 3 (count expected)) (last expected))
                          :uri          uri})

   "status-im://u/statuse2e"
    [:user {:user-id "statuse2e"}]

   (str "status-im://user/" public-key)
    [:user {:user-id public-key}]

   "status-im://b/www.cryptokitties.co"
    [:browser {:domain "www.cryptokitties.c"}]

   (str "status-im://g/args?a=" public-key "&a1=" chat-name-url "&a2=" chat-id)
    [:group-chat {:params "arg"} {"a" public-key "a1" chat-name "a2" chat-id}]

   (str "https://status.app/g/args?a=" public-key "&a1=" chat-name-url "&a2=" chat-id)
    [:group-chat {:params "arg"} {"a" public-key "a1" chat-name "a2" chat-id}]

   "https://status.app/u/statuse2e"
    [:user {:user-id "statuse2e"}]

   (str "https://status.app/user/" public-key)
    [:user {:user-id public-key}]

   ;; Last char removed by: https://github.com/juxt/bidi/issues/104
   "https://status.app/b/www.cryptokitties.co"
    [:browser {:domain "www.cryptokitties.c"}]

   "https://status.app/b/https://www.google.com/"
    [:browser {:domain "https://www.google.co"}]

   "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
    [:ethereum {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

   "ethereum:pay-0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
    [:ethereum
     {:prefix  "pay"
      :address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]
   "ethereum:foo-0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
    [:ethereum
     {:prefix  "foo"
      :address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

   ;; FIXME: Should handle only first line
   "ethereum:foo-state-of-us.eth"
    [:ethereum
     {:prefix  "foo-state-of"
      :address "us.eth"}]

   "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@42"
    [:ethereum
     {:address  "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
      :chain-id "42"}]

   "ethereum:0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x12345&uint256=1"
    [:ethereum
     {:address  "0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
      :function "transfer"}]

   "ethereum:0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=2.014e18&gas=10&gasLimit=21000&gasPrice=50"
    [:ethereum {:address "0x0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}]

   "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@1/transfer?uint256=1"
    [:ethereum
     {:address  "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
      :chain-id "1"
      :function "transfer"}]))

(def error {:error :invalid-group-chat-data})

(deftest match-group-chat-query
  (are [query-params expected] (= (router/match-group-chat {} query-params)
                                  expected)
   nil                                             error
   {}                                              error
   {"b" public-key}                                error
   {"a" public-key "a1" chat-name}                 error
   {"a" "0x00ceded" "a1" chat-name "a2" chat-id}   error
   {"a" public-key "a1" chat-name "a2" public-key} error
   {"a" public-key "a1" chat-name "a2" chat-id}    {:type             :group-chat
                                                    :chat-id          chat-id
                                                    :invitation-admin public-key
                                                    :chat-name        chat-name}))

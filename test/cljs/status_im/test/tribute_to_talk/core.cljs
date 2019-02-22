(ns status-im.test.tribute-to-talk.core
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.utils.money :as money]))

(deftest get-new-snt-amount
  (testing "staying under the limit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "999999.9" "9")
           "999999.99")))
  (testing "getting over the limit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "100000" "0")
           "100000")))
  (testing "replace initial 0 by the first digit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0" "1")
           "1")))
  (testing "disallow more than two dots"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0." ".")
           "0."))
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.0" ".")
           "0.0")))
  (testing "disallow more than two digits after dot"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.0" "0")
           "0.00"))
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.00" "1")
           "0.00")))
  (testing "0 remains if removed is pressed on last digit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0" :remove)
           "0"))
    (is (= (tribute-to-talk/get-new-snt-amount
            "1" :remove)
           "0")))
  (testing "dot is removed when last digit after dot is removed"
    (= (tribute-to-talk/get-new-snt-amount
        "1." :remove)
       "1")
    (= (tribute-to-talk/get-new-snt-amount
        "1.1" :remove)
       "1")))

(def recipient-pk "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97")
(def recipient-address "0xdff1a5e4e57d9723b3294e0f4413372e3ea9a8ff")

(def user-cofx
  {:db {:account/account
        {:address "954d4393515747ea75808a0301fb73317ae1e460"
         :network "testnet_rpc"
         :networks {"testnet_rpc" {:config {:NetworkId 3}}}
         :settings {:tribute-to-talk {:testnet {:snt-amount "1000000000000000000"}}}}
        :contacts/contacts
        {recipient-pk {:name "bob"
                       :address recipient-address
                       :public-key recipient-pk
                       :tribute-to-talk {:snt-amount "1000000000000000000"}}}
        :wallet {:balance {:STT (money/bignumber "1000000000000000000")}}}})

(deftest pay-tribute
  (testing "transaction with enough funds"
    (let [results (tribute-to-talk/pay-tribute
                   user-cofx
                   recipient-pk)]
      (is (=  (dissoc (get-in results [:db :wallet :send-transaction]) :amount)
              {:on-result
               [:tribute-to-talk.callback/pay-tribute-transaction-sent
                "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97"],
               :to-name "bob",
               :amount-text "1",
               :method "eth_sendTransaction",
               :symbol :ETH,
               :send-transaction-message? true,
               :from-chat? true,
               :from "0x954d4393515747ea75808a0301fb73317ae1e460",
               :id "approve",
               :sufficient-funds? true,
               :on-error nil,
               :public-key
               "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97",
               :asset :STT,
               :to "0xc55cf4b03948d7ebc8b9e8bad92643703811d162",
               :data
               "0xa9059cbb000000000000000000000000dff1a5e4e57d9723b3294e0f4413372e3ea9a8ff0000000000000000000000000000000000000000000000000de0b6b3a7640000"}))))

  (testing "insufficient funds"
    (is (= (get-in (tribute-to-talk/pay-tribute
                    (assoc-in user-cofx [:db :wallet] nil)
                    recipient-pk)
                   [:db :wallet :send-transaction :sufficient-funds?])
           false))))

(deftest tribute-transaction-trigger
  (testing "transaction error"
    (is (tribute-to-talk/tribute-transaction-trigger
         {:ethereum/current-block 10}
         {:block "5"
          :error? true})))
  (testing "transaction confirmed"
    (is (tribute-to-talk/tribute-transaction-trigger
         {:ethereum/current-block 10}
         {:block "5"})))
  (testing "transaction not confirmed yet"
    (is (not (tribute-to-talk/tribute-transaction-trigger
              {:ethereum/current-block 5}
              {:block "5"})))))

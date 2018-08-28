(ns status-im.test.chat.commands.impl.transactions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.chat.commands.protocol :as protocol]))

(def cofx {:db {:account/account   {:settings              {:wallet {:visible-tokens {:mainnet #{:SNT}}}}
                                    :wallet-set-up-passed? true}
                :chain             "mainnet"
                :current-chat-id   "recipient"
                :contacts/contacts {"recipient" {:name             "Recipient"
                                                 :address          "0xAA"
                                                 :whisper-identity "0xBB"}}}})

;; testing the `/send` command

(def personal-send-command (transactions/PersonalSendCommand.))

(deftest personal-send-command-test
  (testing "That correct parameters are defined"
    (is (= (into #{} (map :id) (protocol/parameters personal-send-command))
           #{:asset :amount})))
  (testing "Parameters validation"
    (is (= (protocol/validate personal-send-command {:asset "TST"} cofx)
           {:title       "Invalid Asset"
            :description "Unknown token - TST"}))
    (is (= (protocol/validate personal-send-command {:asset "SNT"} cofx)
           {:title       "Amount"
            :description "Amount must be specified"}))
    (is (= (protocol/validate personal-send-command {:asset "SNT" :amount "a"} cofx)
           {:title       "Amount"
            :description "Amount is not valid number"}))
    (is (= (protocol/validate personal-send-command {:asset "ETH" :amount "0.54354353454353453453454353453445345545"} cofx)
           {:title       "Amount"
            :description "Max number of decimals is 18"}))
    (is (= (protocol/validate personal-send-command {:asset "ETH" :amount "0.01"} cofx)
           nil)))
  (testing "Yielding control prefills wallet"
    (let [fx (protocol/yield-control personal-send-command {:asset "ETH" :amount "0.01"} cofx)]
      (is (= (get-in fx [:db :wallet :send-transaction :amount-text]) "0.01"))
      (is (= (get-in fx [:db :wallet :send-transaction :symbol]) :ETH)))))

;; testing the `/request` command

(def personal-request-command (transactions/PersonalRequestCommand.))

(deftest personal-request-command-test
  (testing "That correct parameters are defined"
    (is (= (into #{} (map :id) (protocol/parameters personal-request-command))
           #{:asset :amount})))
  (testing "Parameters validation"
    (is (= (protocol/validate personal-request-command {:asset "TST"} cofx)
           {:title       "Invalid Asset"
            :description "Unknown token - TST"}))
    (is (= (protocol/validate personal-request-command {:asset "SNT"} cofx)
           {:title       "Amount"
            :description "Amount must be specified"}))
    (is (= (protocol/validate personal-request-command {:asset "SNT" :amount "a"} cofx)
           {:title       "Amount"
            :description "Amount is not valid number"}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "0.54354353454353453453454353453445345545"} cofx)
           {:title       "Amount"
            :description "Max number of decimals is 18"}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "0.01"} cofx)
           nil)))
  (testing "On receive adds pending request when `/request` command is received"
    (let [fx (protocol/on-receive personal-request-command
                                  {:chat-id    "recipient"
                                   :message-id "0xAA"}
                                  cofx)]
      (is (= (get-in fx [:db :chats "recipient" :requests "0xAA"])
             {:chat-id    "recipient"
              :message-id "0xAA"
              :response   "send"
              :status     "open"})))))

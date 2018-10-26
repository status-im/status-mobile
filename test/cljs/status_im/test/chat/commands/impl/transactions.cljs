(ns status-im.test.chat.commands.impl.transactions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.i18n :as i18n]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.chat.commands.protocol :as protocol]))

(def cofx {:db {:account/account   {:settings              {:wallet {:visible-tokens {:mainnet #{:SNT}}}}
                                    :wallet-set-up-passed? true}
                :chain             "mainnet"
                :current-chat-id   "recipient"
                :contacts/contacts {"recipient" {:name             "Recipient"
                                                 :address          "0xAA"
                                                 :public-key "0xBB"}}}})

;; testing the `/send` command

(def personal-send-command (transactions/PersonalSendCommand.))

(deftest personal-send-command-test
  (testing "That correct parameters are defined"
    (is (= (into #{} (map :id) (protocol/parameters personal-send-command))
           #{:asset :amount})))
  (testing "Parameters validation"
    (is (= (protocol/validate personal-send-command {:asset "TST"} cofx)
           {:title       (i18n/label :t/send-request-invalid-asset)
            :description (i18n/label :t/send-request-unknown-token {:asset "TST"})}))
    (is (= (protocol/validate personal-send-command {:asset "SNT"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-must-be-specified)}))
    (is (= (protocol/validate personal-send-command {:asset "SNT" :amount "a"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-send-command {:asset "ETH" :amount "0.54354353454353453453454353453445345545"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-max-decimals {:asset-decimals 18})}))
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
           {:title       (i18n/label :t/send-request-invalid-asset)
            :description (i18n/label :t/send-request-unknown-token {:asset "TST"})}))
    (is (= (protocol/validate personal-request-command {:asset "SNT"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-must-be-specified)}))
    (is (= (protocol/validate personal-request-command {:asset "SNT" :amount "a"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "0,1Aaa"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "1-45"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "SNT" :amount "1$#@8"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "SNT" :amount "20,"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "SNT" :amount "20."} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-invalid-number)}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "0.54354353454353453453454353453445345545"} cofx)
           {:title       (i18n/label :t/send-request-amount)
            :description (i18n/label :t/send-request-amount-max-decimals {:asset-decimals 18})}))
    (is (= (protocol/validate personal-request-command {:asset "ETH" :amount "0.01"} cofx)
           nil))))

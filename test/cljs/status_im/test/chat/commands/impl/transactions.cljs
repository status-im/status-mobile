(ns status-im.test.chat.commands.impl.transactions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.i18n :as i18n]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.chat.commands.protocol :as protocol]))

(def public-key "0x04f96bc2229a0ba4125815451e47491d9ab923b8b03f205f6ff11d731c0f5759079c1aa0f3b73233c114372695c30a8e20ce18f73fafa23f924736cc39e726c3de")
(def address "f86b3cefae5851c19abfc48b7fb034b1dfa70b52")
(def cofx {:db {:account/account   {:settings              {:wallet {:visible-tokens {:mainnet #{:SNT}}}}
                                    :wallet-set-up-passed? true}
                :chain             "mainnet"
                :current-chat-id   public-key
                :contacts/contacts {public-key {:name       "Recipient"
                                                :address    address
                                                :public-key public-key}}
                :wallet/all-tokens {:mainnet {"0x744d70fdbe2ba4cf95131626614a1763df805b9e" {:address  "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
                                                                                            :name     "Status Network Token"
                                                                                            :symbol   :SNT
                                                                                            :decimals 18}}}}})

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
    (let [fx (protocol/yield-control personal-send-command {:content {:params {:asset "ETH" :amount "0.01"}}} cofx)]
      (is (= (get-in fx [:db :wallet :send-transaction :amount-text]) "0.01"))
      (is (= (get-in fx [:db :wallet :send-transaction :symbol]) :ETH)))))

(deftest from-contacts
  (testing "the user is in our contacts"
    (let [fx (protocol/yield-control personal-send-command {:content {:params {:asset "ETH" :amount "0.01"}}} cofx)]
      (is (= (get-in fx [:db :wallet :send-transaction :to]) address))
      (is (= (get-in fx [:db :wallet :send-transaction :to-name] "Recipient")))
      (is (= (get-in fx [:db :wallet :send-transaction :public-key]) public-key)))
    (testing "the user is not in our contacts"
      (let [fx (protocol/yield-control personal-send-command {:content {:params {:asset "ETH" :amount "0.01"}}} (update-in cofx [:db :contacts/contacts] dissoc public-key))]
        (is (= (get-in fx [:db :wallet :send-transaction :to]) address))
        (is (= (get-in fx [:db :wallet :send-transaction :to-name]) "Plump Nippy Blobfish"))
        (is (= (get-in fx [:db :wallet :send-transaction :public-key]) public-key))))))

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

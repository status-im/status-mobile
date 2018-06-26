(ns status-im.test.utils.ethereum.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.ethereum.core :as ethereum]))

(deftest call-params
  (testing "ERC20 balance-of params"
    (let [contract "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
          address "0xa7cfd581060ec66414790691681732db249502bd"]
      (is (= (ethereum/call-params contract "balanceOf(address)" address)
             {:to "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
              :data "0x70a08231000000000000000000000000a7cfd581060ec66414790691681732db249502bd"})))))

(deftest valid-words?
  (is (not (true? (ethereum/valid-words? ["rate" "rate"]))))
  (is (not (true? (ethereum/valid-words? ["rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate?"]))))
  (is (true? (ethereum/valid-words? ["rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate"]))))

(deftest passphrase->words?
  (is (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (ethereum/passphrase->words "one two three for five six seven height nine ten eleven twelve"))
      (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (ethereum/passphrase->words "  one two three for five   six seven height nine ten eleven twelve "))))

(ns legacy.status-im.ethereum.mnemonic-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.ethereum.mnemonic :as mnemonic]))

(deftest passphrase->words?-test
  (is (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words "one two three for five six seven height nine ten eleven twelve"))
      (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words
          "  one two three for five   six seven height nine ten eleven twelve "))))

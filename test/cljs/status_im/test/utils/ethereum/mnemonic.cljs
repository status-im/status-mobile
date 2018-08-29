(ns status-im.test.utils.ethereum.mnemonic
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.ethereum.mnemonic :as mnemonic]))

(deftest valid-words?
  (is (not (mnemonic/valid-words? ["rate" "rate"])))
  (is (not (mnemonic/valid-words? ["rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate?"])))
  (is (mnemonic/valid-words? ["rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate" "rate"])))

(deftest valid-phrase
  (is (not (mnemonic/valid-phrase? "rate rate")))
  (is (not (mnemonic/valid-phrase? "rate rate rate rate rate rate rate rate rate rate rate rate?")))
  (is (mnemonic/valid-phrase? "rate rate rate rate rate rate rate rate rate rate rate rate")))

(deftest passphrase->words?
  (is (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words "one two three for five six seven height nine ten eleven twelve"))
      (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words "  one two three for five   six seven height nine ten eleven twelve "))))

(deftest status-generate-phrase?
  (is (mnemonic/status-generated-phrase? "game buzz method pretty olympic fat quit display velvet unveil marine crater"))
  (is (not (mnemonic/status-generated-phrase? "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))

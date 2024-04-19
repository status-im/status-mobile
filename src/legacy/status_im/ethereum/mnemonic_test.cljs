(ns legacy.status-im.ethereum.mnemonic-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [clojure.string :as string]
    [legacy.status-im.ethereum.mnemonic :as mnemonic]))

(deftest valid-length?
  (is (not (mnemonic/valid-length? "rate rate")))
  (is (not (mnemonic/valid-length? (string/join " " (repeat 13 "rate")))))
  (is (not (mnemonic/valid-length? (string/join " " (repeat 16 "rate")))))
  (is (mnemonic/valid-length? (string/join " " (repeat 12 "rate"))))
  (is (mnemonic/valid-length? (string/join " " (repeat 15 "rate"))))
  (is (mnemonic/valid-length? (string/join " " (repeat 18 "rate"))))
  (is (mnemonic/valid-length? (string/join " " (repeat 21 "rate"))))
  (is (mnemonic/valid-length? (string/join " " (repeat 24 "rate")))))

(deftest valid-words?
  (is (not (mnemonic/valid-words? "rate! rate")))
  (is (not (mnemonic/valid-words? "rate rate rate rate rate rate rate rate rate rate rate rate?")))
  (is (mnemonic/valid-words? "rate rate rate rate rate rate rate rate rate rate rate rate")))

(deftest passphrase->words?
  (is (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words "one two three for five six seven height nine ten eleven twelve"))
      (= ["one" "two" "three" "for" "five" "six" "seven" "height" "nine" "ten" "eleven" "twelve"]
         (mnemonic/passphrase->words
          "  one two three for five   six seven height nine ten eleven twelve "))))

(deftest status-generate-phrase?
  (is (mnemonic/status-generated-phrase?
       "game buzz method pretty olympic fat quit display velvet unveil marine crater"))
  (is (not (mnemonic/status-generated-phrase?
            "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))

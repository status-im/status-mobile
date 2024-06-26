(ns status-im.common.validation.keypair-test
  (:require
    [cljs.test :refer-macros [deftest are]]
    [status-im.common.validation.keypair :as keypair-validator]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]))

(deftest keypair-name-too-short-test
  (are [arg expected]
   (expected (keypair-validator/keypair-too-short? arg))
   "abc"    true?
   "abcdef" false?))

(deftest keypair-name-too-long-test
  (are [arg expected]
   (expected (keypair-validator/keypair-too-long? arg))
   (apply str (repeat 25 "a")) true?
   "abcdef"                    false?))

(deftest validation-keypair-name-test
  (are [arg expected]
   (= (keypair-validator/validation-keypair-name arg #{"Collection"}) expected)
   nil                         nil
   ""                          nil
   "name !"                    (i18n/label :t/key-name-error-special-char)
   "Hello 😊"                  (i18n/label :t/key-name-error-emoji)
   "abc"                       (i18n/label :t/key-name-error-too-short
                                           {:count constants/key-pair-name-min-length})
   "Collection"                (i18n/label :t/key-name-error-taken)
   (apply str (repeat 25 "a")) (i18n/label :t/your-key-pair-name-is-too-long)))

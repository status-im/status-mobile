(ns status-im.common.validation.profile-test
  (:require
    [cljs.test :refer-macros [deftest are]]
    [status-im.common.validation.general :as validator]
    [status-im.common.validation.profile :as profile-validator]
    [utils.i18n :as i18n]))

(deftest has-emojis-test
  (are [arg expected]
   (expected (validator/has-emojis? arg))
   "Hello 😊" true?
   "Hello"    false?))

(deftest has-common-names-test
  (are [arg expected]
   (expected (profile-validator/has-common-names? arg))
   "Ethereum" true?
   "Hello"    false?))

(deftest has-special-characters-test
  (are [arg expected]
   (expected (validator/has-special-characters? arg))
   "@name" true?
   "name"  false?))

(deftest name-too-short-test
  (are [arg expected]
   (expected (profile-validator/name-too-short? arg))
   "abc"    true?
   "abcdef" false?))

(deftest name-too-long-test
  (are [arg expected]
   (expected (profile-validator/name-too-long? arg))
   (apply str (repeat 25 "a")) true?
   "abcdef"                    false?))

(deftest validation-name-test
  (are [arg expected]
   (= (profile-validator/validation-name arg) expected)
   nil                         nil
   ""                          nil
   "@name"                     (i18n/label :t/are-not-allowed
                                           {:check (i18n/label :t/special-characters)})
   "name-eth"                  (i18n/label :t/ending-not-allowed {:ending "-eth"})
   "name_eth"                  (i18n/label :t/ending-not-allowed {:ending "_eth"})
   "name.eth"                  (i18n/label :t/ending-not-allowed {:ending ".eth"})
   " name"                     (i18n/label :t/start-with-space)
   "name "                     (i18n/label :t/ends-with-space)
   "Ethereum"                  (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
   "Hello 😊"                  (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
   "abc"                       (i18n/label :t/minimum-characters {:min-chars 5})
   (apply str (repeat 25 "a")) (i18n/label :t/profile-name-is-too-long)))

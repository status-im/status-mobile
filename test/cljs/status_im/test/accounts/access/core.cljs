(ns status-im.test.accounts.access.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.accounts.access.core :as models]
            [clojure.string :as string]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]))

;;;; helpers

(deftest check-phrase-errors
  (is (= :required-field (models/check-phrase-errors nil)))
  (is (= :required-field (models/check-phrase-errors " ")))
  (is (= :required-field (models/check-phrase-errors " \t\n ")))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors "phrase with four words")))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors "phrase with five cool words")))
  (is (nil? (models/check-phrase-errors "monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey")))
  (is (nil? (models/check-phrase-errors (string/join " " (repeat 15 "monkey")))))
  (is (nil? (models/check-phrase-errors (string/join " " (repeat 18 "monkey")))))
  (is (nil? (models/check-phrase-errors (string/join " " (repeat 24 "monkey")))))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors (string/join " " (repeat 14 "monkey")))))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors (string/join " " (repeat 11 "monkey")))))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors (string/join " " (repeat 19 "monkey")))))
  (is (= :recovery-phrase-invalid (models/check-phrase-errors "monkey monkey monkey 12345 monkey adf+123 monkey monkey monkey monkey monkey monkey")))
  ;;NOTE(goranjovic): the following check should be ok because we sanitize extra whitespace
  (is (nil? (models/check-phrase-errors "  monkey monkey monkey\t monkey monkey    monkey monkey monkey monkey monkey monkey monkey \t "))))

(deftest check-phrase-warnings
  (is (nil? (models/check-phrase-warnings "monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey")))
  (is (nil? (models/check-phrase-warnings "game buzz method pretty olympic fat quit display velvet unveil marine crater")))
  (is (= :recovery-phrase-unknown-words (models/check-phrase-warnings "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))

;;;; handlers

(deftest validate-phrase
  (is (= {:db {:accounts/access {:passphrase-error   nil
                                 :passphrase-warning nil
                                 :passphrase         "game buzz method pretty olympic fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/access {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"}}})))
  (is (= {:db {:accounts/access {:passphrase-error   nil
                                 :passphrase-warning :recovery-phrase-unknown-words
                                 :passphrase         "game buzz method pretty zeus fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/access {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"}}})))
  (is (= {:db {:accounts/access {:passphrase-error   :recovery-phrase-invalid
                                 :passphrase-warning :recovery-phrase-unknown-words
                                 :passphrase         "game buzz method pretty 1234 fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/access {:passphrase "game buzz method pretty 1234 fat quit display velvet unveil marine crater"}}}))))

(deftest access-account
  (let [new-cofx (models/access-account {:db {:accounts/access
                                              {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                               :password   "thisisapaswoord"}}})]
    (is (= {:accounts/access {:passphrase  "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                              :password    "thisisapaswoord"
                              :processing? true}}
           (:db new-cofx)))
    (is (= security/MaskedData
           (-> new-cofx :accounts.access/access-account first type)))
    (is (= "thisisapaswoord" (-> new-cofx :accounts.access/access-account second)))))

(deftest access-account-with-checks
  (let [new-cofx (models/access-account-with-checks {:db {:accounts/access
                                                          {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                                           :password   "thisisapaswoord"}}})]
    (is (= {:accounts/access {:passphrase  "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                              :password    "thisisapaswoord"
                              :processing? true}}
           (:db new-cofx)))
    (is (= security/MaskedData
           (-> new-cofx :accounts.access/access-account first type)))
    (is (= "thisisapaswoord" (-> new-cofx :accounts.access/access-account second))))
  (let [new-cofx (models/access-account-with-checks {:db {:accounts/access
                                                          {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                           :password   "thisisapaswoord"}}})]
    (is (= (i18n/label :recovery-typo-dialog-title) (-> new-cofx :ui/show-confirmation :title)))
    (is (= (i18n/label :recovery-typo-dialog-description) (-> new-cofx :ui/show-confirmation :content)))
    (is (= (i18n/label :recovery-confirm-phrase) (-> new-cofx :ui/show-confirmation :confirm-button-text)))))

(ns status-im.test.accounts.recover.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.accounts.recover.core :as models]
            [clojure.string :as string]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]))

;;;; helpers

(deftest check-password-errors
  (is (= :required-field (models/check-password-errors nil)))
  (is (= :required-field (models/check-password-errors " ")))
  (is (= :required-field (models/check-password-errors " \t\n ")))
  (is (= :recover-password-too-short) (models/check-password-errors "a"))
  (is (= :recover-password-too-short) (models/check-password-errors "abc"))
  (is (= :recover-password-too-short) (models/check-password-errors "12345"))
  (is (nil? (models/check-password-errors "123456")))
  (is (nil? (models/check-password-errors "thisisapasswoord"))))

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
  (is (= :recovery-phrase-unknown-words  (models/check-phrase-warnings "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))

;;;; handlers

(deftest set-phrase
  (is (= {:db {:accounts/recover {:passphrase        "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                  :passphrase-valid? true}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty olympic fat quit display velvet unveil marine crater"))))
  (is (= {:db {:accounts/recover {:passphrase        "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                  :passphrase-valid? true}}}
         (models/set-phrase {:db {}} (security/mask-data "Game buzz method pretty Olympic fat quit DISPLAY velvet unveil marine crater"))))
  (is (= {:db {:accounts/recover {:passphrase        "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                  :passphrase-valid? true}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))
  (is (= {:db {:accounts/recover {:passphrase        "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "
                                  :passphrase-valid? true}}}
         (models/set-phrase {:db {}} (security/mask-data "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "))))
  (is (= {:db {:accounts/recover {:passphrase        "game buzz method pretty 1234 fat quit display velvet unveil marine crater"
                                  :passphrase-valid? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty 1234 fat quit display velvet unveil marine crater")))))

(deftest validate-phrase
  (is (= {:db {:accounts/recover {:passphrase-error   nil
                                  :passphrase-warning nil
                                  :passphrase         "game buzz method pretty olympic fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/recover {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"}}})))
  (is (= {:db {:accounts/recover {:passphrase-error   nil
                                  :passphrase-warning :recovery-phrase-unknown-words
                                  :passphrase         "game buzz method pretty zeus fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/recover {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"}}})))
  (is (= {:db {:accounts/recover {:passphrase-error   :recovery-phrase-invalid
                                  :passphrase-warning :recovery-phrase-unknown-words
                                  :passphrase         "game buzz method pretty 1234 fat quit display velvet unveil marine crater"}}}
         (models/validate-phrase {:db {:accounts/recover {:passphrase "game buzz method pretty 1234 fat quit display velvet unveil marine crater"}}}))))

(deftest set-password
  (is (= {:db {:accounts/recover {:password        "     "
                                  :password-valid? false}}}
         (models/set-password {:db {}} (security/mask-data "     "))))
  (is (= {:db {:accounts/recover {:password        "abc"
                                  :password-valid? false}}}
         (models/set-password {:db {}} (security/mask-data "abc"))))
  (is (= {:db {:accounts/recover {:password        "thisisapaswoord"
                                  :password-valid? true}}}
         (models/set-password {:db {}} (security/mask-data "thisisapaswoord")))))

(deftest validate-password
  (is (= {:db {:accounts/recover {:password       "     "
                                  :password-error :required-field}}}
         (models/validate-password {:db {:accounts/recover {:password "     "}}})))
  (is (= {:db {:accounts/recover {:password       "abc"
                                  :password-error :recover-password-too-short}}}
         (models/validate-password {:db {:accounts/recover {:password "abc"}}})))
  (is (= {:db {:accounts/recover {:password       "thisisapaswoord"
                                  :password-error nil}}}
         (models/validate-password {:db {:accounts/recover {:password "thisisapaswoord"}}}))))

(deftest recover-account
  (let [new-cofx (models/recover-account {:random-guid-generator (constantly "random")
                                          :db {:accounts/recover
                                               {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                :password   "thisisapaswoord"}}})]
    (is (contains? new-cofx :node/start))
    (is (= "random" (get-in new-cofx [:db :accounts/new-installation-id])))
    (is (= :recover-account (get-in new-cofx [:db :node/on-ready])))))

(deftest recover-account-with-checks
  (let [new-cofx (models/recover-account-with-checks {:random-guid-generator (constantly "random")
                                                      :db {:accounts/recover
                                                           {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                                            :password   "thisisapaswoord"}}})]
    (is (contains? new-cofx :node/start))
    (is (= "random" (get-in new-cofx [:db :accounts/new-installation-id])))
    (is (= :recover-account (get-in new-cofx [:db :node/on-ready]))))
  (let [new-cofx (models/recover-account-with-checks {:random-guid-generator (constantly "random")
                                                      :db {:accounts/recover
                                                           {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                            :password   "thisisapaswoord"}}})]
    (is (= (i18n/label :recovery-typo-dialog-title) (-> new-cofx :ui/show-confirmation :title)))
    (is (= (i18n/label :recovery-typo-dialog-description) (-> new-cofx :ui/show-confirmation :content)))
    (is (= (i18n/label :recovery-confirm-phrase) (-> new-cofx :ui/show-confirmation :confirm-button-text)))))

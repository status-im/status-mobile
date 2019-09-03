(ns status-im.test.multiaccounts.recover.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.recover.core :as models]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [clojure.string :as string]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]))

;;;; helpers


(deftest check-phrase-warnings
  (is (nil? (models/check-phrase-warnings "monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey monkey")))
  (is (nil? (models/check-phrase-warnings "game buzz method pretty olympic fat quit display velvet unveil marine crater")))
  (is (= :recovery-phrase-unknown-words  (models/check-phrase-warnings "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))

;;;; handlers

(deftest set-phrase
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty olympic fat quit display velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "Game buzz method pretty Olympic fat quit DISPLAY velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty 1234 fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty 1234 fat quit display velvet unveil marine crater")))))

(deftest store-multiaccount
  (let [new-cofx (models/store-multiaccount {:db {:intro-wizard
                                                  {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                   :password   "thisisapaswoord"}}})]
    (is (::multiaccounts.create/store-multiaccount new-cofx))))

(deftest recover-multiaccount-with-checks
  (let [new-cofx (models/recover-multiaccount-with-checks {:db {:intro-wizard
                                                                {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                                                 :password   "thisisapaswoord"}}})]
    (is (::multiaccounts.create/store-multiaccount new-cofx)))
  (let [new-cofx (models/recover-multiaccount-with-checks {:db {:intro-wizard
                                                                {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                                 :password   "thisisapaswoord"}}})]
    (is (= (i18n/label :recovery-typo-dialog-title) (-> new-cofx :ui/show-confirmation :title)))
    (is (= (i18n/label :recovery-typo-dialog-description) (-> new-cofx :ui/show-confirmation :content)))
    (is (= (i18n/label :recovery-confirm-phrase) (-> new-cofx :ui/show-confirmation :confirm-button-text)))))

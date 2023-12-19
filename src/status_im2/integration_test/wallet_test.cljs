(ns status-im2.integration-test.wallet-test
  (:require
    [cljs.test :refer [is]]
    [clojure.string :as string]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [re-frame.core :as rf]
    status-im2.events
    status-im2.navigation.core
    status-im2.subs.root
    [test-helpers.integration :as h]))

;; Workaround to skip test. Switch to `deftest` when test is fixed.
(defn create-wallet-account-test
  []
  (h/log-headline :create-wallet-account-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (h/create-new-account!)
     (rf-test/wait-for
       [:wallet-legacy.accounts/account-stored]
       (h/assert-new-account-created)
       (h/logout)
       (rf-test/wait-for [::logout/logout-method]))))))

;; Workaround to skip test. Switch to `deftest` when test is fixed.
(defn back-up-seed-phrase-test
  []
  (h/log-headline :back-up-seed-phrase-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch-sync [:set-in [:my-profile/seed :step] :12-words]) ; display seed phrase to user
     (rf/dispatch-sync [:my-profile/enter-two-random-words]) ; begin prompting user for seed words
     (let [{:keys [mnemonic]} @(rf/subscribe [:profile/profile])
           seed               @(rf/subscribe [:my-profile/seed])
           word1              (second (:first-word seed))
           word2              (second (:second-word seed))]
       (is (= 12 (count (string/split mnemonic #" "))))
       (rf/dispatch-sync [:set-in [:my-profile/seed :word] word1])
       (rf/dispatch-sync [:my-profile/set-step :second-word])
       (rf/dispatch-sync [:set-in [:my-profile/seed :word] word2])
       (rf/dispatch [:my-profile/finish])
       (rf-test/wait-for
         [:my-profile/finish-success]
         (is (nil? @(rf/subscribe [:mnemonic]))) ; assert seed phrase has been removed
         (h/logout)
         (rf-test/wait-for [::logout/logout-method])))))))

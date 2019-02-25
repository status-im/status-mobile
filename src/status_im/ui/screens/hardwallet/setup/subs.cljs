(ns status-im.ui.screens.hardwallet.setup.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :hardwallet-setup-step
 (fn [db]
   (get-in db [:hardwallet :setup-step])))

(re-frame/reg-sub
 :hardwallet-card-state
 (fn [db]
   (get-in db [:hardwallet :card-state])))

(re-frame/reg-sub
 :hardwallet-flow
 (fn [db]
   (get-in db [:hardwallet :flow])))

(re-frame/reg-sub
 :hardwallet-pair-code
 (fn [db]
   (get-in db [:hardwallet :secrets :password])))

(re-frame/reg-sub
 :hardwallet-recovery-phrase-word
 (fn [db]
   (get-in db [:hardwallet
               :recovery-phrase
               (get-in db [:hardwallet :recovery-phrase :step])])))

(re-frame/reg-sub
 :hardwallet-recovery-phrase-input-word
 (fn [db]
   (get-in db [:hardwallet :recovery-phrase :input-word])))

(re-frame/reg-sub
 :hardwallet-recovery-phrase-confirm-error
 (fn [db]
   (get-in db [:hardwallet :recovery-phrase :confirm-error])))

(re-frame/reg-sub
 :hardwallet-recovery-phrase-step
 (fn [db]
   (get-in db [:hardwallet :recovery-phrase :step])))

(re-frame/reg-sub
 :hardwallet-secrets
 (fn [db]
   (get-in db [:hardwallet :secrets])))

(re-frame/reg-sub
 :hardwallet-setup-error
 (fn [db]
   (get-in db [:hardwallet :setup-error])))

(re-frame/reg-sub
 :hardwallet-mnemonic
 (fn [db]
   (get-in db [:hardwallet :secrets :mnemonic])))

(re-frame/reg-sub
 :hardwallet-application-info
 (fn [db]
   (get-in db [:hardwallet :application-info])))

(re-frame/reg-sub
 :hardwallet-application-info-error
 (fn [db]
   (get-in db [:hardwallet :application-info-error])))

(ns status-im.ui.screens.keycard.setup.subs
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]))

(re-frame/reg-sub
 :keycard-setup-step
 (fn [db]
   (get-in db [:keycard :setup-step])))

(re-frame/reg-sub
 :keycard-card-state
 (fn [db]
   (get-in db [:keycard :card-state])))

(re-frame/reg-sub
 :keycard-flow
 (fn [db]
   (get-in db [:keycard :flow])))

(re-frame/reg-sub
 :keycard-flow-steps
 (fn [db]
   (case (get-in db [:keycard :flow])
     :recovery "2"
     "3")))

(re-frame/reg-sub
 :keycard-pair-code
 (fn [db]
   (get-in db [:keycard :secrets :password])))

(re-frame/reg-sub
 :keycard-recovery-phrase-word
 (fn [db]
   (get-in db [:keycard
               :recovery-phrase
               (get-in db [:keycard :recovery-phrase :step])])))

(re-frame/reg-sub
 :keycard-recovery-phrase-input-word
 (fn [db]
   (get-in db [:keycard :recovery-phrase :input-word])))

(re-frame/reg-sub
 :keycard-recovery-phrase-confirm-error
 (fn [db]
   (get-in db [:keycard :recovery-phrase :confirm-error])))

(re-frame/reg-sub
 :keycard-recovery-phrase-step
 (fn [db]
   (get-in db [:keycard :recovery-phrase :step])))

(re-frame/reg-sub
 :keycard-secrets
 (fn [db]
   (get-in db [:keycard :secrets])))

(re-frame/reg-sub
 :keycard-puk-code
 (fn [db]
   (->> (get-in db [:keycard :secrets :puk])
        (partition 4)
        (map clojure.string/join)
        (clojure.string/join " "))))

(re-frame/reg-sub
 :keycard-setup-error
 (fn [db]
   (get-in db [:keycard :setup-error])))

(re-frame/reg-sub
 :keycard-mnemonic
 (fn [db]
   (map-indexed vector
                (partition 3
                           (map-indexed vector (clojure.string/split
                                                (get-in db [:keycard :secrets :mnemonic])
                                                #" "))))))

(re-frame/reg-sub
 :keycard-application-info
 (fn [db]
   (get-in db [:keycard :application-info])))

(re-frame/reg-sub
 :keycard-application-info-error
 (fn [db]
   (get-in db [:keycard :application-info-error])))

(re-frame/reg-sub
 :keycard-multiaccount
 (fn [db]
   (get-in db [:keycard :multiaccount])))

(re-frame/reg-sub
 :keycard-multiaccount-wallet-address
 (fn [db]
   (str "0x" (get-in db [:keycard :multiaccount-wallet-address]))))

(re-frame/reg-sub
 :keycard-multiaccount-whisper-public-key
 (fn [db]
   (ethereum/normalized-hex (get-in db [:keycard :multiaccount-whisper-public-key]))))

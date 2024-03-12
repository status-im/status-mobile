(ns legacy.status-im.subs.keycard
  (:require
    [clojure.string :as string]
    [legacy.status-im.keycard.common :as common]
    [re-frame.core :as re-frame]
    [utils.address :as address]
    [utils.datetime :as datetime]))

(re-frame/reg-sub
 :keycard/nfc-enabled?
 (fn [db]
   (get-in db [:keycard :nfc-enabled?])))

(re-frame/reg-sub
 :keycard/card-read-in-progress?
 (fn [db]
   (get-in db [:keycard :card-read-in-progress?] false)))

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
   (get-in db
           [:keycard
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
        (map string/join)
        (string/join " "))))

(re-frame/reg-sub
 :keycard-setup-error
 (fn [db]
   (get-in db [:keycard :setup-error])))

(re-frame/reg-sub
 :keycard-mnemonic
 (fn [db]
   (map-indexed vector
                (partition 3
                           (map-indexed vector
                                        (string/split
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
   (get-in db [:keycard :profile/profile])))

(re-frame/reg-sub
 :keycard-multiaccount-wallet-address
 (fn [db]
   (str "0x" (get-in db [:keycard :multiaccount-wallet-address]))))

(re-frame/reg-sub
 :keycard-multiaccount-whisper-public-key
 (fn [db]
   (address/normalized-hex (get-in db [:keycard :multiaccount-whisper-public-key]))))

(re-frame/reg-sub
 :keycard-paired-on
 (fn [db]
   (some-> (get-in db [:profile/profile :keycard-paired-on])
           (datetime/timestamp->year-month-day-date))))

(re-frame/reg-sub
 :keycard-multiaccount-pairing
 (fn [db]
   (get-in db [:profile/profile :keycard-pairing])))

(re-frame/reg-sub
 :keycard/pin-retry-counter
 (fn [db]
   (get-in db [:keycard :application-info :pin-retry-counter])))

(re-frame/reg-sub
 :keycard/puk-retry-counter
 (fn [db]
   (get-in db [:keycard :application-info :puk-retry-counter])))

(re-frame/reg-sub
 :keycard/retry-counter
 :<- [:keycard/pin-retry-counter]
 :<- [:keycard/puk-retry-counter]
 (fn [[pin puk]]
   (if (zero? pin) puk pin)))

(re-frame/reg-sub
 :keycard-reset-card-disabled?
 (fn [db]
   (get-in db [:keycard :reset-card :disabled?] false)))

(re-frame/reg-sub
 :keycard-multiaccount?
 common/keycard-multiaccount?)

(re-frame/reg-sub
 :keycard/original-pin
 (fn [db]
   (get-in db [:keycard :pin :original])))

(re-frame/reg-sub
 :keycard/login-pin
 (fn [db]
   (get-in db [:keycard :pin :login])))

(re-frame/reg-sub
 :keycard/pin-confirmation
 (fn [db]
   (get-in db [:keycard :pin :confirmation])))

(re-frame/reg-sub
 :keycard/pin-enter-step
 (fn [db]
   (get-in db [:keycard :pin :enter-step])))

(re-frame/reg-sub
 :keycard/pin-operation
 (fn [db]
   (get-in db [:keycard :pin :operation])))

(re-frame/reg-sub
 :keycard/pin-data
 (fn [db]
   (get-in db [:keycard :pin])))

(re-frame/reg-sub
 :keycard/pin
 :<- [:keycard/pin-data]
 :<- [:keycard/pin-enter-step]
 (fn [[pin-data step]]
   (get pin-data step)))

(re-frame/reg-sub
 :keycard/pin-status
 (fn [db]
   (get-in db [:keycard :pin :status])))

(re-frame/reg-sub
 :keycard/pin-error-label
 (fn [db]
   (get-in db [:keycard :pin :error-label])))

(re-frame/reg-sub
 :keycard/frozen-card?
 (fn [db]
   (and (common/keycard-multiaccount? db)
        (zero? (get-in db [:keycard :application-info :pin-retry-counter])))))

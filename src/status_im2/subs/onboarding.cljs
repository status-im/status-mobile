(ns status-im2.subs.onboarding
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.recover.core :as recover]
            [status-im2.constants :as constants]))

(re-frame/reg-sub
 :intro-wizard
 :<- [:intro-wizard-state]
 :<- [:dimensions/window]
 (fn [[wizard-state {:keys [width height]}]]
   (assoc wizard-state :view-height height :view-width width)))

(re-frame/reg-sub
 :intro-wizard/choose-key
 :<- [:intro-wizard]
 (fn [wizard-state]
   (select-keys wizard-state [:multiaccounts :selected-id :view-height])))

(re-frame/reg-sub
 :intro-wizard/select-key-storage
 :<- [:intro-wizard]
 (fn [wizard-state]
   (select-keys wizard-state [:selected-storage-type :recovering?])))

(re-frame/reg-sub
 :intro-wizard/enter-phrase
 :<- [:intro-wizard]
 (fn [wizard-state]
   (select-keys wizard-state
                [:processing?
                 :passphrase-word-count
                 :next-button-disabled?
                 :passphrase-error])))

(re-frame/reg-sub
 :intro-wizard/recovery-success
 :<- [:intro-wizard]
 (fn [wizard-state]
   {:pubkey      (get-in wizard-state [:derived constants/path-whisper-keyword :public-key])
    :name        (get-in wizard-state [:derived constants/path-whisper-keyword :name])
    :identicon   (get-in wizard-state [:derived constants/path-whisper-keyword :identicon])
    :processing? (:processing? wizard-state)}))

(re-frame/reg-sub
 :intro-wizard/recover-existing-account?
 :<- [:intro-wizard]
 :<- [:multiaccounts/multiaccounts]
 (fn [[intro-wizard multiaccounts]]
   (recover/existing-account? (:root-key intro-wizard) multiaccounts)))

(defn login-ma-keycard-pairing
  "Compute the keycard-pairing value of the multiaccount selected for login"
  [db _]
  (when-let [acc-to-login (-> db :multiaccounts/login)]
    (-> db
        :multiaccounts/multiaccounts
        (get (:key-uid acc-to-login))
        :keycard-pairing)))

(re-frame/reg-sub
 :intro-wizard/acc-to-login-keycard-pairing
 login-ma-keycard-pairing)

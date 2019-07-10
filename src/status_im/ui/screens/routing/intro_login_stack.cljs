(ns status-im.ui.screens.routing.intro-login-stack
  (:require [status-im.utils.config :as config]))

(def all-screens
  #{:login
    :progress
    :create-multiaccount
    :recover
    :multiaccounts
    :intro
    :intro-wizard
    :hardwallet-authentication-method
    :hardwallet-connect
    :enter-pin-login
    :hardwallet-setup
    :hardwallet-success
    :keycard-connection-lost
    :keycard-nfc-on
    :keycard-pairing
    :keycard-onboarding-intro
    :keycard-onboarding-start
    :keycard-onboarding-puk-code
    :keycard-onboarding-preparing
    :keycard-onboarding-finishing
    :keycard-onboarding-pin
    :keycard-onboarding-recovery-phrase
    :keycard-onboarding-recovery-phrase-confirm-word1
    :keycard-onboarding-recovery-phrase-confirm-word2
    :keycard-recovery-enter-mnemonic
    :keycard-recovery-intro
    :keycard-recovery-start
    :keycard-recovery-pair
    :keycard-recovery-recovering
    :keycard-recovery-success
    :keycard-recovery-no-key
    :keycard-recovery-pin})

(defn login-stack [view-id]
  {:name    :login-stack
   :screens (cond-> [:login
                     :progress
                     :create-multiaccount
                     :recover
                     :multiaccounts]

              config/hardwallet-enabled?
              (concat [:hardwallet-authentication-method
                       :hardwallet-connect
                       :enter-pin-login
                       :hardwallet-setup
                       :hardwallet-success]))
   :config  (if
                ;; add view-id here if you'd like that view to be
                ;; first view when app is started
             (#{:login :progress :multiaccounts :enter-pin-login} view-id)
              {:initialRouteName view-id}
              {:initialRouteName :login})})

(defn intro-stack []
  (-> (login-stack :intro)
      (update :screens conj
              :intro
              :intro-wizard
              :keycard-connection-lost
              :keycard-nfc-on
              :keycard-pairing
              :keycard-onboarding-intro
              :keycard-onboarding-start
              :keycard-onboarding-puk-code
              :keycard-onboarding-preparing
              :keycard-onboarding-finishing
              :keycard-onboarding-pin
              :keycard-onboarding-recovery-phrase
              :keycard-onboarding-recovery-phrase-confirm-word1
              :keycard-onboarding-recovery-phrase-confirm-word2
              :keycard-recovery-enter-mnemonic
              :keycard-recovery-intro
              :keycard-recovery-start
              :keycard-recovery-pair
              :keycard-recovery-recovering
              :keycard-recovery-success
              :keycard-recovery-no-key
              :keycard-recovery-pin)
      (assoc :name :intro-stack)
      (assoc :config {:initialRouteName :intro})))

(ns status-im.ui.screens.routing.intro-login-stack
  (:require [status-im.utils.config :as config]))

(def all-screens
  #{:login
    :progress
    :create-multiaccount
    :recover-multiaccount-enter-phrase
    :recover-multiaccount-select-storage
    :recover-multiaccount-enter-password
    :recover-multiaccount-confirm-password
    :recover-multiaccount-success
    :multiaccounts
    :intro
    :intro-wizard
    :hardwallet-authentication-method
    :hardwallet-connect
    :hardwallet-setup
    :hardwallet-success
    :keycard-connection-lost
    :keycard-connection-lost-setup
    :keycard-nfc-on
    :keycard-pairing
    :keycard-blank
    :keycard-wrong
    :keycard-unpaired
    :keycard-login-pin
    :keycard-login-connect-card
    :not-keycard
    :keycard-onboarding-intro
    :keycard-onboarding-start
    :keycard-onboarding-puk-code
    :keycard-onboarding-preparing
    :keycard-onboarding-finishing
    :keycard-onboarding-pin
    :keycard-onboarding-recovery-phrase
    :keycard-onboarding-recovery-phrase-confirm-word1
    :keycard-onboarding-recovery-phrase-confirm-word2
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
                     :multiaccounts
                     :intro-wizard
                     :progress
                     :keycard-recovery-intro
                     :create-multiaccount
                     :recover-multiaccount-enter-phrase
                     :recover-multiaccount-select-storage
                     :recover-multiaccount-enter-password
                     :recover-multiaccount-confirm-password
                     :recover-multiaccount-success]

              config/hardwallet-enabled?
              (concat [:hardwallet-authentication-method
                       :hardwallet-connect
                       :keycard-login-pin
                       :keycard-login-connect-card
                       :keycard-nfc-on
                       :keycard-blank
                       :keycard-wrong
                       :keycard-unpaired
                       :not-keycard
                       :hardwallet-setup
                       :hardwallet-success]))
   :config  (if
              ;; add view-id here if you'd like that view to be
              ;; first view when app is started
             (#{:login :progress :multiaccounts :enter-pin-login :keycard-login-pin} view-id)
              {:initialRouteName view-id}
              {:initialRouteName :login})})

(defn intro-stack []
  (-> (login-stack :intro)
      (update :screens conj
              :intro
              :intro-wizard
              :keycard-connection-lost
              :keycard-connection-lost-setup
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
              :keycard-recovery-intro
              :keycard-recovery-start
              :keycard-recovery-pair
              :keycard-recovery-recovering
              :keycard-recovery-success
              :keycard-recovery-no-key
              :keycard-recovery-pin)
      (assoc :name :intro-stack)
      (assoc :config {:initialRouteName :intro})))

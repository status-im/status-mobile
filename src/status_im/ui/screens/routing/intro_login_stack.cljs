(ns status-im.ui.screens.routing.intro-login-stack
  (:require [status-im.utils.config :as config]))

(def all-screens
  #{:login
    :progress
    :create-multiaccount
    :create-multiaccount-generate-key
    :create-multiaccount-choose-key
    :create-multiaccount-select-key-storage
    :create-multiaccount-create-code
    :create-multiaccount-confirm-code
    :recover-multiaccount-enter-phrase
    :recover-multiaccount-select-storage
    :recover-multiaccount-enter-password
    :recover-multiaccount-confirm-password
    :recover-multiaccount-success
    :multiaccounts
    :intro
    :intro-wizard
    :hardwallet-authentication-method
    :keycard-pairing
    :keycard-blank
    :keycard-wrong
    :keycard-unpaired
    :keycard-login-pin
    :not-keycard
    :keycard-onboarding-intro
    :keycard-onboarding-puk-code
    :keycard-onboarding-pin
    :keycard-onboarding-recovery-phrase
    :keycard-onboarding-recovery-phrase-confirm-word1
    :keycard-onboarding-recovery-phrase-confirm-word2
    :keycard-recovery-intro
    :keycard-recovery-pair
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
                     :create-multiaccount-generate-key
                     :create-multiaccount-choose-key
                     :create-multiaccount-select-key-storage
                     :create-multiaccount-create-code
                     :create-multiaccount-confirm-code
                     :recover-multiaccount-enter-phrase
                     :recover-multiaccount-select-storage
                     :recover-multiaccount-enter-password
                     :recover-multiaccount-confirm-password
                     :recover-multiaccount-success]

              config/hardwallet-enabled?
              (concat [:hardwallet-authentication-method
                       :keycard-login-pin
                       :keycard-blank
                       :keycard-wrong
                       :keycard-unpaired
                       :not-keycard]))
   :config  (if
                ;; add view-id here if you'd like that view to be
                ;; first view when app is started
             (#{:login :progress :multiaccounts :keycard-login-pin} view-id)
              {:initialRouteName view-id}
              {:initialRouteName :login})})

(defn intro-stack []
  (-> (login-stack :intro)
      (update :screens conj
              :intro
              :intro-wizard
              :keycard-pairing
              :keycard-onboarding-intro
              :keycard-onboarding-puk-code
              :keycard-onboarding-pin
              :keycard-onboarding-recovery-phrase
              :keycard-onboarding-recovery-phrase-confirm-word1
              :keycard-onboarding-recovery-phrase-confirm-word2
              :keycard-recovery-intro
              :keycard-recovery-pair
              :keycard-recovery-success
              :keycard-recovery-no-key
              :keycard-recovery-pin)
      (assoc :name :intro-stack)
      (assoc :config {:initialRouteName :intro})))

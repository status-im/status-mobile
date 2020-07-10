(ns status-im.ui.screens.routing.intro-login-stack
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.multiaccounts.login.views :as login]
            [status-im.ui.screens.progress.views :as progress]
            [status-im.ui.screens.multiaccounts.views :as multiaccounts]
            [status-im.ui.screens.intro.views :as intro]
            [status-im.ui.screens.keycard.onboarding.views :as keycard.onboarding]
            [status-im.ui.screens.keycard.recovery.views :as keycard.recovery]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.keycard.authentication-method.views
             :as
             keycard.authentication]
            [status-im.ui.screens.routing.core :as navigation]))

(defonce stack (navigation/create-stack))

;; NOTE(Ferossgp): There is a point at init when we do not know if there are
;; multiaccounts or no, and we show intro - we could show progress at this time
;; for a better UX
(views/defview intro-stack []
  (views/letsubs [multiaccounts [:multiaccounts/multiaccounts]
                  loading [:multiaccounts/loading]]
    [stack {:header-mode :none}
     [(cond
        loading
        {:name      :progress-start
         :component progress/progress}

        (empty? multiaccounts)
        {:name      :intro
         :component intro/intro}

        :else
        {:name      :multiaccounts
         :component multiaccounts/multiaccounts})
      {:name      :progress
       :component progress/progress}
      {:name      :login
       :component login/login}
      {:name      :create-multiaccount-generate-key
       :component intro/wizard-generate-key}
      {:name         :create-multiaccount-choose-key
       :back-handler :noop
       :component    intro/wizard-choose-key}
      {:name         :create-multiaccount-select-key-storage
       :back-handler :noop
       :component    intro/wizard-select-key-storage}
      {:name         :create-multiaccount-create-code
       :back-handler :noop
       :component    intro/wizard-create-code}
      {:name         :create-multiaccount-confirm-code
       :back-handler :noop
       :component    intro/wizard-confirm-code}
      {:name      :recover-multiaccount-enter-phrase
       :component intro/wizard-enter-phrase}
      {:name         :recover-multiaccount-select-storage
       :back-handler :noop
       :component    intro/wizard-select-key-storage}
      {:name         :recover-multiaccount-enter-password
       :back-handler :noop
       :component    intro/wizard-create-code}
      {:name         :recover-multiaccount-confirm-password
       :back-handler :noop
       :component    intro/wizard-confirm-code}
      {:name         :recover-multiaccount-success
       :back-handler :noop
       :component    intro/wizard-recovery-success}
      {:name         :keycard-onboarding-intro
       :back-handler :noop
       :component    keycard.onboarding/intro}
      {:name         :keycard-onboarding-puk-code
       :back-handler :noop
       :component    keycard.onboarding/puk-code}
      {:name         :keycard-onboarding-pin
       :back-handler :noop
       :component    keycard.onboarding/pin}
      {:name         :keycard-onboarding-recovery-phrase
       :back-handler :noop
       :component    keycard.onboarding/recovery-phrase}
      {:name         :keycard-onboarding-recovery-phrase-confirm-word1
       :back-handler :noop
       :component    keycard.onboarding/recovery-phrase-confirm-word}
      {:name         :keycard-onboarding-recovery-phrase-confirm-word2
       :back-handler :noop
       :component    keycard.onboarding/recovery-phrase-confirm-word}
      {:name         :keycard-recovery-intro
       :back-handler :noop
       :component    keycard.recovery/intro}
      {:name         :keycard-recovery-pair
       :back-handler :noop
       :component    keycard.recovery/pair}
      {:name         :keycard-recovery-success
       :back-handler :noop
       :insets       {:bottom true}
       :component    keycard.recovery/success}
      {:name      :keycard-recovery-no-key
       :component keycard.recovery/no-key}
      {:name      :keycard-recovery-pin
       :component keycard.recovery/pin}
      {:name      :keycard-authentication-method
       :component keycard.authentication/keycard-authentication-method}
      {:name      :keycard-login-pin
       :component keycard/login-pin}
      {:name      :keycard-blank
       :component keycard/blank}
      {:name      :keycard-wrong
       :component keycard/wrong}
      {:name      :keycard-unpaired
       :component keycard/unpaired}
      {:name      :not-keycard
       :component keycard/not-keycard}]]))

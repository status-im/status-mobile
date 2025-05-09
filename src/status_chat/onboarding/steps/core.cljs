(ns status-chat.onboarding.steps.core
  (:require [status-chat.onboarding.steps.biometrics :as biometrics]
            [status-chat.onboarding.steps.color :as profile-color]
            [status-chat.onboarding.steps.create-profile :as create-profile]
            [status-chat.onboarding.steps.password :as password]
            [status-chat.onboarding.steps.profile-name :as profile-name]))

(def steps
  {:create-password password/create-password-step
   :repeat-password password/repeat-password-step
   :biometrics      biometrics/biometrics-step
   :name            profile-name/name-step
   :color           profile-color/color-step
   :create-profile  create-profile/step})

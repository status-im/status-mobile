(ns status-im.ui.screens.routing.intro-login-stack
  (:require [status-im.utils.config :as config]))

(def all-screens
  #{:login
    :progress
    :create-account
    :recover
    :accounts
    :intro
    :hardwallet-authentication-method
    :hardwallet-connect
    :enter-pin
    :hardwallet-setup
    :hardwallet-success})

(defn intro-login-stack [view-id]
  {:name    :intro-login-stack
   :screens (cond-> [:login
                     :progress
                     :create-account
                     :recover
                     :accounts]
              (= :intro view-id)
              (conj :intro)

              config/hardwallet-enabled?
              (concat [:hardwallet-authentication-method
                       :hardwallet-connect
                       :enter-pin
                       :hardwallet-setup
                       :hardwallet-success]))
   :config  (if
             ;; add view-id here if you'd like that view to be
             ;; first view when app is started
             (#{:intro :login :progress :accounts} view-id)
              {:initialRouteName view-id}
              {:initialRouteName :login})})

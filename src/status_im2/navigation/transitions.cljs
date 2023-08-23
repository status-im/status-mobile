(ns status-im2.navigation.transitions
  (:require [react-native.core :as rn]
            [status-im2.constants :as constants]))

(def sign-in-modal-animations
  {:showModal    {:translationY {:from     (:height (rn/get-window))
                                 :to       0
                                 :duration constants/onboarding-modal-animation-duration}
                  :alpha        {:from     1
                                 :to       1
                                 :duration 0}}
   :dismissModal {:translationY {:from     0
                                 :to       (:height (rn/get-window))
                                 :duration constants/onboarding-modal-animation-duration}
                  :alpha        {:from     1
                                 :to       0
                                 :duration constants/onboarding-modal-animation-duration}}})

(def push-animations-for-transparent-background
  {:push {:content {:enter {:translationX {:from     (:width (rn/get-window))
                                           :to       0
                                           :duration constants/onboarding-modal-animation-duration}}
                    :exit  {:translationX {:from     0
                                           :to       (- (:width (rn/get-window)))
                                           :duration constants/onboarding-modal-animation-duration}}}}
   :pop  {:content {:exit  {:translationX {:from     0
                                           :to       (:width (rn/get-window))
                                           :duration constants/onboarding-modal-animation-duration}}
                    :enter {:translationX {:from     (- (:width (rn/get-window)))
                                           :to       0
                                           :duration constants/onboarding-modal-animation-duration}}}}})

(def new-to-status-modal-animations
  {:showModal    {:translationX {:from     (:width (rn/get-window))
                                 :to       0
                                 :duration constants/onboarding-modal-animation-duration}}
   :dismissModal {:translationX {:from     0
                                 :to       (:width (rn/get-window))
                                 :duration constants/onboarding-modal-animation-duration}}})

(ns status-im.navigation.transitions
  (:require
    [react-native.core :as rn]
    [status-im.constants :as constants]))

;;;; Modal Transitions
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

(def modal-animations-vertical
  {:showModal    {:translationY {:from     (:height (rn/get-window))
                                 :to       0
                                 :duration constants/onboarding-modal-animation-duration}}
   :dismissModal {:translationY {:from     0
                                 :to       (:height (rn/get-window))
                                 :duration constants/onboarding-modal-animation-duration}}})

;;;; Stack Transitions
(def stack-slide-transition
  {:push {:content {:translationX {:from     (:width (rn/get-window))
                                   :to       0
                                   :duration 250}}}
   :pop  {:content {:translationX {:from     0
                                   :to       (:width (rn/get-window))
                                   :duration 250}}}})

(def stack-transition-from-bottom
  {:push {:content {:translationY {:from     (:height (rn/get-window))
                                   :to       0
                                   :duration 250}}}
   :pop  {:content {:translationY {:from     0
                                   :to       (:height (rn/get-window))
                                   :duration 250}}}})

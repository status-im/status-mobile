(ns status-im.contexts.onboarding.create-password.style
  (:require
    [quo.foundations.colors :as colors]))

(def heading {:margin-bottom 20})
(def heading-subtitle {:color colors/white})
(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def info-message
  {:margin-top 8})

(def space-between-inputs {:height 16})

(def password-tips
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal 20})

(defn container
  [keyboard-shown footer-height]
  {:flex-grow      (if keyboard-shown 0 1)
   :padding-bottom (when-not keyboard-shown (+ footer-height 36))})

(def form-container {:flex 1 :justify-content :space-between})

(def top-part
  {:padding-horizontal 20
   :flex               0
   :margin-vertical    12})

(def disclaimer-container
  {:padding-horizontal 20
   :margin-vertical    4})

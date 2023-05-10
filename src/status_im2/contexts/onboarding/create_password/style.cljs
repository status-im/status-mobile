(ns status-im2.contexts.onboarding.create-password.style
  (:require [quo2.foundations.colors :as colors]))

(def heading {:margin-bottom 20})
(def heading-subtitle {:color colors/white})
(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def label-container
  {:margin-top     8
   :flex-direction :row
   :align-items    :center
   :height         16})

(def label-icon
  {:width        16
   :height       18
   :margin-right 4})

(defn label-icon-color
  [status]
  (get {:neutral colors/neutral-40
        :success colors/success-60
        :danger  colors/danger-60}
       status))

(defn label-color
  [status]
  (let [colors {:neutral colors/white-opa-70
                :success colors/success-60
                :danger  colors/danger-60}]
    {:color (get colors status)}))

(def space-between-inputs {:height 16})

(def password-tips
  {:flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 20})

(def top-part
  {:margin-horizontal 20
   :margin-top        12})

(def bottom-part
  {:flex            1
   :margin-top      12
   :justify-content :flex-end})

(def disclaimer-container
  {:margin-horizontal 20
   :margin-vertical   4})

(def button-container
  {:margin-horizontal 20
   :margin-vertical   12})

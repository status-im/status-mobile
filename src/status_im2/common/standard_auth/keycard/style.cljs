(ns status-im2.common.standard-auth.keycard.style
  (:require
    [quo2.foundations.colors :as colors]))

(def container
  {:flex 1})

(def inner-container
  {:flex               1
   :padding-horizontal 20})

(def context-tag
  {:margin-top     12
   :margin-bottom  56
   :flex-direction :row})

(def close-button
  {:margin-bottom 12})

(def secondary-text
  {:margin-top    8
   :margin-bottom 20})

(def try-again-button
  {:flex               1
   :padding-horizontal 20})

(def keycard
  {:margin-bottom 12})

(def divider
  {:margin-top 20})

(def reset-keycard-button
  {:align-self
   :flex-start
   :margin-top 12})

(defn digits-container
  [max-attempt-reached?]
  {:flex               1
   :padding-horizontal 60
   :align-items        :center
   :flex-direction     :row
   :justify-content    :space-between
   :margin-bottom      (if max-attempt-reached? 0 34)})

(defn digit
  [max-attempt-reached idx entered-numbers]
  {:width            16
   :height           16
   :border-radius    8
   :background-color (if (<= idx (dec (count entered-numbers)))
                       (if max-attempt-reached
                         colors/danger
                         colors/white)
                       colors/neutral-50)})

(def max-attempt-reached-container
  {:align-self    :center
   :margin-bottom 14
   :margin-top    8})

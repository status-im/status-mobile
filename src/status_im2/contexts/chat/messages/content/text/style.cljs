(ns status-im2.contexts.chat.messages.content.text.style
  (:require [quo2.foundations.colors :as colors]))

(def spacing-between-blocks 10)

(def parsed-text-block
  {:margin-top -4})

(def paragraph
  {:margin-top spacing-between-blocks})

(def block
  {:margin-top         spacing-between-blocks
   :border-radius      6
   :padding-horizontal 3})

(def quote
  {:margin-top        spacing-between-blocks
   :border-left-width 1
   :padding-left      10
   :border-left-color colors/neutral-40})

(defn code
  []
  {:background-color (colors/theme-colors colors/neutral-5 colors/white-opa-5)
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/white-opa-20)})

(def edited-block
  {:margin-top 4})

(defn edited-tag
  []
  {:font-size  11
   :margin-top 4
   :color      (colors/theme-colors colors/neutral-40 colors/neutral-50)})

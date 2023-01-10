(ns status-im2.contexts.chat.messages.content.text.style
  (:require
   [quo2.foundations.colors :as colors]))

(def block
  {:border-radius      6
   :padding-horizontal 3
   :transform          [{:translateY 4}]})

(def quote
  {:border-left-width 1
   :padding-left      10
   :border-left-color colors/neutral-40})

(def code
  {:background-color (colors/theme-colors colors/neutral-5 colors/white-opa-5)
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/white-opa-20)})

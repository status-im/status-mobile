(ns status-im2.contexts.chat.messages.content.text.style
  (:require [quo2.foundations.colors :as colors]))

(def block
  {:border-radius      6
   :padding-horizontal 3
   :transform          [{:translateY 4}]})

(def quote
  {:border-left-width 1
   :padding-left      10
   :border-left-color colors/neutral-40})

(defn code
  []
  {:background-color (colors/theme-colors colors/neutral-5 colors/white-opa-5)
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/white-opa-20)})

(defn message-default-style
  []
  {:font-family    "Inter-Regular"
   :color          (colors/theme-colors colors/neutral-100 colors/white)
   :font-size      15
   :line-height    21.75
   :letter-spacing -0.135})

(defn default-text-style
  []
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style                    (message-default-style)})

(defn edited-style
  []
  (cond->
    (update (default-text-style)
            :style
            assoc
            :color (colors/theme-colors colors/neutral-40 colors/neutral-50)
            :font-size 13
            :line-height 18.2
            :letter-spacing (typography/tracking 13))))

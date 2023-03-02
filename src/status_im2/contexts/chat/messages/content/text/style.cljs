(ns status-im2.contexts.chat.messages.content.text.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [status-im.ui.components.react :as react]))

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
  (assoc typography/paragraph-1 :color (colors/theme-colors colors/neutral-100 colors/white)))

(defn default-text-style
  []
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style                    (message-default-style)})

(defn edited-style
  []
  (-> (default-text-style)
      (update :style assoc :color (colors/theme-colors colors/neutral-40 colors/neutral-50))
      (update :style merge typography/paragraph-2)))

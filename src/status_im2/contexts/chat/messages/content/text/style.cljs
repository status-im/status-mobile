(ns status-im2.contexts.chat.messages.content.text.style
  (:require [quo2.foundations.colors :as colors]
            [quo.platform :as platform]))

(def block
  {:border-radius      6
   :padding-horizontal 3})

(def quote
  {:border-left-width 1
   :padding-left      10
   :border-left-color colors/neutral-40})

(defn mention-tag-wrapper
  [first-child-mention]
  {:flex-direction     :row
   :align-items        :center
   :height             (if platform/ios? 22 21)
   :background-color   colors/primary-50-opa-10
   :padding-horizontal 3
   :border-radius      6
   :transform          [{:translateY (if platform/ios? (if first-child-mention 4.5 3) 4.5)}]})

(def mention-tag-text
  {:color                 (colors/theme-colors colors/primary-50
                                               colors/primary-60)
   :selection-color       :transparent
   :suppress-highlighting true
   :line-height           21})

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

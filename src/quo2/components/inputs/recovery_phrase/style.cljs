(ns quo2.components.inputs.recovery-phrase.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def container
  {:min-height         40
   :flex               1
   :padding-vertical   4
   :padding-horizontal 20})

(defn input
  []
  (assoc (text/text-style {})
         :height              32
         :flex-grow           1
         :padding-vertical    5
         :text-align-vertical :top))

(defn placeholder-color
  [input-state override-theme blur?]
  (cond
    (and (= input-state :focused) blur?)
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 override-theme)

    (= input-state :focused) ; Not blur
    (colors/theme-colors colors/neutral-30 colors/neutral-60 override-theme)

    blur? ; :default & blur
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 override-theme)

    :else ; :default & not blur
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(defn cursor-color
  [customization-color override-theme]
  (colors/theme-colors (colors/custom-color customization-color 50)
                       (colors/custom-color customization-color 60)
                       override-theme))

(defn error-word
  []
  {:height             22
   :padding-horizontal 20
   :background-color   colors/danger-50-opa-10
   :color              (colors/theme-colors colors/danger-50 colors/danger-60)})

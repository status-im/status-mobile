(ns quo.components.selectors.disclaimer.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [blur? theme]
  (let [dark-background (if blur? colors/white-opa-5 colors/neutral-80-opa-40)
        dark-border     (if blur? colors/white-opa-10 colors/neutral-70)]
    {:flex-direction   :row
     :background-color (colors/theme-colors colors/neutral-5 dark-background theme)
     :padding          11
     :align-self       :stretch
     :border-radius    12
     :border-width     1
     :border-color     (colors/theme-colors colors/neutral-20 dark-border theme)}))

(def text
  {:margin-left 8})

(ns quo.components.wallet.missing-keypairs.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:border-width     1
   :border-radius    16
   :padding          8
   :border-color     colors/warning-50-opa-20
   :background-color colors/warning-50-opa-5})

(def title-icon-container
  {:top 1})

(def title-info-container
  {:padding-left 8
   :flex         1})

(def title-row
  {:display         :flex
   :flex-direction  :row
   :justify-content :space-between})

(def title-container
  {:align-items    :flex-start
   :flex-direction :row
   :padding-left   4
   :padding-bottom 12})

(defn subtitle
  [blur? theme]
  {:color (if blur?
            colors/white-opa-40
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(ns status-im.contexts.wallet.swap.set-spending-cap.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex       1
   :margin-top -20})

(def detail-item
  {:flex             1
   :height           36
   :background-color :transparent})

(def content-container
  {:padding-top        12
   :padding-horizontal 20
   :padding-bottom     32})

(def title-container
  {:margin-horizontal 4})

(def title-line-with-margin-top
  {:flex-direction :row
   :margin-top     4})

(def details-container
  {:flex-direction     :row
   :justify-content    :space-between
   :height             52
   :padding-top        7
   :padding-horizontal 1
   :margin-bottom      8})

(def summary-section-container
  {:padding-horizontal 20
   :padding-bottom     16})

(defn section-label
  [theme]
  {:margin-bottom 8
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

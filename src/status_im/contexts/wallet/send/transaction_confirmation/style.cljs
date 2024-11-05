(ns status-im.contexts.wallet.send.transaction-confirmation.style
  (:require [quo.foundations.colors :as colors]))

(def summary-container
  {:padding-horizontal 20
   :padding-bottom     16})

(def detail-item
  {:flex             1
   :height           36
   :background-color :transparent})

(def content-container
  {:padding-top        8
   :padding-horizontal 20
   :padding-bottom     32})

(def title-container
  {:margin-right 4})

(defn details-container
  [{:keys [loading-suggested-routes? route-loaded?]}]
  {:flex-direction    :row
   :width             "100%"
   :justify-content   (if route-loaded? :space-between :center)
   :height            (when (or loading-suggested-routes? route-loaded?) 52)
   :margin-horizontal 5
   :padding-top       7
   :margin-bottom     8})

(defn section-label
  [theme]
  {:margin-bottom 8
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

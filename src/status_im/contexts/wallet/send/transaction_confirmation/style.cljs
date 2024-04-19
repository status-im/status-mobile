(ns status-im.contexts.wallet.send.transaction-confirmation.style
  (:require [quo.foundations.colors :as colors]))

(def detail-item
  {:flex   1
   :height 36})

(def content-container
  {:padding-top        12
   :padding-horizontal 20
   :padding-bottom     32})

(def title-container
  {:margin-right 4})

(defn details-container
  [{:keys [loading-suggested-routes? route-loaded? theme]}]
  {:flex-direction     :row
   :justify-content    (if route-loaded? :space-between :center)
   :height             (when (or loading-suggested-routes? route-loaded?) 52)
   :padding-horizontal 12
   :padding-top        7
   :padding-bottom     8
   :border-radius      16
   :border-width       1
   :border-color       (if (or loading-suggested-routes? route-loaded?)
                         (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
                         :transparent)})

(def details-title-container
  {:padding-horizontal 20
   :padding-bottom     16})

(defn section-label
  [theme]
  {:margin-bottom 8
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

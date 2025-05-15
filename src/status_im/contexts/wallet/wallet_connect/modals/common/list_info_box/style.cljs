(ns status-im.contexts.wallet.wallet-connect.modals.common.list-info-box.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [theme]
  {:padding-horizontal 16
   :padding-vertical   12
   :border-radius      16
   :border-width       1
   :border-color       (colors/theme-colors colors/neutral-10 colors/black-opa-30 theme)
   :background-color   (colors/theme-colors colors/neutral-2_5 colors/black-opa-30 theme)})

(def title
  {:color         colors/neutral-50
   :margin-bottom 8})

(def item
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :gap            8})

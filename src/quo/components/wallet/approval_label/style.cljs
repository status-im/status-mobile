(ns quo.components.wallet.approval-label.style
  (:require [quo.foundations.colors :as colors]))

(def ^:const top-hole-view-height 24)

(defn container
  [customization-color theme]
  {:background-color           (colors/resolve-color customization-color theme 5)
   :align-items                :center
   :padding-horizontal         12
   :padding-vertical           8
   :padding-top                (+ 8 top-hole-view-height)
   :gap                        8
   :border-bottom-left-radius  16
   :border-bottom-right-radius 16
   :flex-direction             :row})

(def content
  {:flex-direction   :row
   :align-items      :center
   :flex             1
   :padding-vertical 4
   :gap              4})

(defn message
  [theme]
  {:flex  1
   :color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

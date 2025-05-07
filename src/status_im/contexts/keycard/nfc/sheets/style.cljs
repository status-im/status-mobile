(ns status-im.contexts.keycard.nfc.sheets.style
  (:require [quo.foundations.colors :as colors]))

(def ^:private sheet-border-radius 38)

(defn sheet
  [theme]
  {:position                :absolute
   :height                  380
   :bottom                  0
   :left                    0
   :right                   0
   :z-index                 1
   :align-items             :center
   :padding-horizontal      20
   :padding-top             20
   :padding-bottom          34
   :background-color        (colors/theme-colors colors/white colors/neutral-100 theme)
   :border-top-left-radius  sheet-border-radius
   :border-top-right-radius sheet-border-radius})

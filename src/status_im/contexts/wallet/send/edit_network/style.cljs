(ns status-im.contexts.wallet.send.edit-network.style
  (:require [quo.foundations.colors :as colors]))

(defn warning-container
  [color theme]
  {:flex-direction    :row
   :border-width      1
   :border-color      (colors/resolve-color color theme 10)
   :background-color  (colors/resolve-color color theme 5)
   :margin-horizontal 20
   :margin-top        4
   :margin-bottom     8
   :padding-left      12
   :padding-vertical  11
   :border-radius     12})

(def warning-text
  {:margin-left   8
   :margin-right  12
   :padding-right 12})

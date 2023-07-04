(ns status-im.ui.screens.wallet.components.styles
  (:require [quo.design-system.colors :as colors]))

(defn separator
  []
  {:height           1
   :background-color colors/gray-lighter})

(defn separator-dark
  []
  {:height           1
   :background-color colors/black-transparent})

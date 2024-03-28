(ns status-im.contexts.wallet.create-account.new-keypair.keypair-name.style
  (:require [quo.foundations.colors :as colors]))

(def header-container
  {:margin-horizontal 20
   :margin-top        12
   :margin-bottom     20})

(def bottom-action
  {:margin-horizontal -20})

(def error-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :align-self      :flex-start
   :margin-left     20
   :margin-vertical 8})

(defn error
  [theme]
  {:margin-left 4
   :color       (colors/resolve-color :danger theme)})

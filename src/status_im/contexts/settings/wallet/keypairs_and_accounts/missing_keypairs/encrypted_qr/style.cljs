(ns status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.encrypted-qr.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(defn container-main
  []
  {:background-color colors/neutral-95
   :padding-top      (safe-area/get-top)
   :flex             1})

(def page-container
  {:margin-top        14
   :margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def standard-auth
  {:margin-top 12
   :flex       1})

(def qr-container
  {:margin-top       12
   :background-color colors/white-opa-5
   :border-radius    20
   :flex             1
   :padding          12})

(def sub-text-container
  {:margin-bottom   8
   :justify-content :space-between
   :align-items     :center
   :flex-direction  :row})

(def valid-cs-container
  {:flex   1
   :margin 12})

(def warning-text
  {:margin-horizontal 16
   :margin-top        20
   :text-align        :center
   :color             colors/white-opa-70})

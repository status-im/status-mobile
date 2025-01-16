(ns status-im.contexts.wallet.add-account.create-account.style
  (:require
    [quo.foundations.colors :as colors]))

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     80})

(defn title-input-container
  [error?]
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     (if error? 8 16)})

(def color-picker-container
  {:padding-vertical 12})

(defn color-label
  [theme]
  {:color              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :padding-bottom     4
   :padding-horizontal 20})

(def slide-button-container {:z-index 1})

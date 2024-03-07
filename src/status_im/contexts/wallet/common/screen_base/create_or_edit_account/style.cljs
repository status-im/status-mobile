(ns status-im.contexts.wallet.common.screen-base.create-or-edit-account.style
  (:require [quo.foundations.colors :as colors]))

(defn root-container
  [top]
  {:flex       1
   :margin-top top})

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     76})

(def title-input-container
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     16})

(def error-container
  {:margin-horizontal 20
   :margin-bottom     16})

(def divider-1
  {:margin-bottom 12})

(def section-container
  {:padding-horizontal 20
   :padding-bottom     4})

(def color-picker-container
  {:padding-vertical 8})

(def color-picker
  {:padding-horizontal 20})

(def divider-2
  {:margin-top    4
   :margin-bottom 12})

(defn bottom-action
  [{:keys [bottom theme]}]
  {:padding-horizontal 20
   :padding-vertical   12
   :background-color   (colors/theme-colors colors/white colors/neutral-100 theme)
   :margin-bottom      bottom})

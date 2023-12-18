(ns status-im2.contexts.communities.actions.addresses-for-permissions.style
  (:require [quo.foundations.colors :as colors]))

(def container {:flex 1})

(def account-item-container
  {:font-size          30
   :border-radius      16
   :flex-direction     :row
   :border-width       1
   :height             56
   :padding-horizontal 12
   :align-items        :center
   :margin-bottom      8
   :gap                8
   :border-color       colors/neutral-90})

(def buttons
  {:flex-direction     :row
   :gap                12
   :padding-horizontal 20
   :padding-vertical   12})

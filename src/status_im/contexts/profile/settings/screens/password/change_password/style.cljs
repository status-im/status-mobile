(ns status-im.contexts.profile.settings.screens.password.change-password.style
  (:require
    [quo.foundations.colors :as colors]))

(def flex-fill {:flex 1})

(def heading {:margin-bottom 20})
(def heading-subtitle {:color colors/white})
(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def info-message
  {:margin-top 8})

(def space-between-inputs {:height 16})

(def password-tips
  {:flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 20})

(def top-part
  {:margin-horizontal 20
   :margin-top        12})

(def bottom-part
  {:flex            1
   :margin-top      12
   :justify-content :flex-end})

(def disclaimer-container
  {:margin-horizontal 20
   :margin-vertical   4})

(def button-container
  {:margin-horizontal 20
   :margin-vertical   12})

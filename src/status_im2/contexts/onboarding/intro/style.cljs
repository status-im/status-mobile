(ns status-im2.contexts.onboarding.intro.style
  (:require
    [quo2.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:flex      1
   :max-width 180
   :flex-wrap :wrap})

(def plain-text
  {:size   :paragraph-2
   :weight :regular
   :color  colors/white-opa-70})

(def highlighted-text
  {:flex   1
   :size   :paragraph-2
   :weight :regular
   :color  colors/white})

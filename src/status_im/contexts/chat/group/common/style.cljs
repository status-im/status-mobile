(ns status-im.contexts.chat.group.common.style
  (:require [quo.foundations.colors :as colors]))

(def avatar {:width 88 :margin-top 12 :margin-left 20})

(def camera {:position :absolute :right 0 :bottom 0})

(defn color-label
  [theme]
  {:color              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :padding-horizontal 20})

(def tags {:flex-direction :row :flex-wrap :wrap :padding-horizontal 20 :padding-top 12})

(def tag {:margin-right 8 :margin-bottom 8})

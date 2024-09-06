(ns status-im.contexts.chat.group.details.style
  (:require
    [quo.foundations.colors :as colors]))

(def actions-view
  {:margin-top         8
   :margin-bottom      20
   :padding-horizontal 20})

(defn close-icon
  [theme]
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :margin-left      20
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :margin-bottom    24})

(defn bottom-container
  [bottom theme]
  {:padding-horizontal 20
   :padding-vertical   12
   :margin-bottom      bottom
   :background-color   (colors/theme-colors colors/white colors/neutral-95-opa-70 theme)
   :flex-direction     :row})

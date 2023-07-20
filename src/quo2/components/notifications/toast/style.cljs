(ns quo2.components.notifications.toast.style
  (:require
    [quo2.foundations.colors :as colors]))

(def box-container
  {:margin-horizontal 12
   :border-radius     12
   :overflow          :hidden})

(def blur-container
  {:height           "100%"
   :width            "100%"
   :position         :absolute
   :padding-vertical 8
   :padding-left     10
   :padding-right    8})

(def content-container
  {:flex-direction   :row
   :justify-content  :space-between
   :padding-vertical 8
   :padding-left     10
   :padding-right    8
   :border-radius    12})

(defn title
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn text
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn icon
  [theme]
  {:color           (colors/theme-colors colors/white colors/neutral-100 theme)
   :container-style {:width 20 :height 20}})

(def left-side-container {:padding 2})
(def right-side-container {:padding 4 :flex 1})

(def action-container
  {:flex-direction     :row
   :padding-vertical   3
   :padding-horizontal 8
   :align-items        :center
   :border-radius      8})

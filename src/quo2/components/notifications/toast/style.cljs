(ns quo2.components.notifications.toast.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.shadows :as shadows]))

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
   :padding-right    8
   :background-color :transparent})

(defn content-container
  [override-theme]
  (merge
   (:shadow-1 shadows/normal-scale)
   {:background-color (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 override-theme)
    :flex-direction   :row
    :justify-content  :space-between
    :padding-vertical 8
    :padding-left     10
    :padding-right    8
    :border-radius    12}))

(defn title
  [override-theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 override-theme)})

(defn text
  [override-theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 override-theme)})

(defn icon
  [override-theme]
  {:color           (colors/theme-colors colors/white colors/neutral-100 override-theme)
   :container-style {:width 20 :height 20}})

(def left-side-container {:padding 2})
(def right-side-container {:padding 4 :flex 1})

(defn action-container
  [override-theme]
  {:background-color   (colors/theme-colors colors/white-opa-5 colors/neutral-80-opa-5 override-theme)
   :flex-direction     :row
   :padding-vertical   3
   :padding-horizontal 8
   :align-items        :center
   :border-radius      8})

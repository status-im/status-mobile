(ns quo.components.wallet.wallet-activity.style
  (:require
    [quo.foundations.colors :as colors]))

(defn wallet-activity-container
  [{:keys [pressed?
           theme
           blur?]}]
  (merge
   {:border-radius  16
    :padding-top    8
    :padding-left   12
    :padding-right  12
    :padding-bottom 12}
   (when pressed?
     {:background-color (if blur?
                          colors/white-opa-5
                          (colors/theme-colors colors/neutral-5 colors/neutral-90 theme))})))

(def transaction-header-container
  {:flex-direction :row
   :align-items    :center})

(defn transaction-header
  [theme]
  {:color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :margin-right 4})

(defn transaction-counter
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn transaction-counter-container
  [theme blur?]
  {:border-width       1
   :border-radius      6
   :margin-right       4
   :padding-horizontal 2
   :border-color       (if-not blur?
                         (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
                         colors/white-opa-10)})

(def timestamp-container
  {:margin-left 4
   :flex        1})

(defn timestamp
  [theme blur?]
  {:color (if-not blur?
            (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
            colors/white-opa-40)})

(defn prop-text
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(def icon-container
  {:width      32
   :height     32
   :margin-top 8})

(def container
  {:flex-direction :row
   :flex           1
   :column-gap     8})

(def content-line
  {:flex-direction  :row
   :margin-top      2
   :align-items     :center
   :column-gap      4
   :justify-content :flex-start})

(def icon-hole-view
  {:width           32
   :height          32
   :align-items     :center
   :justify-content :center})

(defn icon-circle-border
  [theme blur?]
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :width         32
   :height        32
   :border-width  1
   :border-radius 16
   :border-color  (if-not blur?
                    (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
                    colors/white-opa-5)})

(defn icon-color
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(def icon-status-container
  {:position :absolute
   :bottom   0
   :right    0})


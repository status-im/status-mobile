(ns quo2.components.settings.data-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [size card? theme blur?]
  {:width              335
   :flex-direction     :row
   :justify-content    :space-between
   :padding-vertical   (if (= size :default)
                         8
                         0)
   :padding-horizontal (if (= size :default)
                         12
                         0)
   :border-radius      16
   :border-width       (if (and card? (not= size :small))
                         1
                         0)
   :border-color       (cond
                         blur?            colors/white-opa-10
                         (= :light theme) colors/neutral-10
                         (= :dark theme)  colors/neutral-80)})

(defn loading-container
  [size theme blur?]
  {:width            (if (= size :default)
                       132
                       72)
   :height           (if (= size :default)
                       16
                       10)
   :background-color (cond
                       blur?            colors/white-opa-5
                       (= :light theme) colors/neutral-5
                       (= :dark theme)  colors/neutral-90)
   :border-radius    (if (= size :default)
                       6
                       3)
   :margin-vertical  (if (= size :default)
                       4
                       3)})

(def subtitle-container
  {:flex-direction :row})

(def right-container
  {:flex-direction :row
   :align-items    :center})

(defn subtitle-icon-container
  [description]
  {:margin-right    (if (not= :default description) 4 0)
   :justify-content :center})

(defn title
  [theme]
  {:color        (if (= theme :dark)
                   colors/neutral-40
                   colors/neutral-50)
   :margin-right 4})

(def title-container
  {:flex-direction :row
   :align-items    :center})

(def image
  {:width 16 :height 16})

(defn description
  [theme blur?]
  {:color (cond
            (or blur? (= :dark theme)) colors/white
            (= :light theme)           colors/neutral-100)})

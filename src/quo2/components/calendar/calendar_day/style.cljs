(ns quo2.components.calendar.calendar-day.style
  (:require [quo2.foundations.colors :as colors]))

(def wrapper
  {:flex            1
   :margin-vertical 2
   :justify-content :center
   :align-items     :center})

(def container-base
  {:align-items     :center
   :justify-content :center
   :border-radius   10
   :height          32
   :width           32})

(defn text-base
  [theme]
  {:color      (colors/theme-colors colors/neutral-100 colors/white theme)
   :text-align :center})

(defn in-range-background
  [{:keys [in-range theme]}]
  (cond-> {:position :absolute
           :top      0
           :right    0
           :left     0
           :bottom   0}
    (= in-range :start)
    (assoc :background-color
           (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
           :left 20)

    (= in-range :middle)
    (assoc :background-color
           (colors/theme-colors colors/neutral-5 colors/neutral-80 theme))

    (= in-range :end)
    (assoc :background-color
           (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
           :right 20)))

(defn container
  [{:keys [state theme customization-color]}]
  (cond-> container-base
    (= state :default)
    (assoc :background-color colors/neutral-100-opa-0)

    (= state :disabled)
    (assoc :opacity 0.3)

    (= state :selected)
    (assoc :background-color (colors/custom-color-by-theme customization-color 50 60 nil nil theme))))

(defn text
  [{:keys [state theme]}]
  (cond-> (text-base theme)
    (= state :selected) (assoc :color colors/white)))

(defn indicator
  [{:keys [state theme customization-color]}]
  {:width            4
   :position         :absolute
   :bottom           3
   :height           2
   :border-radius    8
   :background-color (if (= state :today)
                       (colors/custom-color-by-theme customization-color 50 60 nil nil theme)
                       colors/neutral-100-opa-0)})

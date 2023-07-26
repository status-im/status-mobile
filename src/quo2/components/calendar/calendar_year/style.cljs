(ns quo2.components.calendar.calendar-year.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]))

(def container-base
  {:align-items     :center
   :justify-content :center
   :border-radius   10
   :height          32
   :width           48})

(defn text-base
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn container
  [{:keys [selected? disabled? theme]}]
  (cond-> container-base
    disabled? (assoc :opacity 0.3)
    selected? (assoc :background-color
                     (colors/theme-colors colors/neutral-10 colors/neutral-70 theme))))

(defn text
  [{:keys [selected? theme]}]
  (cond-> (text-base theme)
    selected? (assoc :color (colors/theme-colors colors/neutral-100 colors/white theme))))

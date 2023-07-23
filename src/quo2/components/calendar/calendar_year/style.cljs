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
  []
  (-> typography/paragraph-2
      (merge typography/font-medium)
      (merge {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})))

(defn container
  [{:keys [selected? disabled?]}]
  (cond-> container-base
    disabled? (assoc :opacity 0.3)
    selected? (assoc :background-color (colors/theme-colors colors/neutral-10 colors/neutral-70))))

(defn text
  [{:keys [selected?]}]
  (cond-> (text-base)
    selected? (assoc :color (colors/theme-colors colors/neutral-100 colors/white))))

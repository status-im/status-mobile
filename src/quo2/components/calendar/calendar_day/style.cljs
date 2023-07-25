(ns quo2.components.calendar.calendar-day.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]))

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
  []
  (-> typography/paragraph-2
      (merge typography/font-medium)
      (merge
       {:color      (colors/theme-colors colors/neutral-100 colors/white)
        :text-align :center})))

(defn in-range-background
  [{:keys [in-range]}]
  (cond-> {:position :absolute
           :top      0
           :right    0
           :left     0
           :bottom   0}
    (= in-range :start)
    (assoc :background-color
           (colors/theme-colors colors/neutral-5 colors/neutral-80)
           :left 20)

    (= in-range :middle)
    (assoc :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80))

    (= in-range :end)
    (assoc :background-color
           (colors/theme-colors colors/neutral-5 colors/neutral-80)
           :right 20)))

(defn container
  [{:keys [state]}]
  (cond-> container-base
    (= state :default)
    (assoc :background-color colors/neutral-100-opa-0)

    (= state :disabled)
    (assoc :opacity 0.3)

    (= state :selected)
    (assoc :background-color (get-in colors/customization [:blue 50]))))

(defn text
  [{:keys [state]}]
  (cond-> (text-base)
    (= state :selected) (assoc :color colors/white)))

(defn indicator
  [{:keys [state]}]
  {:width            4
   :position         :absolute
   :bottom           3
   :height           2
   :border-radius    8
   :background-color (if (= state :today)
                       (get-in colors/customization [:blue 50])
                       colors/neutral-100-opa-0)})

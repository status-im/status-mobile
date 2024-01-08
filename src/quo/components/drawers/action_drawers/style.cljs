(ns quo.components.drawers.action-drawers.style
  (:require
    [quo.foundations.colors :as colors]))

(defn divider
  [theme]
  {:border-top-width 1
   :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :margin-top       8
   :margin-bottom    7
   :align-items      :center
   :flex-direction   :row})

(defn container
  [{:keys [sub-label disabled? state customization-color blur? theme]}]
  (cond-> {:border-radius     12
           :margin-horizontal 8}

    sub-label
    (assoc :height 56)

    (not sub-label)
    (assoc :height 48)

    disabled?
    (assoc :opacity 0.3)

    (= state :selected)
    (assoc :background-color
           (if blur?
             colors/white-opa-5
             (colors/resolve-color customization-color theme 5)))))

(defn row-container
  [sub-label]
  {:height            (if sub-label 56 48)
   :margin-horizontal 12
   :flex-direction    :row})

(defn left-icon
  [sub-label?]
  {:height        20
   :margin-top    (if sub-label? 8 :auto)
   :margin-bottom (when-not sub-label? :auto)
   :margin-right  12
   :width         20})

(def text-container
  {:flex            1
   :justify-content :center})

(def right-side-container
  {:flex-direction :row
   :align-items    :center})

(def right-icon
  {:height        20
   :margin-top    :auto
   :margin-bottom :auto
   :width         20})

(defn right-text
  [theme]
  {:color        (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :margin-right 12})

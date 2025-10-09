(ns quo.components.dropdowns.dropdown-input.style
  (:require
    [quo.foundations.colors :as colors]))

(def gap 8)

(def left-icon
  {:margin-right gap})

(defn right-icon
  [theme]
  {:color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :margin-right gap})

(defn header-label
  [theme blur?]
  {:color         (if blur?
                    (colors/theme-colors colors/neutral-80-opa-40
                                         colors/white-opa-70
                                         theme)
                    (colors/theme-colors colors/neutral-50
                                         colors/neutral-40
                                         theme))
   :margin-bottom gap})

(def root-container
  {:width  "100%"
   :height 40})

(defn container
  [{:keys [background-color border-color]}
   {:keys [icon? state]}]
  (cond-> {:align-items      :center
           :justify-content  :space-between
           :flex-direction   :row
           :padding-vertical 9
           :padding-left     16
           :padding-right    12
           :overflow         :hidden
           :background-color background-color
           :border-radius    12}

    icon?
    (assoc :padding-left 12)

    border-color
    (assoc :border-color border-color
           :border-width 1)

    (= state :disabled)
    (assoc :opacity 0.3)))

(def right-half-container
  {:flex-direction :row
   :align-items    :center})

(ns quo.components.community.channel-action.style
  (:require
    [quo.foundations.colors :as colors]))

(defn channel-action-container
  [{:keys [big? disabled?]}]
  (cond-> {}
    disabled?  (assoc :opacity 0.3)
    big?       (assoc :flex-grow 1)
    (not big?) (assoc :width 104)))

(defn channel-action
  [{:keys [color pressed? theme]}]
  {:height           102
   :padding          12
   :border-radius    16
   :background-color (colors/resolve-color color theme (if pressed? 20 10))
   :justify-content  :space-between})

(def channel-action-row
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

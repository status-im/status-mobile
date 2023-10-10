(ns quo2.components.tabs.tabs.style
  (:require [utils.number]))

(def linear-gradient
  {:width  "100%"
   :height "100%"})

(defn tab
  [{:keys [size default-tab-size number-of-items index style]}]
  {:margin-right  (if (= size default-tab-size) 12 8)
   :padding-right (when (= index (dec number-of-items))
                    (:padding-left style))})

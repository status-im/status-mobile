(ns quo.components.common.new-feature-dot
  (:require [quo.components.common.new-feature-gradient :as new-feature-gradient]))

(def ^:const size 8)

(defn view
  [{:keys [style accessibility-label]}]
  [new-feature-gradient/view
   {:style               (merge {:width         size
                                 :height        size
                                 :border-radius (/ size 2)}
                                style)
    :accessibility-label accessibility-label}])

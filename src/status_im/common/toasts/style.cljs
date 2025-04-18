(ns status-im.common.toasts.style
  (:require [react-native.safe-area :as safe-area]))

(defn outmost-transparent-container
  []
  {:elevation       2
   :pointer-events  :box-none
   :padding-top     (+ safe-area/top 6)
   :flex-direction  :column
   :justify-content :center
   :align-items     :center})

(def each-toast-container
  {:width         "100%"
   :margin-bottom 5})

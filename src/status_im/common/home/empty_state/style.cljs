(ns status-im.common.home.empty-state.style
  (:require
    [react-native.safe-area :as safe-area]
    [status-im.common.home.constants :as constants]))

(defn empty-state-container
  []
  {:flex            1
   :margin-top      (+ constants/header-height safe-area/top)
   :margin-bottom   44
   :justify-content :center})

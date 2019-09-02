(ns status-im.ui.components.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]))

(defn toolbar [{:keys [center left right]}]
  (if center
    [react/view {:height 52 :align-items :center :justify-content :center}
     [button/button center]]
    [react/view {:height 52 :align-items :center :flex-direction :row}
     (when left
       [button/button left])
     [react/view {:flex 1}]
     (when right
       [button/button right])]))
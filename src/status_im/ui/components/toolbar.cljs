(ns status-im.ui.components.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]))

(defn toolbar [{:keys [center left right show-border?]}]
  (if center
    [react/view (merge {:height 52 :align-items :center :justify-content :center}
                       (when show-border? {:border-top-width 1 :border-top-color colors/gray-lighter}))
     [button/button center]]
    [react/view (merge {:height 52 :align-items :center :flex-direction :row}
                       (when show-border? {:border-top-width 1 :border-top-color colors/gray-lighter}))
     (when left
       [button/button left])
     [react/view {:flex 1}]
     (when right
       [button/button right])]))
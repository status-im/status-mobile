(ns status-im.ui.components.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]))

(defn toolbar-container [{:keys [show-border? size center?]
                          :or   {size :default}}]
  (merge {:align-items        :center
          :padding-horizontal 16
          :flex-direction     :row}
         (when center?
           {:justify-content :center})
         (when show-border?
           {:border-top-width 1
            :border-top-color colors/gray-lighter})
         (case size
           :large {:height 60}
           {:height 52})))

(defn toolbar [{:keys [center left right show-border? size]}]
  (if center
    [react/view {:style (toolbar-container {:show-border? show-border?
                                            :center?      true
                                            :size         size})}
     [button/button center]]
    [react/view {:style (toolbar-container {:show-border? show-border?
                                            :center?      false
                                            :size         size})}
     (when left
       [button/button left])
     [react/view {:flex 1}]
     (when right
       [button/button right])]))

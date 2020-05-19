(ns status-im.ui.components.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn toolbar-container [{:keys [show-border? size center?]
                          :or   {size :default}}]
  (merge {:align-items        :center
          :padding-horizontal 8
          :flex-direction     :row}
         (when center?
           {:justify-content :center})
         (when show-border?
           {:border-top-width 1
            :border-top-color colors/gray-lighter})
         (case size
           :large {:height 60}
           {:height 52})))

;; TODO(Ferossgp): Allow components when moving to Quo
(defn toolbar [{:keys [center left right show-border? size]}]
  (if center
    [react/view {:style (toolbar-container {:show-border? show-border?
                                            :center?      true
                                            :size         size})}
     center]
    [react/view {:style (toolbar-container {:show-border? show-border?
                                            :center?      false
                                            :size         size})}
     (when left left)
     [react/view {:flex 1}]
     (when right right)]))

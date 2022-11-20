(ns status-im.ui2.components.toolbar
  (:require [status-im.ui.components.react :as react]
            [quo.design-system.colors :as colors]))

(defn toolbar-container [{:keys [show-border? size center? margin-bottom]
                          :or   {size :default}}]
  (merge {:align-items        :center
          :padding-horizontal 8
          :margin-bottom      (or margin-bottom 0)
          :width              "100%"
          :flex-direction     :row
          :justify-content    :space-between}
         (when center?
           {:justify-content :center})
         (when show-border?
           {:border-top-width 1
            :border-top-color colors/gray-lighter})
         (case size
           :large {:height 60}
           {:height 52})))

;; TODO(Ferossgp): Allow components when moving to Quo
(defn toolbar [{:keys [center left right show-border? size margin-bottom]}]
  (if center
    [react/view {:style (toolbar-container {:show-border?  show-border?
                                            :center?       true
                                            :margin-bottom margin-bottom
                                            :size          size})}
     center]
    [react/view {:style (toolbar-container {:show-border?  show-border?
                                            :margin-bottom margin-bottom
                                            :center?       false
                                            :size          size})}
     [react/view {:flex-shrink 1}
      (when left left)]
     [react/view
      (when right right)]]))

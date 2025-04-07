(ns legacy.status-im.ui.components.toolbar
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]))

(defn toolbar-container
  [{:keys [show-border? size center?]
    :or   {size :default}}]
  (merge {:align-items        :center
          :padding-horizontal 8
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
(defn toolbar
  [{:keys [center left right show-border? size]}]
  (if center
    [react/view
     {:style (toolbar-container {:show-border? show-border?
                                 :center?      true
                                 :size         size})}
     center]
    [react/view
     {:style (toolbar-container {:show-border? show-border?
                                 :center?      false
                                 :size         size})}
     [react/view {:flex-shrink 1}
      (when left left)]
     [react/view
      (when right right)]]))

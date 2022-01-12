(ns status-im.ui.components.image-with-loader
  (:require [reagent.core :as reagent]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]))

(defn image-with-loader [props]
  (let [loaded? (reagent/atom nil)
        {:keys [source style]} props]
    (fn []
      [react/view
       (when (or (nil? @loaded?) @loaded?)
         [react/fast-image {:onLoad #(reset! loaded? true)
                            :style (if @loaded?
                                     style
                                     {})
                            :source source}])
       (when-not @loaded?
         [react/view {:style (merge style
                                    {:align-items :center
                                     :justify-content :center
                                     :background-color colors/gray-lighter})}
          [react/activity-indicator {:animating true}]])])))

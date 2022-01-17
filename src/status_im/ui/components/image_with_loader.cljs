(ns status-im.ui.components.image-with-loader
  (:require [reagent.core :as reagent]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]))

(defn- placeholder [props child]
  (let [{:keys [style]} props]
    [react/view {:style (merge style
                               {:align-items :center
                                :justify-content :center
                                :background-color colors/gray-lighter})}
     child]))

(defn image-with-loader [props]
  (let [{:keys [source style]} props
        loaded? (reagent/atom false)
        error? (reagent/atom false)]
    (fn []
      [react/view
       (when @error?
         [placeholder {:style style}
          [icons/icon :main-icons/cancel]])
       (when-not (or @loaded? @error?)
         [placeholder {:style style}
          [react/activity-indicator {:animating true}]])
       (when (not @error?)
         [react/fast-image {:onError #(reset! error? true)
                            :onLoad #(reset! loaded? true)
                            :style (if @loaded? style {})
                            :source source}])])))

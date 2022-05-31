(ns status-im.ui.components.fast-image
  (:require [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]))

(defn placeholder [style child]
  [react/view {:style (merge style {:flex 1 :justify-content :center :align-items :center})}
   child])

(defn fast-image [props]
  (let [loaded? (reagent/atom false)
        error? (reagent/atom false)]
    (fn []
      [react/fast-image-class (merge
                               {:on-error #(reset! error? true)
                                :on-load #(reset! loaded? true)}
                               props)
       (when (or @error? (not @loaded?))
         [placeholder (:style props)
          (if @error?
            [icons/icon :main-icons/cancel]
            (when-not @loaded?
              [react/activity-indicator {:animating true}]))])])))
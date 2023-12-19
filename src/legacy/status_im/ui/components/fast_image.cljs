(ns legacy.status-im.ui.components.fast-image
  (:require
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [reagent.core :as reagent]))

(defn placeholder
  [style child]
  [react/view {:style (merge style {:flex 1 :justify-content :center :align-items :center})}
   child])

(defn fast-image
  [_]
  (let [loaded? (reagent/atom false)
        error?  (reagent/atom false)]
    (fn [props]
      [react/fast-image-class
       (merge
        props
        {:on-error (fn [e]
                     (when-let [on-error (:on-error props)]
                       (on-error e))
                     (reset! error? true))
         :on-load  (fn [e]
                     (when-let [on-load (:on-load props)]
                       (on-load e))
                     (reset! loaded? true)
                     (reset! error? false))})
       (when (or @error? (not @loaded?))
         [placeholder (:style props)
          (if @error?
            [icons/icon :main-icons/cancel]
            (when-not @loaded?
              [react/activity-indicator {:animating true}]))])])))

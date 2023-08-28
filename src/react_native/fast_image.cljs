(ns react-native.fast-image
  (:require ["react-native-fast-image" :as FastImage]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def fast-image-class (reagent/adapt-react-class ^js FastImage))

(defn placeholder
  [style child]
  [rn/view {:style (merge style {:flex 1 :justify-content :center :align-items :center})}
   child])

(defn fast-image
  [_]
  (let [loaded? (reagent/atom false)
        error?  (reagent/atom false)]
    (fn [{:keys [source] :as props}]
      [fast-image-class
       (merge
        props
        {:source   (if (string? source)
                     {:uri source}
                     source)
         :on-error (fn [e]
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
            [rn/text "X"]
            (when-not @loaded?
              [rn/activity-indicator {:animating true}]))])])))

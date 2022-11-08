(ns quo2.components.tags.base-tag
  (:require [react-native.core :as rn]))

(defn style-container [size disabled border-color border-width background-color label type]
  (merge {:height             size
          :border-color       border-color
          :background-color   background-color
          :border-width       border-width
          :border-radius      size
          :align-items        :center
          :justify-content    :center}
         (when disabled
           {:opacity 0.3})
         (when (and (or (= type :icon) (= type :emoji)) (not label))
           {:width       size})))

(defn base-tag
  "opts
   {:type :icon/:emoji/:label/:permission
    :size 32/24}
    :labelled true"
  [_]
  (fn [{:keys [id size disabled border-color border-width background-color on-press
               accessibility-label label type] :or   {size 32}} children]
    [rn/touchable-without-feedback (merge {:disabled            disabled
                                           :accessibility-label accessibility-label}
                                          (when on-press
                                            {:on-press #(on-press id)}))
     [rn/view {:style (merge (style-container size disabled border-color border-width
                                              background-color label type))}
      children]]))

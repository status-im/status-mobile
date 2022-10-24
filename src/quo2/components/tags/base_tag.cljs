(ns quo2.components.tags.base-tag
  (:require [react-native.core :as rn]))

<<<<<<< HEAD
(defn style-container
  [size disabled border-color border-width background-color label type]
  (merge {:height           size
          :border-color     border-color
          :background-color background-color
          :border-width     border-width
          :border-radius    size
          :align-items      :center
          :justify-content  :center}
         (when disabled
           {:opacity 0.3})
         (when (and (or (= type :icon) (= type :emoji)) (not label))
           {:width size})))
=======
(defn style-container [size disabled? border-color border-width background-color labelled? type]
  (merge {:height             size
          :border-color       border-color
          :background-color   background-color
          :border-width       border-width
          :border-radius      size
          :align-items        :center
          :justify-content    :center}
         (when disabled?
           {:opacity 0.3})
         (when (and (or (= type :icon) (= type :emoji)) (not labelled?))
           {:width       size})))
>>>>>>> f6cfb0aa0 (refactored scrollable-tags to share the same logic with scrollable-tabs)

(defn base-tag
  [_]
<<<<<<< HEAD
  (fn [{:keys [id size disabled border-color border-width background-color on-press
               accessibility-label label type]
        :or   {size 32}} children]
    [rn/touchable-without-feedback
     (merge {:disabled            disabled
             :accessibility-label accessibility-label}
            (when on-press
              {:on-press #(on-press id)}))
     [rn/view
      {:style (merge (style-container size
                                      disabled
                                      border-color
                                      border-width
                                      background-color
                                      label
                                      type))}
=======
  (fn [{:keys [id size disabled? border-color border-width background-color on-press
               accessibility-label labelled? type] :or   {size 32}} children]
    [rn/touchable-without-feedback (merge {:disabled            disabled?
                                           :accessibility-label accessibility-label}
                                          (when on-press
                                            {:on-press #(on-press id)}))
     [rn/view {:style (merge (style-container size disabled? border-color border-width
                                              background-color labelled? type))}
>>>>>>> f6cfb0aa0 (refactored scrollable-tags to share the same logic with scrollable-tabs)
      children]]))

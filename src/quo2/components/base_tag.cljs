(ns quo2.components.base-tag
  (:require
   [quo.react-native :as rn]
   [quo2.components.icon :as icons]
   [quo2.components.text :as text]))

(defn style-container [size disabled border-color background-color label]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     :row
          :border-color       border-color
          :background-color   background-color
          :border-width       1
          :border-radius      size}
         (when disabled
           {:opacity 0.3})
         (if label
           {:padding-horizontal (case size 32 12 24 8)}
           {:width              size})))

(defn base-tag [_]
  (fn [{:keys [size text-color icon emoji icon-color disabled label border-color background-color]
        :or   {size 32}}]
    [rn/view {:style (style-container size disabled border-color background-color label)}
     (when icon
       [icons/icon icon {:container-style (when label
                                            {:margin-right 4})
                         :resize-mode      :center
                         :size             (case size
                                             32 20
                                             24 12)
                         :color            icon-color}])
     (when emoji
       [rn/image {:source emoji
                  :style  (merge (case size
                                   32 {:height 20
                                       :width  20}
                                   24 {:height 12
                                       :width  12})
                                 (when label
                                   {:margin-right 4}))}])
     (when label
       [text/text (merge {:size            (case size
                                             32 :paragraph-1
                                             24 :paragraph-2
                                             20 :label nil)
                          :weight          :medium
                          :number-of-lines 1}
                         text-color)
        label])]))

(ns quo2.components.settings.reorder-item.items.item-tabs
  (:require [quo2.components.tabs.segmented-tab :as quo2]
            [quo.react-native :as rn]
            [quo.components.text :as text]
            [quo2.components.settings.reorder-item.style :as style]
            [quo2.components.icon :as quo2-icons]))

(defn transform-data
  [data]
  (map #(assoc %
               :id    (:id %)
               :label [rn/view
                       {:style style/tab-item-container}
                       (if (contains? % :image)
                         [rn/image
                          {:source (:image %)
                           :style  style/tab-item-image}]
                         [rn/view {:style style/tab-item-image}
                          (quo2-icons/icon (:icon %) (style/tab-icon))])
                       [text/text
                        {:style style/tab-item-label
                         :width :medium}
                        (:label %)]])
       data))

(defn view
  [data default-active]
  [quo2/segmented-control
   {:default-active              default-active
    :size                        32
    :blur?                       false
    :container-style             (style/tab-container)
    :item-container-style        (style/segmented-tab-item-container)
    :active-item-container-style (style/active-segmented-tab-item-container)
    :data                        (transform-data data)
    :on-change                   #(println "Active tab" %)}])

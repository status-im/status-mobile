(ns quo2.components.list.sortable-list.items.item-tabs
  (:require [quo2.components.tabs.segmented-tab :as quo2]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo.components.text :as text]
            [quo2.components.list.sortable-list.style :as style]))

(defn transform-data
  [data]
  (map #(assoc %
               :id (:id %)
               :label [rn/view
                       {:style style/tab-item-container}
                       (if (contains? % :image)
                         [rn/image
                          {:source (:image %)
                           :style style/tab-item-image}]
                         [rn/view {:style style/tab-item-image} (:icon %)]
                         ) 
                       [text/text 
                        {:style style/tab-item-label 
                         :width :medium} 
                        (:label %)]])
       data))

(defn view
  [data default-active]
  [quo2/segmented-control
   {:default-active default-active
    :size  32
    :blur? false
    :container-style style/tab-container
    :item-container-style style/segmented-tab-item-container
    :inactive-background-color colors/neutral-30
    :data           (transform-data data)
    :on-change      #(println "Active tab" %)}])
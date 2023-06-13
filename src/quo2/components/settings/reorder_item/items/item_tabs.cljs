(ns quo2.components.settings.reorder-item.items.item-tabs
  (:require [quo2.components.tabs.segmented-tab :as quo2]
            [quo.react-native :as rn]
            [quo.components.text :as text]
            [quo2.components.settings.reorder-item.style :as style]
            [quo2.components.icon :as quo2-icons]))

(defn render-tab-item
  [item]
  (let [tab-image (cond
                    (item :image) [rn/image
                                   {:source (:image item)
                                    :style  style/tab-item-image}]
                    (item :icon) [rn/view {:style style/tab-item-image}
                                  (quo2-icons/icon (:icon item) (style/tab-icon))])]
    [rn/view
     {:style style/tab-item-container}
     tab-image
     [text/text
      {:style style/tab-item-label
       :width :medium}
      (:label item)]]))

(defn transform-data
  [data]
  (map #(hash-map :id (:id %) :label (render-tab-item %)) data))

(defn view
  [{:keys [data default-active on-change]
    :or   {data nil
           default-active 1
           on-change (fn [] nil)}}] 
    [quo2/segmented-control
     {:default-active              default-active
      :size                        32
      :blur?                       false
      :container-style             (style/tab-container)
      :item-container-style        (style/segmented-tab-item-container)
      :active-item-container-style (style/active-segmented-tab-item-container)
      :data                        (transform-data data)
      :on-change                   on-change}])

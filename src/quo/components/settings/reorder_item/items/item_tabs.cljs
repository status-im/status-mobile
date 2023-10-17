(ns quo.components.settings.reorder-item.items.item-tabs
  (:require
    [quo.components.icon :as quo-icons]
    [quo.components.markdown.text :as text]
    [quo.components.settings.reorder-item.style :as style]
    [quo.components.tabs.segmented-tab :as quo]
    [react-native.core :as rn]))

(defn render-tab-item
  [item]
  (let [tab-image (cond
                    (item :image) [rn/image
                                   {:source (:image item)
                                    :style  style/tab-item-image}]
                    (item :icon)  [rn/view {:style style/tab-item-image}
                                   (quo-icons/icon (:icon item) (style/tab-icon))])]
    [rn/view
     {:style style/tab-item-container}
     tab-image
     [text/text
      {:size   :paragraph-1
       :weight :medium}
      (:label item)]]))

(defn transform-data
  [data]
  (map #(hash-map :id (:id %) :label (render-tab-item %)) data))

(defn view
  [{:keys [data default-active on-change]
    :or   {default-active 1
           on-change      (constantly nil)}}]
  [quo/segmented-control
   {:default-active              default-active
    :size                        32
    :blur?                       false
    :container-style             (style/tab-container)
    :item-container-style        (style/segmented-tab-item-container)
    :active-item-container-style (style/active-segmented-tab-item-container)
    :data                        (transform-data data)
    :on-change                   on-change}])

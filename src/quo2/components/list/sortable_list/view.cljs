(ns quo2.components.list.sortable-list.view 
  (:require [quo.react-native :as rn]
            [react-native.platform :as platform]
            [quo2.components.list.sortable-list.item :as item]
            [reagent.core :as reagent]
            [quo2.components.list.sortable-list.style :as style]
            [quo2.components.icon :as quo2-icons]
            [quo2.components.list.sortable-list.item-placeholder :as placeholder]
            [quo2.components.list.sortable-list.item-skeleton :as skeleton]
            [quo2.components.list.sortable-list.item-tabs :as tab]
            [status-im2.common.resources :as resources]))

(def data (reagent/atom
                [{:id 1
                  :type "item"
                  :image (resources/get-mock-image :diamond)
                  :image-size 24
                  :right-icon [quo2-icons/icon :i/world style/right-icon]
                  :title "Trip to Bahamas"}
                 {:id 2
                  :type "item"
                  :image (resources/get-mock-image :status-logo)
                  :image-size 24
                  :right-icon [quo2-icons/icon :i/world style/right-icon]
                  :title "Status"}
                 {:id 3
                  :type "item"
                  :image (resources/tokens :eth)
                  :image-size 24
                  :right-icon [quo2-icons/icon :i/world style/right-icon]
                  :title "Ethereum"}
                 {:id 4
                  :type "item"
                  :image (resources/get-mock-image :monkey)
                  :image-size 36
                  :right-icon [quo2-icons/icon :i/world style/right-icon]
                  :title "3045"
                  :subtitle "Bored Ape Yatch Club"}
                 {:id 5
                  :type "item"
                  :image (resources/get-mock-image :pinterest)
                  :image-size 24
                  :right-text "@sheralito"
                  :title "Pinterest"}
                 {:id 6
                  :type "placeholder"
                  :label "Label"}
                 {:id 7
                  :type "placeholder"
                  :label "Label"}
                 {:id 8
                  :type "skeleton"}
                 {:id 9
                  :type "skeleton"}
                 {:id 10
                  :type "tab"
                  :default-active 1
                  :data [{:id 1
                          :label "Everyone"
                          :icon (quo2-icons/icon :i/world style/right-icon)}
                         {:id 2
                          :label "Contacts"
                          :image (resources/get-mock-image :contact)}
                         {:id 3
                          :label "Verified"
                          :image (resources/get-mock-image :verified)}]}
                 {:id 11
                  :type "tab"
                  :default-active 1
                  :data [{:id 1
                          :label "Everyone"
                          :icon (quo2-icons/icon :i/world style/right-icon)}
                         {:id 2
                          :label "Contacts"
                          :image (resources/get-mock-image :contact)}
                         {:id 3
                          :label "Verified"
                          :image (resources/get-mock-image :verified)}]}]))

(defn render-fn
  [item _ _ _ _ drag] 
  (let [label-value (:label item)
        type (:type item)
        data (:data item)
        default-active (:default-active item)]

    (case type 
      "item" [item/view item drag]
      "placeholder" [placeholder/view label-value drag]
      "skeleton" [skeleton/view]
      "tab" [tab/view data default-active])))

(defn on-drag-end-fn
  [_ _ data-js]
  (reset! data data-js)
  (reagent/flush)
)

(defn reorder-list []
  [rn/view
   {:style style/container}
   [rn/draggable-flat-list
    {:key-fn               :id
     :data                 @data
     :render-fn            render-fn
     :autoscroll-threshold (if platform/android? 150 250)
     :autoscroll-speed     (if platform/android? 10 150)
     :content-container-style {:padding-vertical 20}
     :shows-vertical-scroll-indicator false
     :on-drag-end-fn       on-drag-end-fn}]])
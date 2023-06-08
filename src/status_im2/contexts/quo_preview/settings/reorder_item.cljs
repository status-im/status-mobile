(ns status-im2.contexts.quo-preview.settings.reorder-item
  (:require
   [quo2.core :as quo]
   [react-native.core :as rn]
   [status-im2.common.resources :as resources]))

(def mock-data
  [{:id 1
    :type "item"
    :image (resources/get-mock-image :diamond)
    :image-size 21
    :right-icon :i/world
    :title "Trip to Bahamas"}
   {:id 2
    :type "item"
    :image (resources/get-mock-image :status-logo)
    :image-size 21
    :right-icon :i/world
    :title "Status"}
   {:id 3
    :type "item"
    :image (resources/tokens :eth)
    :image-size 21
    :right-icon :i/world
    :title "Ethereum"}
   {:id 4
    :type "item"
    :image (resources/get-mock-image :monkey)
    :image-size 30
    :right-icon :i/world
    :title "3045"
    :subtitle "Bored Ape Yatch Club"}
   {:id 5
    :type "item"
    :image (resources/get-mock-image :pinterest)
    :image-size 21
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
            :icon :i/world}
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
            :icon :i/world}
           {:id 2
            :label "Contacts"
            :image (resources/get-mock-image :contact)}
           {:id 3
            :label "Verified"
            :image (resources/get-mock-image :verified)}]}])


(defn preview-reorder-item
  []
  [rn/view
   [quo/reorder-item mock-data]])

(ns status-im2.contexts.quo-preview.settings.reorder-item
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [quo2.foundations.resources :as quo2.resources]
    [quo2.foundations.colors :as colors]
    [quo2.components.settings.reorder-item.types :as types]))

(def mock-data
  [{:id   1
    :type types/item
    :data {:on-press   (println "pressed")
           :image      (resources/get-mock-image :diamond)
           :image-size 21
           :right-icon :i/world
           :title      "Trip to Bahamas"}}
   {:id   2
    :type types/item
    :data {:image      (resources/get-mock-image :status-logo)
           :image-size 21
           :right-icon :i/world
           :title      "Status"}}
   {:id   3
    :type types/item
    :data {:image      (quo2.resources/get-token :eth)
           :image-size 21
           :right-icon :i/world
           :title      "Ethereum"}}
   {:id   4
    :type types/item
    :data {:image      (resources/get-mock-image :monkey)
           :image-size 30
           :right-icon :i/world
           :title      "3045"
           :subtitle   "Bored Ape Yatch Club"}}
   {:id   5
    :type types/item
    :data {:image      (resources/get-mock-image :pinterest)
           :image-size 21
           :right-text "@sheralito"
           :title      "Pinterest"}}
   {:id   6
    :type types/placeholder
    :data {:label "Label"}}
   {:id   7
    :type types/placeholder
    :data {:label "Label"}}
   {:id   8
    :type types/skeleton}
   {:id   9
    :type types/skeleton}
   {:id   10
    :type types/tab
    :data {:data           [{:id    1
                             :label "Everyone"
                             :icon  :i/world}
                            {:id    2
                             :label "Contacts"
                             :image (resources/get-mock-image :contact)}
                            {:id    3
                             :label "Verified"
                             :image (resources/get-mock-image :verified)}]
           :on-change      (fn [tab-id] (println tab-id))
           :default-active 2}}
   {:id   11
    :type types/tab
    :data {:data           [{:id    1
                             :label "Everyone"
                             :icon  :i/world}
                            {:id    2
                             :label "Contacts"
                             :image (resources/get-mock-image :contact)}
                            {:id    3
                             :label "Verified"
                             :image (resources/get-mock-image :verified)}]
           :on-change      (fn [tab-id] (println tab-id))
           :default-active 1}}])


(defn container
  []
  {:padding-horizontal 38
   :padding-vertical   20
   :background-color   (colors/theme-colors
                        colors/neutral-10
                        colors/neutral-100)
   :flex               1})

(defn preview-reorder-item
  []
  [rn/scroll-view
   {:style (container)}
   (for [item mock-data]
     [quo/reorder-item (item :data) (item :type)])])

(ns status-im.contexts.preview-screens.quo-preview.settings.reorder-item
  (:require
    [quo.components.settings.reorder-item.types :as types]
    [quo.core :as quo]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def mock-data
  [{:id   1
    :type types/item
    :data {:image      (resources/get-mock-image :diamond)
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
    :data {:image      (resources/get-mock-image :status-logo)
           :image-size 21
           :right-icon :i/world
           :title      "Status"}}
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

(defn view
  []
  (fn []
    [preview/preview-container {}
     (for [item mock-data]
       [quo/reorder-item (item :data) (item :type)])]))

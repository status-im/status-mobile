(ns quo2.components.list.sortable-list.mock-data 
  (:require [status-im2.common.resources :as resources]
            [quo2.components.icon :as quo2-icons]
            [quo2.components.drawers.action-drawers.style :as style]))

(def data 
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
                     :image (resources/get-mock-image :verified)}]}])
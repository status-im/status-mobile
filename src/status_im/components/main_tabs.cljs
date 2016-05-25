(ns status-im.components.main-tabs
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text-input
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.discovery.screen :refer [discovery]]
            [status-im.contacts.screen :refer [contact-list]]
            [status-im.components.tabs.tabs :refer [tabs]]
            [status-im.components.tabs.styles :as st]
            [status-im.components.styles :as common-st]
            [status-im.i18n :refer [label]]))

(def tab-list
  [{:view-id :chat-list
    :title   (label :t/chats)
    :screen  chats-list
    :icon    :icon_tab_chats}
   {:view-id :discovery
    :title   (label :t/discovery)
    :screen  discovery
    :icon    :icon_tab_discovery}
   {:view-id :contact-list
    :title   (label :t/contacts)
    :screen  contact-list
    :icon    :icon_tab_contacts}])

(defn show-view? [current-view view-id]
  (let [key-map {:key view-id}]
    (if (= current-view view-id)
      (merge st/show-tab key-map)
      (merge st/hide-tab key-map))))

(defn tab-view [current-view {:keys [view-id screen]}]
  [view (show-view? current-view view-id)
   [screen]])

(defview main-tabs []
  [view-id [:get :view-id]]
   [view common-st/flex
    (doall (map #(tab-view view-id %1) tab-list))
    [tabs {:selected-view-id view-id
           :tab-list         tab-list}]])

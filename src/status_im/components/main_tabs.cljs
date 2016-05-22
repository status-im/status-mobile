(ns status-im.components.main_tabs
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              touchable-highlight]]
            [status-im.components.tabs.tabs :refer [tabs]]
            [status-im.utils.logging :as log]))

(defview main-tabs []
  [view-id [:get :view-id]]
  [tabs {:selected-index (case view-id
                           :chat-list 0
                           :discovery 1
                           :contact-list 2
                           0)
         :tab-list       [{:handler #(dispatch [:navigate-to
                                                :chat-list])
                           :title   "Chats"
                           :icon    :icon_tab_chats}
                          {:handler #(dispatch [:navigate-to
                                                :discovery])
                           :title   "Discover"
                           :icon    :icon_tab_discovery}
                          {:handler #(dispatch [:navigate-to
                                                :contact-list])
                           :title   "Contacts"
                           :icon    :icon_tab_contacts}]}])

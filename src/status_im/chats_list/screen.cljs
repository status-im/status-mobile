(ns status-im.chats-list.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [list-view
                                                list-item
                                                view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.styles :refer [color-blue
                                                 toolbar-background1
                                                 toolbar-background2]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.i18n :refer [label]]
            [status-im.chats-list.styles :as st]
            [status-im.components.styles :as cst]
            [status-im.components.tabs.bottom-gradient :refer [bottom-gradient]]
            [status-im.components.tabs.styles :refer [tabs-height]]))

(defview chats-list-toolbar []
  [chats-scrolled? [:get :chats-scrolled?]]
  [view
   [status-bar]
   [toolbar {:nav-action       {:image   {:source {:uri :icon_hamburger}
                                          :style  st/hamburger-icon}
                                :handler open-drawer}
             :title            (label :t/chats)
             :background-color (if chats-scrolled?
                                 toolbar-background1
                                 toolbar-background2)
             ;; TODO implement search
             :action           {:image   {:source {:uri :icon_search}
                                          :style  st/search-icon}
                                :handler (fn [])}}]])

(defview chats-list []
  [chats [:get :chats]]
  ;; todo what is this?!
  #_(dispatch [:set :chats-scrolled? false])
  [view st/chats-container
   [chats-list-toolbar]
   [list-view {:dataSource (to-datasource chats)
               :renderRow  (fn [row _ _]
                             (list-item [chat-list-item row]))
               :style      st/list-container}]
   [view {:style         (st/action-buttons-container false 0)
          :pointerEvents :box-none}
    [action-button {:buttonColor color-blue
                    :offsetY     16
                    :offsetX     16}
     [action-button-item
      {:title       (label :t/new-chat)
       :buttonColor :#9b59b6
       :onPress     #(dispatch [:navigate-forget :group-contacts :people])}
      [ion-icon {:name  :md-create
                 :style st/create-icon}]]
     [action-button-item
      {:title       (label :t/new-group-chat)
       :buttonColor :#1abc9c
       :onPress     #(dispatch [:navigate-to :new-group])}
      [ion-icon {:name  :md-person
                 :style st/person-stalker-icon}]]]]
   [bottom-gradient]])

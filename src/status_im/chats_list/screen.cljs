(ns status-im.chats-list.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [list-view
                                                list-item
                                                view
                                                animated-view
                                                text
                                                icon
                                                image
                                                touchable-highlight]]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.styles :refer [color-blue]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.react :refer [linear-gradient]]
            [status-im.components.sync-state.offline :refer [offline-view]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.chats-list.styles :as st]
            [status-im.components.tabs.styles :refer [tabs-height]]))

(defview toolbar-view []
  [chats-scrolled? [:get :chats-scrolled?]]
  (let [new-chat? (get-in platform-specific [:chats :new-chat-in-toolbar?])
        actions   (if new-chat?
                    [(act/add #(dispatch [:navigate-to :group-contacts :people]))])]
    [toolbar-with-search
     {:show-search?       false
      :search-key         :chat-list
      :title              (label :t/chats)
      :search-placeholder (label :t/search-for)
      :nav-action         (act/hamburger open-drawer)
      :style              (st/toolbar chats-scrolled?)
      :actions            actions}]))

(defn chats-action-button []
  [action-button {:button-color color-blue
                  :offset-x     16
                  :offset-y     22
                  :hide-shadow  true
                  :spacing      13}
   [action-button-item
    {:title       (label :t/new-chat)
     :buttonColor :#9b59b6
     :onPress     #(dispatch [:navigate-to :group-contacts :people])}
    [ion-icon {:name  :md-create
               :style st/create-icon}]]
   [action-button-item
    {:title       (label :t/new-group-chat)
     :buttonColor :#1abc9c
     :onPress     #(dispatch [:open-contact-toggle-list :chat-group])}
    [icon :private_group_big st/group-icon]]
   [action-button-item
    {:title       (label :t/new-public-group-chat)
     :buttonColor :#1abc9c
     :onPress     #(dispatch [:navigate-to :new-public-group])}
    [icon :public_group_big st/group-icon]]])

(defn chat-shadow-item []
  [view {:height 3}
   [linear-gradient {:style  {:height 3}
                     :colors st/gradient-top-bottom-shadow}]])

(defview chats-list []
  [chats [:get :chats]]
  [view st/chats-container
   [toolbar-view]
   [list-view {:dataSource      (to-datasource chats)
               :renderRow       (fn [[id :as row] _ _]
                                  (list-item ^{:key id} [chat-list-item row]))
               :renderFooter    #(list-item [chat-shadow-item])
               :renderSeparator #(list-item
                                   (when (< %2 (- (count chats) 1))
                                     ^{:key (str "separator-" %2)}
                                     [view st/chat-separator-wrapper
                                      [view st/chat-separator-item]]))
               :style           st/list-container}]
   (when (get-in platform-specific [:chats :action-button?])
     [chats-action-button])
   [offline-view]])

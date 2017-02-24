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
            [status-im.components.toolbar.view :refer [toolbar toolbar-with-search]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.react :refer [linear-gradient]]
            [status-im.components.sync-state.offline :refer [offline-view]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific
                                              ios?]]
            [status-im.chats-list.styles :as st]
            [status-im.components.toolbar.styles :as tst]
            [status-im.components.tabs.styles :refer [tabs-height]]))

(def android-toolbar-popup-options
  [{:text (label :t/edit) :value #(dispatch [:set-in [:chat-list-ui-props :edit?] true])}])

(defn android-toolbar-actions []
  [view st/toolbar-actions
   [touchable-highlight
    {:on-press #(dispatch [:set-in [:toolbar-search :show] true])}
    [view st/toolbar-btn
     [icon :search_dark]]]
   [view st/toolbar-btn
    [context-menu
     [icon :options_dark]
     android-toolbar-popup-options]]])

(def ios-toolbar-popup-options
  [{:text (label :t/edit-chats) :value #(dispatch [:set-in [:chat-list-ui-props :edit?] true])}
   {:text (label :t/search-chats) :value #(dispatch [:set-in [:toolbar-search :show] true])}])

(defn ios-toolbar-actions []
  [view st/toolbar-actions
   [view st/toolbar-btn
    [context-menu
     [icon :options_dark]
     ios-toolbar-popup-options]]
   [touchable-highlight
    {:on-press #(dispatch [:navigate-to :group-contacts :people])}
    [view st/toolbar-btn
     [icon :add]]]])

(defn toolbar-view []
  [toolbar {:style         tst/toolbar-with-search
            :title          (label :t/chats)
            :nav-action     (act/hamburger open-drawer)
            :custom-action  (if ios?
                              (ios-toolbar-actions)
                              (android-toolbar-actions))}])

(defn toolbar-edit []
  [toolbar {:style          tst/toolbar-with-search
            :nav-action     (act/back #(dispatch [:set-in [:chat-list-ui-props :edit?] false]))
            :title          (label :t/edit-chats)}])

(defn toolbar-search []
  [toolbar-with-search
   {:show-search?       true
    :search-key         :chat-list
    :title              (label :t/chats)
    :search-placeholder (label :t/search-for)
    :style              (st/toolbar)}])

(defn chats-action-button []
  [action-button {:button-color color-blue
                  :offset-x     16
                  :offset-y     22
                  :hide-shadow  true
                  :spacing      13
                  :on-press     #(dispatch [:navigate-to :group-contacts :people])}])

(defn chat-shadow-item []
  [view {:height 3}
   [linear-gradient {:style  {:height 3}
                     :colors st/gradient-top-bottom-shadow}]])

(defview chats-list []
  [chats   [:filtered-chats]
   edit?   [:get-in [:chat-list-ui-props :edit?]]
   search? [:get-in [:toolbar-search :show]]]
  [view st/chats-container
   (cond
     edit?   [toolbar-edit]
     search? [toolbar-search]
     :else   [toolbar-view])
   [list-view {:dataSource      (to-datasource chats)
               :renderRow       (fn [[id :as row] _ _]
                                  (list-item ^{:key id} [chat-list-item row edit?]))
               :renderFooter    #(list-item [chat-shadow-item])
               :renderSeparator #(list-item
                                   (when (< %2 (- (count chats) 1))
                                     ^{:key (str "separator-" %2)}
                                     [view st/chat-separator-wrapper
                                      [view st/chat-separator-item]]))
               :style           st/list-container}]
   (when (and (not edit?)
              (not search?)
              (get-in platform-specific [:chats :action-button?]))
     [chats-action-button])
   [offline-view]])

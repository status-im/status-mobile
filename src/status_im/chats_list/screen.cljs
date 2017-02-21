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
            [status-im.components.toolbar-new.view :refer [toolbar toolbar-with-search]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :as tst]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.react :refer [linear-gradient]]
            [status-im.components.sync-state.offline :refer [offline-view]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.tabs.styles :refer [tabs-height]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [status-im.chats-list.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific ios?]]))

(def android-toolbar-popup-options
  [{:text (label :t/edit) :value #(dispatch [:set-in [:chat-list-ui-props :edit?] true])}])

(defn android-toolbar-actions []
  [(act/search #(dispatch [:set-in [:toolbar-search :show] true]))
   (act/opts android-toolbar-popup-options)])

(def ios-toolbar-popup-options
  [{:text (label :t/edit-chats) :value #(dispatch [:set-in [:chat-list-ui-props :edit?] true])}
   {:text (label :t/search-chats) :value #(dispatch [:set-in [:toolbar-search :show] true])}])

(defn ios-toolbar-actions []
  [(act/opts ios-toolbar-popup-options)
   (act/add #(dispatch [:navigate-to :new-chat]))])

(defn toolbar-view []
  [toolbar {:title      (label :t/chats)
            :nav-action (act/hamburger open-drawer)
            :actions    (if ios?
                          (ios-toolbar-actions)
                          (android-toolbar-actions))}])

(defn toolbar-edit []
  [toolbar {:nav-action (act/back #(dispatch [:set-in [:chat-list-ui-props :edit?] false]))
            :title      (label :t/edit-chats)}])

(defn toolbar-search []
  [toolbar-with-search
   {:show-search?       true
    :search-key         :chat-list
    :title              (label :t/chats)
    :search-placeholder (label :t/search-for)}])

(defn chats-action-button []
  [action-button {:button-color color-blue
                  :offset-x     16
                  :offset-y     22
                  :hide-shadow  true
                  :spacing      13
                  :on-press     #(dispatch [:navigate-to :new-chat])}])

(defn chat-list-padding []
  [view {:height (if ios? 0 8)
         :background-color :white}])

(defn chat-shadow-item []
  (when-not ios?
    [view {:height 12}
     [chat-list-padding]
     [linear-gradient {:style  {:height 4}
                       :colors st/gradient-top-bottom-shadow}]]))

(defn render-separator-fn [chats]
  (fn [_ row-id _]
    (list-item
     (when (< row-id (dec (count chats)))
       ^{:key (str "separator-" row-id)}
       [view st/chat-separator-wrapper
        [view st/chat-separator-item]]))))

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
               :renderHeader    #(when (seq chats) (list-item [chat-list-padding]))
               :renderFooter    #(when (seq chats) (list-item [chat-shadow-item]))
               :renderSeparator (when (get-in platform-specific [:chats :render-separator?])
                                  (render-separator-fn chats))
               :style           st/list-container}]
   (when (and (not edit?)
              (not search?)
              (get-in platform-specific [:chats :action-button?]))
     [chats-action-button])
   [offline-view]])

(ns status-im.ui.screens.chats-list.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.common.common :as common]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.react :refer [list-view
                                                list-item
                                                view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.native-action-button :refer [native-action-button]]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.styles :refer [color-blue]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar toolbar-with-search]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :as tst]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.sync-state.offline :refer [offline-view]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.tabs.styles :refer [tabs-height]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.ui.screens.chats-list.views.inner-item :refer [chat-list-item-inner-view]]
            [status-im.ui.screens.chats-list.styles :as st]
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
            :title      (label :t/edit-chats)
            :actions    [{:image :blank}]}])

(defview toolbar-search []
  (letsubs [search-text [:get-in [:toolbar-search :text]]]
    [toolbar-with-search
     {:show-search?       true
      :search-text        search-text
      :search-key         :chat-list
      :title              (label :t/chats)
      :search-placeholder (label :t/search-for)}]))

(defn chats-action-button []
  [native-action-button {:button-color color-blue
                         :offset-x     16
                         :offset-y     40
                         :spacing      13
                         :hide-shadow  true
                         :on-press     #(dispatch [:navigate-to :new-chat])}])

(defn chat-list-item [[chat-id chat] edit?]
  [touchable-highlight {:on-press #(dispatch [:navigate-to :chat chat-id])}
   [view
    [chat-list-item-inner-view (assoc chat :chat-id chat-id) edit?]]])

(defview chats-list []
  (letsubs [chats        [:filtered-chats]
            edit?        [:get-in [:chat-list-ui-props :edit?]]
            search?      [:get-in [:toolbar-search :show]]
            tabs-hidden? [:tabs-hidden?]]
    [view st/chats-container
     (cond
       edit?   [toolbar-edit]
       search? [toolbar-search]
       :else   [toolbar-view])
     [list-view {:dataSource      (to-datasource chats)
                 :renderRow       (fn [[id :as row] _ _]
                                    (list-item ^{:key id} [chat-list-item row edit?]))
                 :renderHeader    (when-not (empty? chats) renderers/list-header-renderer)
                 :renderFooter    (when-not (empty? chats)
                                    #(list-item [view
                                                 [common/list-footer]
                                                 [common/bottom-shadow]]))
                 :renderSeparator renderers/list-separator-renderer
                 :style           (st/list-container tabs-hidden?)}]
     (when (and (not edit?)
                (not search?)
                (get-in platform-specific [:chats :action-button?]))
       [chats-action-button])
     [offline-view]]))

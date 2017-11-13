(ns status-im.ui.screens.chats-list.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.renderers.renderers :as renderers]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.native-action-button :refer [native-action-button]]
            [status-im.ui.components.drawer.view :as drawer]
            [status-im.ui.components.styles :refer [color-blue]]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.styles :as tst]
            [status-im.ui.components.icons.custom-icons :refer [ion-icon]]
            [status-im.ui.components.sync-state.offline :refer [offline-view]]
            [status-im.ui.components.context-menu :refer [context-menu]]
            [status-im.ui.components.tabs.styles :refer [tabs-height]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.ui.screens.chats-list.views.inner-item :as inner-item]
            [status-im.ui.screens.chats-list.styles :as st]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [status-im.utils.notifications :as notifications]))

(def android-toolbar-popup-options
  [{:text (i18n/label :t/edit) :value #(re-frame/dispatch [:set-in [:chat-list-ui-props :edit?] true])}])

(defn android-toolbar-actions []
  [(act/search #(re-frame/dispatch [:set-in [:toolbar-search :show] true]))
   (act/opts android-toolbar-popup-options)])

(def ios-toolbar-popup-options
  [{:text (i18n/label :t/edit-chats) :value #(re-frame/dispatch [:set-in [:chat-list-ui-props :edit?] true])}
   {:text (i18n/label :t/search-chats) :value #(re-frame/dispatch [:set-in [:toolbar-search :show] true])}])

(defn ios-toolbar-actions []
  [(act/opts ios-toolbar-popup-options)
   (act/add #(re-frame/dispatch [:navigate-to :new-chat]))])

(defn toolbar-view []
  [toolbar/toolbar {:show-sync-bar? true}
   [toolbar/nav-button (act/hamburger drawer/open-drawer!)]
   [toolbar/content-title (i18n/label :t/chats)]
   [toolbar/actions
    (if ios?
      (ios-toolbar-actions)
      (android-toolbar-actions))]])

(defn toolbar-edit []
  [toolbar/toolbar {:show-sync-bar? true}
   [toolbar/nav-button (act/back #(re-frame/dispatch [:set-in [:chat-list-ui-props :edit?] false]))]
   [toolbar/content-title (i18n/label :t/edit-chats)]])

(defview toolbar-search []
  (letsubs [search-text [:get-in [:toolbar-search :text]]]
    [toolbar/toolbar-with-search
     {:show-search?       true
      :search-text        search-text
      :search-key         :chat-list
      :title              (i18n/label :t/chats)
      :search-placeholder (i18n/label :t/search-for)}]))

(defn chats-action-button []
  [native-action-button {:button-color        color-blue
                         :offset-x            16
                         :offset-y            40
                         :spacing             13
                         :hide-shadow         true
                         :accessibility-label :plus-button
                         :on-press            #(re-frame/dispatch [:navigate-to :new-chat])}])

(defn chat-list-item [[chat-id chat] edit?]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat chat-id])}
   [react/view
    [inner-item/chat-list-item-inner-view (assoc chat :chat-id chat-id) edit?]]])

(defview chats-list []
  (letsubs [chats        [:filtered-chats]
            edit?        [:get-in [:chat-list-ui-props :edit?]]
            search?      [:get-in [:toolbar-search :show]]
            tabs-hidden? [:tabs-hidden?]]
    [react/view st/chats-container
     (cond
       edit?   [toolbar-edit]
       search? [toolbar-search]
       :else   [toolbar-view])
     [react/list-view {:dataSource      (to-datasource chats)
                       :renderRow       (fn [[id :as row] _ _]
                                          (react/list-item ^{:key id} [chat-list-item row edit?]))
                       :renderHeader    (when-not (empty? chats) renderers/list-header-renderer)
                       :renderFooter    (when-not (empty? chats)
                                          #(react/list-item [react/view
                                                             [common/list-footer]
                                                             [common/bottom-shadow]]))
                       :renderSeparator renderers/list-separator-renderer
                       :style           st/list-container}]
     (when (and (not edit?)
                (not search?)
                (get-in platform-specific [:chats :action-button?]))
       [chats-action-button])
     [offline-view]]))

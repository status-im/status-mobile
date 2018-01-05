(ns status-im.ui.screens.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.native-action-button :refer [native-action-button]]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.sync-state.offline :refer [offline-view]]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

(defn toolbar-view []
  [toolbar/toolbar {:show-sync-bar? true}
   nil
   [toolbar/content-title (i18n/label :t/status)]
   [toolbar/actions
    (when platform/ios?
      [(toolbar.actions/add #(re-frame/dispatch [:navigate-to :new-chat]))])]])

(defn home-action-button []
  [native-action-button {:button-color        components.styles/color-blue
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

(views/defview home []
  (views/letsubs [chats        [:filtered-chats]
                  edit?        [:get-in [:chat-list-ui-props :edit?]]
                  show-search  [:get-in [:toolbar-search :show]]
                  tabs-hidden? [:tabs-hidden?]]
    [react/view styles/chats-container
     [toolbar-view]
     [list/flat-list {:style           styles/list-container
                      :data            chats
                      :render-fn       (fn [[chat-id :as chat]]
                                         ^{:key chat-id} [chat-list-item chat edit?])
                      :header          (when-not (empty? chats) list/default-header)
                      :footer          (when-not (empty? chats)
                                         [react/view
                                          [components.common/list-footer]
                                          [components.common/bottom-shadow]])}]

     (when platform/android?
       [home-action-button])
     [offline-view]]))

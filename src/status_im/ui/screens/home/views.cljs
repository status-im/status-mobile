(ns status-im.ui.screens.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.native-action-button :refer [native-action-button]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.sync-state.offline :refer [offline-view]]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.platform :as platform]))

(defn- toolbar []
  [toolbar/toolbar {:title-centered? true}
   nil
   [toolbar/content-title (i18n/label :t/status)]
   [toolbar/actions
    (when platform/ios?
      [(toolbar.actions/add #(re-frame/dispatch [:navigate-to :new]))])]])

(defn- home-action-button []
  [native-action-button {:button-color        colors/blue
                         :offset-x            styles/native-button-offset
                         :offset-y            styles/native-button-offset
                         :accessibility-label :plus-button
                         :on-press            #(re-frame/dispatch [:navigate-to :new])}])

(defn- home-list-item [[home-item-id home-item]]
  (if (:chat-id home-item)
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat home-item-id])}
     [react/view
      [inner-item/home-list-chat-item-inner-view home-item]]]
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:open-browser home-item])}
     [react/view
      [inner-item/home-list-browser-item-inner-view home-item]]]))

(views/defview home []
  (views/letsubs [home-items [:home-items]]
    [react/view styles/container
     [toolbar]
     [list/flat-list {:data            home-items
                      :render-fn       (fn [[home-item-id :as home-item]]
                                         ^{:key home-item-id} [home-list-item home-item])}]
     (when platform/android?
       [home-action-button])
     [offline-view]]))

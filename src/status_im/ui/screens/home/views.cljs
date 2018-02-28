(ns status-im.ui.screens.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.native-action-button :refer [native-action-button]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.sync-state.offline :refer [offline-view]]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.i18n :as i18n]))

(defn- toolbar [show-welcome?]
  [toolbar/toolbar nil nil
   (when-not show-welcome?
     [toolbar/content-wrapper
      [components.common/logo styles/toolbar-logo]])
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

;;do not remove view-id and will-update or will-unmount handlers, this is how it works
(views/defview welcome [view-id]
  (views/letsubs [handler #(re-frame/dispatch [:set-in [:accounts/create :show-welcome?] false])]
    {:component-will-update  handler
     :component-will-unmount handler}
    [react/view {:style styles/welcome-view}
     [react/view {:style styles/welcome-image-container}
      [react/image {:source (:welcome-image resources/ui)
                    :style  styles/welcome-image}]]
     [react/text {:style styles/welcome-text}
      (i18n/label :t/welcome-to-status)]
     [react/view
      [react/text {:style styles/welcome-text-description}
       (i18n/label :t/welcome-to-status-description)]]]))

(views/defview home []
  (views/letsubs [home-items [:home-items]
                  show-welcome? [:get-in [:accounts/create :show-welcome?]]
                  view-id [:get :view-id]]
    [react/view styles/container
     [toolbar show-welcome?]
     (cond show-welcome?
           [welcome view-id]
           (empty? home-items)
           [react/view styles/no-chats
            [react/text {:style styles/no-chats-text}
             (i18n/label :t/no-recent-chats)]]
           :else
           [list/flat-list {:data      home-items
                            :render-fn (fn [[home-item-id :as home-item]]
                                         ^{:key home-item-id} [home-list-item home-item])}])
     (when platform/android?
       [home-action-button])
     [offline-view]]))

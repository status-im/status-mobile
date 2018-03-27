(ns status-im.ui.screens.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.screens.home.animations.responder :as responder]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn- toolbar [show-welcome?]
  [toolbar/toolbar nil nil
   (when-not show-welcome?
     [toolbar/content-wrapper
      [components.common/logo styles/toolbar-logo]])
   [toolbar/actions
    (when platform/ios?
      [(toolbar.actions/add #(re-frame/dispatch [:navigate-to :new]))])]])

(defn- home-action-button []
  [react/view styles/action-button-container
   [react/touchable-highlight {:accessibility-label :plus-button
                               :on-press            #(re-frame/dispatch [:navigate-to :new])}
    [react/view styles/action-button
     [icons/icon :icons/add {:color :white}]]]])

(views/defview home-list-deletable [[home-item-id home-item]]
  (views/letsubs [swiped? [:delete-swipe-position home-item-id]]
    (let [delete-action (if (:chat-id home-item) :delete-chat :remove-browser)
          inner-item-view (if (:chat-id home-item)
                            inner-item/home-list-chat-item-inner-view
                            inner-item/home-list-browser-item-inner-view)
          offset-x (animation/create-value (if swiped? styles/delete-button-width 0))
          swipe-pan-responder (responder/swipe-pan-responder offset-x styles/delete-button-width home-item-id swiped?)
          swipe-pan-handler (responder/pan-handlers swipe-pan-responder)]
      [react/view swipe-pan-handler
       [react/animated-view {:style {:flex 1 :right offset-x}}
        [inner-item-view home-item]
        [react/touchable-highlight {:style    styles/delete-icon-highlight
                                    :on-press #(do
                                                 (re-frame/dispatch [:set-swipe-position home-item-id false])
                                                 (re-frame/dispatch [delete-action home-item-id]))}
         [react/view {:style styles/delete-icon-container}
          [vector-icons/icon :icons/delete {:color colors/red}]]]]])))

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
                            :key-fn    first
                            :render-fn (fn [home-item]
                                         [home-list-deletable home-item])}])
     (when platform/android?
       [home-action-button])
     [connectivity/error-view]]))

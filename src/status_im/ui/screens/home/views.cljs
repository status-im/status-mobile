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
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.screens.home.animations.responder :as responder]
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

(defn- home-list-deletable [[home-item-id home-item]]
  (views/letsubs [swiped?      [:delete-swipe-position home-item-id]]
    (let [delete-action       (if (:chat-id home-item) :remove-chat :remove-browser)
          inner-view          (if (:chat-id home-item)
                                inner-item/home-list-chat-item-inner-view
                                inner-item/home-list-browser-item-inner-view)
          offset-x            (animation/create-value (if swiped? styles/delete-button-width 0))
          swipe-pan-responder (responder/swipe-pan-responder offset-x styles/delete-button-width home-item-id swiped?)
          swipe-pan-handler   (responder/pan-handlers swipe-pan-responder)]
      [react/view swipe-pan-handler
       [react/animated-view {:style {:right offset-x}}
        [inner-view home-item]
        [react/touchable-highlight {:style styles/delete-icon-highlight
                                    :on-press #(do
                                                 (re-frame/dispatch [:set-swipe-position home-item-id false])
                                                 (re-frame/dispatch [delete-action home-item-id]))}
         [react/view {:style styles/delete-icon-container}
          [vector-icons/icon :icons/delete {:color colors/red}]]]]])))

(views/defview home []
  (views/letsubs [home-items [:home-items]]
    [react/view styles/container
     [toolbar]
     [list/flat-list {:data            home-items
                      :render-fn       (fn [[home-item-id :as home-item]]
                                         ^{:key home-item-id} [home-list-deletable home-item])}]
     (when platform/android?
       [home-action-button])
     [offline-view]]))

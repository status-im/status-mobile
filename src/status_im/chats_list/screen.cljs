(ns status-im.chats-list.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [list-view
                                                list-item
                                                view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.utils.listview :refer [to-datasource]]
            [reagent.core :as r]
            [status-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.styles :refer [color-blue
                                                 toolbar-background1
                                                 toolbar-background2]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.i18n :refer [label]]
            [status-im.chats-list.styles :as st]
            [status-im.components.tabs.styles :refer [tabs-height]]))

(defview chats-list-toolbar []
  [chats-scrolled? [:get :chats-scrolled?]]
  [toolbar {:nav-action {:image   {:source {:uri :icon_hamburger}
                                   :style  st/hamburger-icon}
                         :handler open-drawer}
            :title      (label :t/chats)
            :background-color (if chats-scrolled?
                                toolbar-background1
                                toolbar-background2)
            ;; TODO implement search
            :action     {:image   {:source {:uri :icon_search}
                                   :style  st/search-icon}
                         :handler (fn [])}}])

(defn chats-list []
  (let [chats (subscribe [:get :chats])
        chats-scrolled? (subscribe [:get :chats-scrolled?])
        animation? (subscribe [:animations :tabs-bar-animation?])
        tabs-bar-value (subscribe [:animations :tabs-bar-value])
        container-height (r/atom 0)
        content-height (r/atom 0)]
    (dispatch [:set :chats-scrolled? false])
    (fn []
      [drawer-view
       [view st/chats-container
        [chats-list-toolbar]
        [list-view {:dataSource          (to-datasource @chats)
                    :renderRow           (fn [row _ _]
                                           (list-item [chat-list-item row]))
                    :style               st/list-container
                    ;;; if "maximazing" chat list will make scroll to 0,
                    ;;; then disable maximazing
                    :onLayout            (fn [event]
                                           (when-not @chats-scrolled?
                                             (let [height (.. event -nativeEvent -layout -height)]
                                               (reset! container-height height))))
                    :onContentSizeChange (fn [width height]
                                           (reset! content-height height))
                    :onScroll            (fn [e]
                                           (let [offset (.. e -nativeEvent -contentOffset -y)
                                                 min-content-height (+ @container-height tabs-height)
                                                 scrolled? (and (< 0 offset) (< min-content-height @content-height))]
                                             (dispatch [:set :chats-scrolled? scrolled?])
                                             (dispatch [:set-animation :tabs-bar-animation? true])))}]
        [animated-view {:style         (st/action-buttons-container @animation? (or @tabs-bar-value 0))
                        :pointerEvents :box-none}
         [action-button {:buttonColor color-blue
                         :offsetY     16
                         :offsetX     16}
          [action-button-item
           {:title       (label :t/new-chat)
            :buttonColor :#9b59b6
            :onPress     #(dispatch [:show-group-contacts :people])}
           [icon {:name  :android-create
                  :style st/create-icon}]]
          [action-button-item
           {:title       (label :t/new-group-chat)
            :buttonColor :#1abc9c
            :onPress     #(dispatch [:show-group-new])}
           [icon {:name  :person-stalker
                  :style st/person-stalker-icon}]]]]]])))

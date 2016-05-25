(ns status-im.chats-list.screen
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [list-view
                                                list-item
                                                view
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
                                                 toolbar-background2]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.i18n :refer [t]]
            [status-im.chats-list.styles :as st]))

(defn chats-list-toolbar []
  [toolbar {:nav-action {:image   {:source {:uri :icon_hamburger}
                                   :style  st/hamburger-icon}
                         :handler open-drawer}
            :title      (t :chats.title)
            :background-color toolbar-background2
            ;; TODO implement search
            :action     {:image   {:source {:uri :icon_search}
                                   :style  st/search-icon}
                         :handler (fn [])}}])

(defn chats-list []
  (let [chats (subscribe [:get :chats])]
    (fn []
      [drawer-view
       [view st/chats-container
        [chats-list-toolbar]
        [list-view {:dataSource (to-datasource (vals @chats))
                    :renderRow  (fn [row _ _]
                                  (list-item [chat-list-item row]))
                    :style      st/list-container}]
        [action-button {:buttonColor color-blue
                        :offsetY     16
                        :offsetX     16}
         [action-button-item
          {:title       (t :chats.new-chat)
           :buttonColor :#9b59b6
           :onPress     #(dispatch [:navigate-to :contact-list])}
          [icon {:name  :android-create
                 :style st/create-icon}]]
         [action-button-item
          {:title       (t :chats.new-group-chat)
           :buttonColor :#1abc9c
           :onPress     #(dispatch [:show-group-new])}
          [icon {:name  :person-stalker
                 :style st/person-stalker-icon}]]]]])))

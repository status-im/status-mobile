(ns syng-im.chats-list.screen
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [reagent.core :as r]
            [syng-im.chats-list.views.chat-list-item :refer [chat-list-item]]
            [syng-im.components.action-button :refer [action-button
                                                      action-button-item]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-black
                                               color-blue
                                               text1-color
                                               text2-color]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.icons.ionicons :refer [icon]]
            [syng-im.chats-list.styles :as st]))


(defn chats-list-toolbar []
  [toolbar {:nav-action {:image   {:source {:uri :icon_hamburger}
                                   :style st/hamburger-icon }
                         :handler (fn [])}
            :title      "Chats"
            :action     {:image   {:source {:uri :icon_search}
                                   :style  st/search-icon}
                         :handler (fn [])}}])

(defn chats-list []
  (let [chats (subscribe [:get-chats])]
    (fn []
      (let [chats      @chats
            datasource (to-realm-datasource chats)]
        [view st/chats-container
         [chats-list-toolbar]
         [list-view {:dataSource datasource
                     :renderRow  (fn [row _ _]
                                   (r/as-element [chat-list-item row]))
                     :style     st/list-container}]
         [action-button {:buttonColor color-blue}
          [action-button-item
           {:title       "New Chat"
            :buttonColor :#9b59b6
            :onPress     #(dispatch [:navigate-to :contact-list])}
           [icon {:name  :android-create
                  :style st/create-icon}]]
          [action-button-item
           {:title       "New Group Chat"
            :buttonColor :#1abc9c
            :onPress     #(dispatch [:show-group-new])}
           [icon {:name  :person-stalker
                  :style st/person-stalker-icon}]]]]))))

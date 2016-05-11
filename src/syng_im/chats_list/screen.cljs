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
            [syng-im.components.icons.ionicons :refer [icon]]))

(defn chats-list-toolbar []
  [toolbar {:nav-action {:image   {:source {:uri "icon_hamburger"}
                                   :style  {:width  16
                                            :height 12}}
                         :handler (fn [])}
            :title      "Chats"
            :action     {:image   {:source {:uri "icon_search"}
                                   :style  {:width  17
                                            :height 17}}
                         :handler (fn [])}}])

(defn chats-list []
  (let [chats (subscribe [:get-chats])]
    (fn []
      (let [chats      @chats
            datasource (to-realm-datasource chats)]
        [view {:flex            1
               :backgroundColor :white}
         [chats-list-toolbar]
         [list-view {:dataSource datasource
                     :renderRow  (fn [row _ _]
                                   (r/as-element [chat-list-item row]))
                     :style      {:backgroundColor :white}}]
         [action-button {:buttonColor color-blue}
          [action-button-item
           {:title       "New Chat"
            :buttonColor :#9b59b6
            :onPress     #(dispatch [:navigate-to :contact-list])}
           [icon {:name  :android-create
                  :style {:fontSize 20
                          :height   22
                          :color    :white}}]]
          [action-button-item
           {:title       "New Group Chat"
            :buttonColor :#1abc9c
            :onPress     #(dispatch [:show-group-new])}
           [icon {:name  :person-stalker
                  :style {:fontSize 20
                          :height   22
                          :color    :white}}]]]]))))

(ns syng-im.components.chats.chats-list
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [reagent.core :as r]
            [syng-im.components.chats.chat-list-item :refer [chat-list-item]]
            [syng-im.components.action-button :refer [action-button
                                                      action-button-item]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-black
                                               color-blue
                                               text1-color
                                               text2-color]]
            [syng-im.components.icons.ionicons :refer [icon]]))


(defn chats-list [{:keys [navigator]}]
  (let [chats (subscribe [:get-chats])]
    (fn []
      (let [chats      @chats
            _          (log/debug "chats=" chats)
            datasource (to-realm-datasource chats)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:navIcon       res/icon-hamburger
                             :style         {:backgroundColor "white"
                                             :height          56
                                             :elevation       2}
                             :onIconClicked (fn []
                                              (nav-pop navigator))
                             :actions [{:title "Search"
                                        :icon res/icon-search
                                        :show "always"}]}
            [view {:style {:flex 1
                           :alignItems "center"
                           :justifyContent "center"
                           :marginRight 112
                           :backgroundColor "transparent"}}
             [text {:style {:marginTop  -2.5
                            :color      text1-color
                            :fontSize   16
                            :fontFamily font}}
              "Chats"]]])
         [list-view {:dataSource datasource
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [chat-list-item row navigator]))
                     :style      {:backgroundColor "white"}}]
         [action-button {:buttonColor color-blue}
          [action-button-item {:title       "New Chat"
                               :buttonColor "#9b59b6"
                               :onPress     (fn []
                                              (dispatch [:show-contacts navigator]))}
           [icon {:name  "android-create"
                  :style {:fontSize 20
                          :height   22
                          :color    "white"}}]]
          [action-button-item {:title       "New Group Chat"
                               :buttonColor "#1abc9c"
                               :onPress     (fn []
                                              (dispatch [:show-group-new navigator]))}
           [icon {:name  "person-stalker"
                  :style {:fontSize 20
                          :height   22
                          :color    "white"}}]]]]))))

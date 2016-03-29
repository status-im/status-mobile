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
            [syng-im.components.chats.chat-list-item :refer [chat-list-item]]))


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
           [toolbar-android {:logo          res/logo-icon
                             :title         "Your Chats"
                             :titleColor    "#4A5258"
                             :subtitle      "List of your recent chats"
                             :subtitleColor "#AAB2B2"
                             :navIcon       res/nav-back-icon
                             :style         {:backgroundColor "white"
                                             :height          56
                                             :elevation       2}
                             :onIconClicked (fn []
                                              (nav-pop navigator))}])
         [list-view {:dataSource datasource
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [chat-list-item row navigator]))
                     :style      {:backgroundColor "white"}}]]))))

(ns syng-im.components.chat
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
            [syng-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [reagent.core :as r]
            [syng-im.components.chat.chat-message :refer [chat-message]]
            [syng-im.components.chat.chat-message-new :refer [chat-message-new]]))


(defn chat [{:keys [navigator]}]
  (let [messages (subscribe [:get-chat-messages])
        chat     (subscribe [:get-current-chat])]
    (fn []
      (let [msgs       @messages
            _          (log/debug "messages=" msgs)
            datasource (to-realm-datasource msgs)]
        [view {:style {:flex            1
                       :backgroundColor "#eef2f5"}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:logo          res/logo-icon
                             :title         (or (@chat :name)
                                                "Chat name")
                             :titleColor    "#4A5258"
                             :subtitle      "Last seen just now"
                             :subtitleColor "#AAB2B2"
                             :navIcon       res/nav-back-icon
                             :style         {:backgroundColor "white"
                                             :height          56
                                             :elevation       2}
                             :onIconClicked (fn []
                                              (nav-pop navigator))}])
         [list-view {:dataSource            datasource
                     :renderScrollComponent (fn [props]
                                              (invertible-scroll-view nil))
                     :renderRow             (fn [row section-id row-id]
                                              (r/as-element [chat-message (js->clj row :keywordize-keys true)]))
                     :style                 {:backgroundColor "white"}}]
         [chat-message-new]]))))

(ns syng-im.new-group.screen
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              touchable-highlight]]
            [syng-im.components.styles :refer [color-purple]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.new-group.views.contact :refer [new-group-contact]]
            [reagent.core :as r]
            [syng-im.new-group.styles :as st]))


(defn new-group-toolbar [group-name]
  [toolbar {:title  "New group chat"
            :action {:image   {:source res/v                ;; {:uri "icon_search"}
                               :style  st/toolbar-icon}
                     :handler #(dispatch [:create-new-group group-name])}}])

(defn new-group []
  (let [contacts   (subscribe [:all-contacts])
        group-name (subscribe [:get ::group-name])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view st/new-group-container
         [new-group-toolbar @group-name]
         [view st/chat-name-container
          [text {:style st/chat-name-text} "Chat name"]
          [text-input
           {:underlineColorAndroid color-purple
            :style                 st/group-name-input
            :autoFocus             true
            :placeholder           "Group Name"
            :onChangeText          #(dispatch [:set ::group-name %])
            :onSubmitEditing       #(dispatch [:set ::group-name nil])}
           @group-name]
          [text {:style st/members-text} "Members"]
          [touchable-highlight {:on-press (fn [])}
           [view st/add-container
            [icon :add_gray st/add-icon]
            [text {:style st/add-text} "Add members"]]]
          [list-view
           {:dataSource contacts-ds
            :renderRow  (fn [row _ _]
                          (let [row' (js->clj row :keywordize-keys true)]
                            (r/as-element [new-group-contact row'])))
            :style      st/contacts-list}]]]))))

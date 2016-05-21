(ns status-im.new-group.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [status-im.components.styles :refer [color-purple]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]))


(defview new-group-toolbar []
  [group-name [:get ::group-name]
   creation-disabled? [:get :disable-group-creation]]
  [toolbar
   {:title  "New group chat"
    :action {:image   {:source res/v                        ;; {:uri "icon_search"}
                       :style  st/toolbar-icon}
             :handler (when-not creation-disabled?
                        #(dispatch [:init-group-creation group-name]))}}])

(defview group-name-input []
  [group-name [:get ::group-name]]
  [text-input
   {:underlineColorAndroid color-purple
    :style                 st/group-name-input
    :autoFocus             true
    :placeholder           "Group Name"
    :onChangeText          #(dispatch [:set ::group-name %])}
   group-name])

(defview new-group []
  [contacts [:all-contacts]]
  [view st/new-group-container
   [new-group-toolbar]
   [view st/chat-name-container
    [text {:style st/chat-name-text} "Chat name"]
    [group-name-input]
    [text {:style st/members-text} "Members"]
    [touchable-highlight {:on-press (fn [])}
     [view st/add-container
      [icon :add_gray st/add-icon]
      [text {:style st/add-text} "Add members"]]]
    [list-view
     {:dataSource (to-datasource contacts)
      :renderRow  (fn [row _ _]
                    (list-item [new-group-contact row]))
      :style      st/contacts-list}]]])

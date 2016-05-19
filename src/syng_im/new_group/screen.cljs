(ns syng-im.new-group.screen
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.components.styles :refer [color-purple]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.new-group.views.contact :refer [new-group-contact]]
            [syng-im.new-group.styles :as st]))


(defview new-group-toolbar []
  [group-name [:get ::group-name]]
  [toolbar
   {:title  "New group chat"
    :action {:image   {:source res/v                        ;; {:uri "icon_search"}
                       :style  st/toolbar-icon}
             :handler #(dispatch [:create-new-group group-name])}}])

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

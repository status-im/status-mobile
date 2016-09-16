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
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.components.styles :as cst]))


(defview new-group-toolbar []
  [group-name [:get :new-chat-name]
   creation-disabled? [:get :disable-group-creation]
   valid? [:new-chat-name-valid?]]
  (let [create-btn-enabled? (and valid? (not creation-disabled?))]
    [view
     [status-bar]
     [toolbar
      {:title  (label :t/new-group-chat)
       :action {:image   {:source res/v                     ;; {:uri "icon_search"}
                          :style  (st/toolbar-icon create-btn-enabled?)}
                :handler (when create-btn-enabled?
                           #(dispatch [:init-group-creation group-name]))}}]]))

(defview group-name-input []
  [group-name [:get :new-chat-name]
   validation-messages [:new-chat-name-validation-messages]]
  [view
   [text-input
    {:underlineColorAndroid color-purple
     :style                 st/group-name-input
     :autoFocus             true
     :placeholder           (label :t/group-name)
     :onChangeText          #(dispatch [:set :new-chat-name %])}
    group-name]
   (when (pos? (count validation-messages))
     [text {:style st/group-name-validation-message} (first validation-messages)])])

(defview new-group []
  [contacts [:all-added-contacts]]
  [view st/new-group-container
   [new-group-toolbar]
   [view st/chat-name-container
    [text {:style st/chat-name-text} (label :t/chat-name)]
    [group-name-input]
    [text {:style st/members-text} (label :t/members-title)]
    [touchable-highlight {:on-press (fn [])}
     [view st/add-container
      [icon :add_gray st/add-icon]
      [text {:style st/add-text} (label :t/add-members)]]]
    [list-view
     {:dataSource (to-datasource contacts)
      :renderRow  (fn [row _ _]
                    (list-item [new-group-contact row]))
      :style      st/contacts-list}]]])

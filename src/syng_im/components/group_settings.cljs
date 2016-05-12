(ns syng-im.components.group-settings
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              touchable-highlight]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [color-purple
                                               text2-color]]
            [syng-im.components.group-settings-styles :as st]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.contact-list.contact-inner :refer [contact-inner-view]]
            [syng-im.components.chats.new-group-contact :refer [new-group-contact]]
            [reagent.core :as r]))

(defn set-group-settings-name [chat-name]
  (dispatch [:set-group-settings-name chat-name]))

(defn save-group-chat []
  (dispatch [:save-group-chat]))

(defn chat-members [members]
  [view st/chat-members-container
   (for [member members]
     ^{:key member} [contact-inner-view member]
     ;; [new-group-contact member nil]
     )])

(defn action-save []
  [touchable-highlight
   {:on-press save-group-chat}
   [view st/save-btn
    [text {:style st/save-btn-text} "S"]]])

(defn new-group-toolbar []
  [toolbar {:title  "Chat settings"
            :custom-action [action-save]}])

(defn group-settings []
  (let [chat-name (subscribe [:group-settings-name])
        members   (subscribe [:group-settings-members])]
    (fn []
      [view st/group-settings
       [new-group-toolbar]
       [view st/properties-container
        [text {:style st/chat-name-text}
         "Chat name"]
        [text-input {:style                 st/chat-name-input
                     :underlineColorAndroid color-purple
                     :autoFocus             true
                     :placeholderTextColor  text2-color
                     :onChangeText          set-group-settings-name}
         @chat-name]
        [text {:style st/members-text}
         "Members"]
        [touchable-highlight {:on-press (fn []
                                          ;; TODO not implemented
                                          )}
         [view st/add-members-container
          [icon :add-gray st/add-members-icon]
          [text {:style st/add-members-text}
           "Add members"]]]
        [chat-members (vals (js->clj @members :keywordize-keys true))]
        [text {:style st/settings-text}
         "Settings"]]])))

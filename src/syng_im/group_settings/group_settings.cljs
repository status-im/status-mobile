(ns syng-im.group-settings.group-settings
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              modal
                                              touchable-highlight]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [color-purple
                                               text2-color]]
            [syng-im.group-settings.styles.group-settings :as st]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.group-settings.views.member :refer [member-view]]
            [reagent.core :as r]))

(defn remove-member [{:keys [whisper-identity]}]
  (dispatch [:chat-remove-member whisper-identity]))

(defn close-member-menu []
  (dispatch [:select-group-chat-member nil]))

(defn member-menu [member]
  [modal {:animated       false
          :transparent    false
          :onRequestClose close-member-menu}
   [touchable-highlight {:style    st/modal-container
                         :on-press close-member-menu}
    [view st/modal-inner-container
     [text {:style st/modal-member-name}
      (:name member)]
     [touchable-highlight {:on-press #(remove-member member)}
      [text {:style st/modal-remove-text}
       "Remove"]]]]])

(defn chat-members [members]
  [view st/chat-members-container
   (for [member members]
     ^{:key member} [member-view member])])

(defn show-chat-name-edit []
  (dispatch [:navigate-to :chat-name-edit]))

(defn chat-icon []
  (let [chat-name (subscribe [:get-current-chat-name])]
    (fn []
      [view st/chat-icon
       [text {:style st/chat-icon-text} (nth @chat-name 0)]])))

(defn new-group-toolbar []
  [toolbar {:title         "Chat settings"
            :custom-action [chat-icon]}])

(defn group-settings []
  (let [chat-name       (subscribe [:get-current-chat-name])
        members         (subscribe [:current-chat-contacts])
        selected-member (subscribe [:selected-group-chat-member])]
    (fn []
      [view st/group-settings
       [new-group-toolbar]
       [text {:style st/chat-name-text}
        "Chat name"]
       [view st/chat-name-value-container
        [text {:style st/chat-name-value}
         @chat-name]
        [touchable-highlight {:style st/chat-name-btn-edit-container
                              :on-press show-chat-name-edit}
         [text {:style st/chat-name-btn-edit-text}
          "Edit"]]]
       [text {:style st/members-text}
        "Members"]
       [touchable-highlight {:on-press (fn []
                                         (dispatch [:show-add-participants]))}
        [view st/add-members-container
         [icon :add-gray st/add-members-icon]
         [text {:style st/add-members-text}
          "Add members"]]]
       [chat-members (vals (js->clj @members :keywordize-keys true))]
       [text {:style st/settings-text}
        "Settings"]
       (when @selected-member
         [member-menu @selected-member])])))

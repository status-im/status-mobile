(ns syng-im.group-settings.group-settings
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              modal
                                              picker
                                              picker-item
                                              scroll-view
                                              touchable-highlight]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [color-purple
                                               text2-color]]
            [syng-im.group-settings.styles.group-settings :as st]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.group-settings.views.member :refer [member-view]]
            [syng-im.utils.logging :as log]
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

(defn setting-view [{:keys     [icon-style custom-icon handler title subtitle]
                     icon-name :icon}]
  [touchable-highlight {:on-press handler}
   [view st/setting-row
    [view st/setting-icon-view
     (or custom-icon
         [icon icon-name icon-style])]
    [view st/setting-view
     [text {:style st/setting-title} title]
     (when-let [subtitle subtitle]
       [text {:style st/setting-subtitle}
        subtitle])]]])

(defn close-chat-color-picker []
  (dispatch [:show-group-settings-color-picker false]))

(defn set-chat-color [color]
  (close-chat-color-picker)
  (dispatch [:set-chat-color color]))

(defn chat-color-picker []
  (let [show-color-picker (subscribe [:group-settings-show-color-picker])
        chat-color        (subscribe [:get-current-chat-color])
        selected-color    (r/atom @chat-color)]
    (fn []
      [modal {:animated       false
              :transparent    false
              :onRequestClose close-chat-color-picker}
       [touchable-highlight {:style    st/modal-container
                             :on-press close-chat-color-picker}
        [view st/modal-color-picker-inner-container
         [picker {:selectedValue @selected-color
                  :onValueChange #(reset! selected-color %)}
          [picker-item {:label "Blue"       :value "#7099e6"}]
          [picker-item {:label "Purple"     :value "#a187d5"}]
          [picker-item {:label "Green"      :value "green"}]
          [picker-item {:label "Red"        :value "red"}]]
         [touchable-highlight {:on-press #(set-chat-color @selected-color)}
          [text {:style st/modal-color-picker-save-btn-text}
           "Save"]]]]])))

(defn chat-color-icon []
  (let [chat-color (subscribe [:get-current-chat-color])]
    (fn []
      [view {:style (st/chat-color-icon @chat-color)}])))

(defn settings-view []
  ;; TODO implement settings handlers
  (let [settings [{:custom-icon [chat-color-icon]
                   :title       "Change color"
                   :handler     #(dispatch [:show-group-settings-color-picker true])}
                  (merge {:title    "Notifications and sounds"
                          :subtitle "!not implemented"
                          :handler  nil}
                         (if true
                           {:icon       :notifications-on
                            :icon-style {:width  16
                                         :height 21}}
                           {:icon       :muted
                            :icon-style {:width  18
                                         :height 21}}))
                  {:icon        :close-gray
                   :icon-style  {:width  12
                                 :height 12}
                   :title       "Clear history"
                   :subtitle    "!not implemented"
                   :handler     nil}
                  {:icon        :bin
                   :icon-style  {:width  12
                                 :height 18}
                   :title       "Delete and leave"
                   :subtitle    "!not implemented"
                   :handler     nil}]]
    [view st/settings-container
     (for [setting settings]
       ^{:key setting} [setting-view setting])]))

(defn chat-icon []
  (let [chat-name  (subscribe [:get-current-chat-name])
        chat-color (subscribe [:get-current-chat-color])]
    (fn []
      [view (st/chat-icon @chat-color)
       [text {:style st/chat-icon-text} (nth @chat-name 0)]])))

(defn new-group-toolbar []
  [toolbar {:title         "Chat settings"
            :custom-action [chat-icon]}])

(defn group-settings []
  (let [chat-name         (subscribe [:get-current-chat-name])
        members           (subscribe [:current-chat-contacts])
        selected-member   (subscribe [:selected-group-chat-member])
        show-color-picker (subscribe [:group-settings-show-color-picker])]
    (fn []
      [view st/group-settings
       [new-group-toolbar]
       [scroll-view st/body
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
        [settings-view]]
       (when @show-color-picker
         [chat-color-picker])
       (when @selected-member
         [member-menu @selected-member])])))

(ns status-im.group-settings.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              icon
                                              modal
                                              picker
                                              picker-item
                                              scroll-view
                                              touchable-highlight]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.group-settings.styles.group-settings :as st]
            [status-im.group-settings.views.member :refer [member-view]]))

(defn remove-member []
  (dispatch [:remove-participants]))

(defn close-member-menu []
  (dispatch [:set :selected-participants #{}]))

(defview member-menu []
  [{:keys [name] :as participant} [:selected-participant]]
  (when participant
    [modal {:animated       false
            :transparent    false
            :onRequestClose close-member-menu}
     [touchable-highlight {:style    st/modal-container
                           :on-press close-member-menu}
      [view st/modal-inner-container
       [text {:style st/modal-member-name} name]
       [touchable-highlight {:on-press remove-member}
        [text {:style st/modal-remove-text}
         "Remove"]]]]]))

(defview chat-members []
  [members [:current-chat-contacts]]
  [view st/chat-members-container
   (for [member members]
     ^{:key member} [member-view member])])

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
  (dispatch [:group-settings :show-color-picker false]))

(defn set-chat-color []
  (close-chat-color-picker)
  (dispatch [:set-chat-color]))

(defview chat-color-picker []
  [show-color-picker [:group-settings :show-color-picker]
   new-color [:get :new-chat-color]]
  [modal {:animated       false
          :transparent    false
          :onRequestClose close-chat-color-picker}
   [touchable-highlight {:style    st/modal-container
                         :on-press close-chat-color-picker}
    [view st/modal-color-picker-inner-container
     [picker {:selectedValue new-color
              :onValueChange #(dispatch [:set :new-chat-color %])}
      [picker-item {:label "Blue" :value "#7099e6"}]
      [picker-item {:label "Purple" :value "#a187d5"}]
      [picker-item {:label "Green" :value "green"}]
      [picker-item {:label "Red" :value "red"}]]
     [touchable-highlight {:on-press set-chat-color}
      [text {:style st/modal-color-picker-save-btn-text}
       "Save"]]]]])

(defview chat-color-icon []
  [chat-color [:chat :color]]
  [view {:style (st/chat-color-icon chat-color)}])

(defn show-chat-color-picker []
  (dispatch [:group-settings :show-color-picker true]))

(defn settings-view []
  ;; TODO implement settings handlers
  (let [settings [{:custom-icon [chat-color-icon]
                   :title       "Change color"
                   :handler     show-chat-color-picker}
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
                  {:icon       :close-gray
                   :icon-style {:width  12
                                :height 12}
                   :title      "Clear history"
                   :handler    #(dispatch [:clear-history])}
                  {:icon       :bin
                   :icon-style {:width  12
                                :height 18}
                   :title      "Delete and leave"
                   :handler    #(dispatch [:leave-group-chat])}]]
    [view st/settings-container
     (for [setting settings]
       ^{:key setting} [setting-view setting])]))

(defview chat-icon []
  [name [:chat :name]
   color [:chat :color]]
  [view (st/chat-icon color)
   [text {:style st/chat-icon-text} (first name)]])

(defn new-group-toolbar []
  [toolbar {:title         "Chat settings"
            :custom-action [chat-icon]}])

(defn focus []
  (dispatch [:set ::name-input-focused true]))

(defn blur []
  (dispatch [:set ::name-input-focused false]))

(defn save []
  (dispatch [:set-chat-name]))

(defview chat-name []
  [name [:chat :name]
   new-name [:get :new-chat-name]
   focused? [:get ::name-input-focused]]
  [view
   [text {:style st/chat-name-text} "Chat name"]
   [view (st/chat-name-value-container focused?)
    [text-input {:style          st/chat-name-value
                 :ref            #(when (and % focused?) (.focus %))
                 :on-change-text #(dispatch [:set :new-chat-name %])
                 :on-focus       focus
                 :on-blur        blur}
     name]
    (if (or focused? (not= name new-name))
      [touchable-highlight {:style    st/chat-name-btn-edit-container
                            :on-press save}
       [view [icon :ok-purple st/add-members-icon]]]
      [touchable-highlight {:style    st/chat-name-btn-edit-container
                            :on-press focus}
       [text {:style st/chat-name-btn-edit-text} "Edit"]])]])

(defview group-settings []
  [show-color-picker [:group-settings :show-color-picker]]
  [view st/group-settings
   [new-group-toolbar]
   [scroll-view st/body
    [chat-name]
    [text {:style st/members-text} "Members"]
    [touchable-highlight {:on-press #(dispatch [:navigate-to :add-participants])}
     [view st/add-members-container
      [icon :add-gray st/add-members-icon]
      [text {:style st/add-members-text}
       "Add members"]]]
    [chat-members]
    [text {:style st/settings-text}
     "Settings"]
    [settings-view]]
   (when show-color-picker
     [chat-color-picker])
   [member-menu]])

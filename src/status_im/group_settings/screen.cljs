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
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-action]]
            [status-im.group-settings.styles.group-settings :as st]
            [status-im.group-settings.views.member :refer [member-view]]
            [status-im.i18n :refer [label]]
            [status-im.group-settings.views.color-settings :refer [color-settings]]))

(defn remove-member []
  (dispatch [:remove-participants]))

(defn close-member-menu []
  (dispatch [:set :selected-participants #{}]))

;; TODO not in design
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
         (label :t/remove)]]]]]))

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

(defview chat-color-icon []
  [chat-color [:chat :color]]
  [view {:style (st/chat-color-icon chat-color)}])

(defn show-chat-color-picker []
  (dispatch [:group-settings :show-color-picker true]))

(defn settings-view []
  (let [settings [{:custom-icon [chat-color-icon]
                   :title       (label :t/change-color)
                   :handler     show-chat-color-picker}
                  ;; TODO not implemented: Notifications
                  (merge {:title    (label :t/notifications-title)
                          :subtitle (label :t/not-implemented)
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
                   :title      (label :t/clear-history)
                   ;; TODO show confirmation dialog?
                   :handler    #(dispatch [:clear-history])}
                  {:icon       :bin
                   :icon-style {:width  12
                                :height 18}
                   :title      (label :t/delete-and-leave)
                   ;; TODO show confirmation dialog?
                   :handler    #(dispatch [:leave-group-chat])}]]
    [view st/settings-container
     (for [setting settings]
       ^{:key setting} [setting-view setting])]))

(defview chat-icon []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  [view st/action
   [chat-icon-view-action chat-id group-chat name color false]])

(defn new-group-toolbar []
  [view
   [status-bar]
   [toolbar {:title         (label :t/chat-settings)
             :custom-action [chat-icon]}]])

(defn focus []
  (dispatch [:set ::name-input-focused true]))

(defn blur []
  (dispatch [:set ::name-input-focused false]))

(defn save []
  (dispatch [:set-chat-name]))

(defview chat-name []
  [name [:chat :name]
   new-name [:get :new-chat-name]
   validation-messages [:new-chat-name-validation-messages]
   focused? [:get ::name-input-focused]
   valid? [:new-chat-name-valid?]]
  [view
   [text {:style st/chat-name-text} (label :t/chat-name)]
   [view (st/chat-name-value-container focused?)
    [text-input {:style          st/chat-name-value
                 :ref            #(when (and % focused?) (.focus %))
                 :on-change-text #(dispatch [:set :new-chat-name %])
                 :on-focus       focus
                 :on-blur        blur}
     name]
    (if (or focused? (not= name new-name))
      [touchable-highlight {:style    (st/chat-name-btn-edit-container valid?)
                            :on-press save}
       [view [icon :ok_purple st/add-members-icon]]]
      [touchable-highlight {:style    (st/chat-name-btn-edit-container true)
                            :on-press focus}
       [view [text {:style st/chat-name-btn-edit-text} (label :t/edit)]]])]
   (when (pos? (count validation-messages))
     [text {:style st/chat-name-validation-message} (first validation-messages)])])

(defn group-settings []
  [view st/group-settings
   [new-group-toolbar]
   [scroll-view st/body
    [chat-name]
    [text {:style st/members-text} (label :t/members-title)]
    [touchable-highlight {:on-press #(dispatch [:navigate-to :add-participants])}
     ;; TODO add participants view is not in design
     [view st/add-members-container
      [icon :add_gray st/add-members-icon]
      [text {:style st/add-members-text}
       (label :t/add-members)]]]
    [chat-members]
    [text {:style st/settings-text}
     (label :t/settings)]
    [settings-view]]
   [color-settings]
   [member-menu]])

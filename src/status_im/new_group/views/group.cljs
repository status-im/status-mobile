(ns status-im.new-group.views.group
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
    [status-im.contacts.styles :as cst]
    [status-im.components.common.common :as common]
    [status-im.components.action-button.action-button :refer [action-button
                                                              action-button-disabled
                                                              action-separator]]
    [status-im.components.react :refer [view
                                        text
                                        icon
                                        touchable-highlight]]
    [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
    [status-im.components.styles :refer [color-blue color-gray5 color-light-blue]]
    [status-im.components.status-bar :refer [status-bar]]
    [status-im.components.toolbar-new.view :refer [toolbar]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.new-group.styles :as st]
    [status-im.i18n :refer [label]]))

(defn group-toolbar [group-type edit?]
  [view
   [status-bar]
   [toolbar
    {:title (label
              (if (= group-type :contact-group)
                (if edit? :t/edit-group :t/new-group)
                (if edit? :t/chat-settings :t/new-group-chat)))
     :actions [{:image :blank}]}]])

(defview group-name-view []
  [new-group-name [:get :new-chat-name]]
  [view st/group-name-container
   [text-input-with-label
    {:auto-focus        true
     :label             (label :t/name)
     :on-change-text    #(dispatch [:set :new-chat-name %])
     :default-value     new-group-name}]])

(defn add-btn [on-press]
  [action-button (label :t/add-members)
   :add_blue
                 on-press])

(defn delete-btn [on-press]
  [view st/settings-group-container
   [touchable-highlight {:on-press on-press}
    [view st/settings-group-item
     [view st/delete-icon-container
      [icon :close_red st/add-icon]]
     [view st/settings-group-text-container
      [text {:style st/delete-group-text}
       (label :t/delete-group)]
      [text {:style st/delete-group-prompt-text}
       (label :t/delete-group-prompt)]]]]])

(defn group-chat-settings-btns []
  [view st/settings-group-container
   [view {:opacity 0.4}
    [touchable-highlight {:on-press #()}
     [view st/settings-group-item
      [view st/settings-icon-container
       [icon :speaker_blue st/add-icon]]
      [view st/settings-group-text-container
       [text {:style st/settings-group-text}
        (label :t/mute-notifications)]]]]]
   [action-separator]
   [action-button (label :t/clear-history)
                  :close_blue
                  #(dispatch [:clear-history])]
   [action-separator]
   [touchable-highlight {:on-press #(dispatch [:leave-group-chat])}
    [view st/settings-group-item
     [view st/delete-icon-container
      [icon :arrow_right_red st/add-icon]]
     [view st/settings-group-text-container
      [text {:style st/delete-group-text}
       (label :t/leave-chat)]]]]])

(defn more-btn [contacts-limit contacts-count on-press]
  [view
   [common/list-separator]
   [view cst/show-all
    [touchable-highlight {:on-press on-press}
     [view
      [text {:style cst/show-all-text
             :uppercase? (get-in platform-specific [:uppercase?])
             :font (get-in platform-specific [:component-styles :contacts :show-all-text-font])}
       (str (- contacts-count contacts-limit) " " (label :t/more))]]]]])


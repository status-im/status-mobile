(ns status-im.new-group.views.group
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
    [status-im.contacts.styles :as cst]
    [status-im.components.react :refer [view
                                        text
                                        icon
                                        touchable-highlight]]
    [status-im.components.text-field.view :refer [text-field]]
    [status-im.components.styles :refer [color-blue color-gray5 color-light-blue]]
    [status-im.components.status-bar :refer [status-bar]]
    [status-im.components.toolbar-new.view :refer [toolbar]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.new-group.styles :as st]
    [status-im.i18n :refer [label]]))

(defn separator []
  [view cst/contact-item-separator-wrapper
   [view cst/contact-item-separator]])

(defview group-name-input []
  [new-group-name [:get :new-chat-name]]
  [view
   [text-field
    {:wrapper-style     st/group-chat-name-wrapper
     :line-color        color-gray5
     :focus-line-color  color-light-blue
     :focus-line-height st/group-chat-focus-line-height
     :label-hidden?     true
     :input-style       st/group-chat-name-input
     :auto-focus        true
     :on-change-text    #(dispatch [:set :new-chat-name %])
     :value             new-group-name}]])

(defn group-toolbar [group-type edit?]
  [view
   [status-bar]
   [toolbar
    {:title (label
              (if (= group-type :contact-group)
                (if edit? :t/edit-group :t/new-group)
                (if edit? :t/chat-settings :t/new-group-chat)))
     :actions [{:image :blank}]}]])

(defn group-name-view []
  [view st/chat-name-container
   [text {:style st/group-name-text}
    (label :t/name)]
   [group-name-input]])

(defn add-btn [on-press]
  [view st/add-button-container
   [touchable-highlight {:on-press on-press}
    [view st/add-container
     [view st/settings-icon-container
      [icon :add_blue st/add-icon]]
     [view st/settings-group-text-container
      [text {:style st/add-group-text}
       (label :t/add-members)]]]]])

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
   [separator]
   [touchable-highlight {:on-press #(dispatch [:clear-history])}
    [view st/settings-group-item
     [view st/settings-icon-container
      [icon :close_blue st/add-icon]]
     [view st/settings-group-text-container
      [text {:style st/settings-group-text}
       (label :t/clear-history)]]]]
   [separator]
   [touchable-highlight {:on-press #(dispatch [:leave-group-chat])}
    [view st/settings-group-item
     [view st/delete-icon-container
      [icon :arrow_right_red st/add-icon]]
     [view st/settings-group-text-container
      [text {:style st/delete-group-text}
       (label :t/leave-chat)]]]]])

(defn more-btn [contacts-limit contacts-count on-press]
  [view
   [view cst/contact-item-separator-wrapper
    [view cst/contact-item-separator]]
   [view cst/show-all
    [touchable-highlight {:on-press on-press}
     [view
      [text {:style cst/show-all-text
             :uppercase? (get-in platform-specific [:uppercase?])
             :font (get-in platform-specific [:component-styles :contacts :show-all-text-font])}
       (str (- contacts-count contacts-limit) " " (label :t/more))]]]]])


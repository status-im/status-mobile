(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.common.common :as common]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.contacts.screen :refer [contact-options]]
            [status-im.components.react :refer [view text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :refer [toolbar-background1]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.image-button.view :refer [scan-button]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn render-row [group edit?]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :on-press       #(dispatch [:open-chat-with-contact %])
                     :extended?      edit?
                     :extend-options (contact-options row group)}])))

(defview contact-list-toolbar-edit [group]
  [toolbar {:nav-action     (act/back #(dispatch [:set-in [:contact-list-ui-props :edit?] false]))
            :actions        [{:image :blank}]
            :title          (if-not group
                              (label :t/contacts)
                              (or (:name group) (label :t/contacts-group-new-chat)))}])

(defview contact-list-toolbar [group]
  [show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              (if-not group
                           (label :t/contacts)
                           (or (:name group) (label :t/contacts-group-new-chat)))
     :search-placeholder (label :t/search-contacts)
     :actions            [(act/opts [{:text (label :t/edit)
                                      :value #(dispatch [:set-in [:contact-list-ui-props :edit?] true])}])]}))

(defview contacts-list-view [group edit?]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]]
  [list-view {:dataSource                (lw/to-datasource contacts)
              :enableEmptySections       true
              :renderRow                 (render-row group edit?)
              :keyboardShouldPersistTaps true
              :renderHeader              renderers/list-header-renderer
              :renderFooter              renderers/list-footer-renderer
              :renderSeparator           renderers/list-separator-renderer
              :style                     st/contacts-list}])

(defview contact-list []
  [edit? [:get-in [:contact-list-ui-props :edit?]]
   group [:get :contacts-group]
   type [:get :group-type]]
  [drawer-view
   [view {:flex 1}
    [view
     [status-bar]
     (if edit?
       [contact-list-toolbar-edit group]
       [contact-list-toolbar group])]
    [contacts-list-view group edit?]]])

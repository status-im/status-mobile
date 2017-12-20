(ns status-im.ui.screens.contacts.contact-list.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.renderers.renderers :as renderers]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.screens.contacts.views :refer [contact-options]]
            [status-im.ui.components.react :refer [view list-view list-item]]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.drawer.view :refer [drawer-view]]
            [status-im.ui.screens.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defn render-row [group edit?]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :on-press       #(dispatch [:open-chat-with-contact %])
                     :extended?      edit?
                     :extend-options (contact-options row group)}])))

(defview contact-list-toolbar-edit [group]
  [toolbar/toolbar {}
   [toolbar/nav-button (act/back #(dispatch [:set-in [:contacts/list-ui-props :edit?] false]))]
   [toolbar/content-title
    (if-not group
      (label :t/contacts)
      (or (:name group) (label :t/contacts-group-new-chat)))]])

(defview contact-list-toolbar [group]
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    (toolbar/toolbar-with-search
      {:show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              (if-not group
                             (label :t/contacts)
                             (or (:name group) (label :t/contacts-group-new-chat)))
       :search-placeholder (label :t/search-contacts)
       :actions            [(act/opts [{:text (label :t/edit)
                                        :value #(dispatch [:set-in [:contacts/list-ui-props :edit?] true])}])]})))

(defview contacts-list-view [group edit?]
  (letsubs [contacts [:all-added-group-contacts-filtered (:group-id group)]]
    [list-view {:dataSource                (lw/to-datasource contacts)
                :enableEmptySections       true
                :renderRow                 (render-row group edit?)
                :keyboardShouldPersistTaps :always
                :renderHeader              renderers/list-header-renderer
                :renderFooter              renderers/list-footer-renderer
                :renderSeparator           renderers/list-separator-renderer
                :style                     st/contacts-list}]))

(defview contact-list []
  (letsubs [edit? [:get-in [:contacts/list-ui-props :edit?]]
            group [:get-contact-group]]
    [drawer-view
     [view {:flex 1}
      [view
       [status-bar]
       (if edit?
         [contact-list-toolbar-edit group]
         [contact-list-toolbar group])]
      [contacts-list-view group edit?]]]))


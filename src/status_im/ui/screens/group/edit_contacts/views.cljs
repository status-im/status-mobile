(ns status-im.ui.screens.group.edit-contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.components.renderers.renderers :as renderers]
            [status-im.ui.components.react :refer [view list-view list-item]]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.i18n :refer [label]]))

(defview contact-list-toolbar [title]
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    (toolbar-with-search
      {:show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              title
       :search-placeholder (label :t/search-contacts)})))

(defn contacts-list [contacts extended? extend-options]
  [view {:flex 1}
   [list-view {:dataSource                (to-datasource contacts)
               :enableEmptySections       true
               :renderRow                 (fn [row _ _]
                                            (list-item
                                              ^{:key row}
                                              [contact-view {:contact        row
                                                             :extended?      extended?
                                                             :extend-options (extend-options row)}]))
               :bounces                   false
               :keyboardShouldPersistTaps :always
               :renderSeparator           renderers/list-separator-renderer
               :renderFooter              renderers/list-footer-renderer
               :renderHeader              renderers/list-header-renderer}]])

(defn chat-extended-options [item]
  [{:value #(dispatch [:remove-group-chat-participants #{(:whisper-identity item)}])
    :text  (label :t/remove)}])

(defn contact-extended-options [group-id]
  (fn [item]
    [{:value               #(dispatch [:remove-contact-from-group
                                       (:whisper-identity item)
                                       group-id])
      :accessibility-label :remove-button
      :text                (label :t/remove-from-group)}]))

(defview edit-chat-group-contact-list []
  (letsubs [chat-name [:chat :name]
            contacts [:contacts-filtered :current-chat-contacts]
            current-pk [:get :current-public-key]
            group-admin [:chat :group-admin]]
    (let [admin? (= current-pk group-admin)]
      [view styles/group-container
       [status-bar]
       [contact-list-toolbar chat-name]
       [contacts-list
        contacts
        admin?
        chat-extended-options]])))

(defview contacts-list-view [group-id]
  (letsubs [contacts [:all-added-group-contacts-filtered group-id]]
    [contacts-list
     contacts
     true
     (contact-extended-options group-id)]))

(defview edit-contact-group-contact-list []
  (letsubs [group [:get-contact-group]]
    [view styles/group-container
     [status-bar]
     [contact-list-toolbar (:name group)]
     [contacts-list-view (:group-id group)]]))

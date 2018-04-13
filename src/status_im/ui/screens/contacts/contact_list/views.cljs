(ns status-im.ui.screens.contacts.contact-list.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.contact.contact :as contact-view]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.screens.contacts.styles :as styles]
            [status-im.ui.screens.contacts.views :as contact-options]
            [status-im.i18n :as i18n]))

;; TODO(alwx): this namespace is not used; should it be removed?

(defn render-row [group edit?]
  (fn [row _ _]
    [contact-view/contact-view {:contact        row
                                :on-press       #(re-frame/dispatch [:open-chat-with-contact %])
                                :extended?      edit?
                                :extend-options (contact-options/contact-options row group)}]))

(defview contact-list-toolbar-edit [group]
  [toolbar/toolbar {}
   [toolbar/nav-button (act/back #(re-frame/dispatch [:set-in [:contacts/list-ui-props :edit?] false]))]
   [toolbar/content-title
    (if-not group
      (i18n/label :t/contacts)
      (or (:name group) (i18n/label :t/contacts-group-new-chat)))]])

(defview contacts-list-view [group edit?]
  (letsubs [contacts [:get-all-added-group-contacts (:group-id group)]]
    [list/flat-list {:style                     styles/contacts-list
                     :data                      contacts
                     :key-fn                    :address
                     :render-fn                 (render-row group edit?)
                     :enableEmptySections       true
                     :keyboardShouldPersistTaps :always
                     :header                    list/default-header
                     :footer                    list/default-footer}]))

(defview contact-list []
  (letsubs [edit? [:get-in [:contacts/list-ui-props :edit?]]
            group [:get-contact-group]]
    [react/view {:flex 1}
     [react/view
      [status-bar/status-bar]
      (if edit?
        [contact-list-toolbar-edit group]
        [toolbar/simple-toolbar (if-not group
                                  (i18n/label :t/contacts)
                                  (or (:name group) (i18n/label :t/contacts-group-new-chat)))])]
     [contacts-list-view group edit?]]))

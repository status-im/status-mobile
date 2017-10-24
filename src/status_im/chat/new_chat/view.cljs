(ns status-im.chat.new-chat.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.action-button.action-button :refer [action-button
                                                                         action-separator]]
            [status-im.ui.components.action-button.styles :refer [actions-list]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.ui.components.drawer.view :as drawer]
            [status-im.chat.new-chat.styles :as styles]
            [status-im.i18n :as i18n]))

(defn options-list []
  [react/view actions-list
   [action-button {:label     (i18n/label :t/new-group-chat)
                   :icon      :icons/group-big
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:open-contact-toggle-list :chat-group])}]
   [action-separator]
   [action-button {:label     (i18n/label :t/new-public-group-chat)
                   :icon      :icons/public
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:navigate-to :new-public-chat])}]
   [action-separator]
   [action-button {:label     (i18n/label :t/add-new-contact)
                   :icon      :icons/add
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:navigate-to :new-contact])}]])

(defn contact-list-row [contact]
  [contact-view {:contact  contact
                 :on-press #(dispatch [:open-chat-with-contact %])}])

(defview new-chat-toolbar []
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    [react/view
     [status-bar]
     (toolbar-with-search
      {:show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              (i18n/label :t/contacts-group-new-chat)
       :search-placeholder (i18n/label :t/search-for)})]))

(defn- header [contacts]
  [react/view
   [options-list]
   [common/bottom-shadow]
   [common/form-title (i18n/label :t/choose-from-contacts)
    {:count-value (count contacts)}]
   [common/list-header]])

(defview new-chat []
  (letsubs [contacts [:all-added-group-contacts-filtered]
            params [:get :contacts/click-params]]
    [drawer/drawer-view
     [react/view styles/contacts-list-container
      [new-chat-toolbar]
      (when contacts
        [list/flat-list {:style                     styles/contacts-list
                         :data                      contacts
                         :render-fn                 contact-list-row
                         :bounces                   false
                         :keyboardShouldPersistTaps :always
                         :header                    (header contacts)
                         :footer                    [react/view
                                                     [common/list-footer]
                                                     [common/bottom-shadow]]}])]]))

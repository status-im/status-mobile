(ns status-im.chat.new-chat.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.common.common :as common]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.action-button.action-button :refer [action-button
                                                                      action-separator]]
            [status-im.components.action-button.styles :refer [actions-list]]
            [status-im.components.react :refer [view text list-view list-item]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.chat.new-chat.styles :as styles]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defn options-list []
  [view actions-list
   [action-button {:label    (label :t/new-group-chat)
                   :icon     [:icons/group_big {:color :blue}]
                   :on-press #(dispatch [:open-contact-toggle-list :chat-group])}]
   [action-separator]
   [action-button {:label    (label :t/new-public-group-chat)
                   :icon     [:icons/public {:color :blue}]
                   :on-press #(dispatch [:navigate-to :new-public-chat])}]
   [action-separator]
   [action-button {:label    (label :t/add-new-contact)
                   :icon     [:icons/add {:color :blue}]
                   :on-press #(dispatch [:navigate-to :new-contact])}]])

(defn contact-list-row []
  (fn [row _ _]
    (list-item ^{:key row}
               [contact-view {:contact  row
                              :on-press #(dispatch [:open-chat-with-contact %])}])))

(defview new-chat-toolbar []
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    [view
     [status-bar]
     (toolbar-with-search
      {:show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              (label :t/contacts-group-new-chat)
       :search-placeholder (label :t/search-for)})]))

(defview new-chat []
  (letsubs [contacts [:all-added-group-contacts-filtered]
            params [:get :contacts/click-params]]
    [drawer-view
     [view styles/contacts-list-container
      [new-chat-toolbar]
      (when contacts
        [list-view {:dataSource                (lw/to-datasource contacts)
                    :enableEmptySections       true
                    :renderRow                 (contact-list-row)
                    :bounces                   false
                    :keyboardShouldPersistTaps :always
                    :renderHeader              #(list-item
                                                  [view
                                                   [options-list]
                                                   [common/bottom-shadow]
                                                   [common/form-title (label :t/choose-from-contacts)
                                                    {:count-value (count contacts)}]
                                                   [common/list-header]])
                    :renderSeparator           renderers/list-separator-renderer
                    :renderFooter              #(list-item [view
                                                            [common/list-footer]
                                                            [common/bottom-shadow]])
                    :style                     styles/contacts-list}])]]))

(ns status-im.new-group.views.contact-toggle-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.react :refer [view
                                                keyboard-avoiding-view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.sticky-button :refer [sticky-button]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.utils.platform :refer [ios?]]
            [status-im.new-group.views.toggle-contact :refer [group-toggle-contact
                                                              group-toggle-participant]]
            [status-im.new-group.styles :as st]
            [status-im.contacts.styles :as cst]
            [status-im.i18n :refer [label]]
            [status-im.components.toolbar-new.actions :as act]))

(defn title-with-count [title count-value]
  [view st/toolbar-title-with-count
   [text {:style st/toolbar-title-with-count-text
          :font  :toolbar-title}
    title]
   (when (pos? count-value)
     [view st/toolbar-title-with-count-container
      [text {:style st/toolbar-title-with-count-text-count
             :font  :toolbar-title}
       count-value]])])

(defview toggle-list-toolbar [title contacts-count]
  [show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-group-list)
     :search-text        search-text
     :search-key         :contact-group-list
     :custom-title       (title-with-count title contacts-count)
     :search-placeholder (label :t/search-contacts)}))

(defn toggle-list [contacts render-function]
  [view {:flex 1}
   [list-view
    {:dataSource                (to-datasource contacts)
     :renderRow                 (fn [row _ _]
                                  (list-item ^{:key row} [render-function row]))
     :renderSeparator           renderers/list-separator-renderer
     :renderFooter              renderers/list-footer-renderer
     :renderHeader              renderers/list-header-renderer
     :style                     cst/contacts-list
     :keyboardShouldPersistTaps true}]])

(defview contact-toggle-list []
  [contacts [:all-added-group-contacts-filtered]
   selected-contacts-count [:selected-contacts-count]
   group-type [:get :group-type]]
  [keyboard-avoiding-view {:style st/group-container}
   [status-bar]
   [toggle-list-toolbar
    (label (if (= group-type :contact-group)
             :t/new-group
             :t/new-group-chat))
    selected-contacts-count]
   [toggle-list contacts group-toggle-contact]
   (when (pos? selected-contacts-count)
     [sticky-button (label :t/next) #(dispatch [:navigate-to :new-group])])])

(defview add-contacts-toggle-list []
  [contacts [:all-group-not-added-contacts-filtered]
   group [:get-contact-group]
   selected-contacts-count [:selected-contacts-count]]
  [keyboard-avoiding-view {:style st/group-container}
   [status-bar]
   [toggle-list-toolbar (:name group) selected-contacts-count]
   [toggle-list contacts group-toggle-contact]
   (when (pos? selected-contacts-count)
     [sticky-button (label :t/save) #(do
                                        (dispatch [:add-selected-contacts-to-group])
                                        (dispatch [:navigate-back]))])])

(defview add-participants-toggle-list []
  [contacts [:contacts-filtered :all-new-contacts]
   chat-name [:chat :name]
   selected-contacts-count [:selected-participants-count]]
  [keyboard-avoiding-view {:style st/group-container}
   [status-bar]
   [toggle-list-toolbar chat-name selected-contacts-count]
   [toggle-list contacts group-toggle-participant]
   (when (pos? selected-contacts-count)
     [sticky-button (label :t/save) #(do
                                        (dispatch [:add-new-participants])
                                        (dispatch [:navigate-back]))])])

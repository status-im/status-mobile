(ns status-im.new-group.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.styles :as st]
            [status-im.i18n :refer [label]]))

(defview contact-list-toolbar [title]
  [show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              title
     :search-placeholder (label :t/search-contacts)}))

(defn contacts-list [contacts renderer-function]
  [view {:flex 1}
   [list-view {:dataSource                (to-datasource contacts)
               :enableEmptySections       true
               :renderRow                 renderer-function
               :bounces                   false
               :keyboardShouldPersistTaps true
               :renderSeparator           renderers/list-separator-renderer
               :renderFooter              renderers/list-footer-renderer
               :renderHeader              renderers/list-header-renderer}]])

(defview chat-contacts-list-view []
  [contacts [:contacts-filtered :current-chat-contacts]
   current-pk [:get :current-public-key]
   group-admin [:chat :group-admin]]
  (let [admin? (= current-pk group-admin)]
    [contacts-list contacts (fn [row _ _]
                              (list-item
                                ^{:key row}
                                [contact-view {:contact        row
                                               :extended?      admin?
                                               :extend-options [{:value #(do
                                                                           (dispatch [:set :selected-participants
                                                                                      #{(:whisper-identity row)}])
                                                                           (dispatch [:remove-participants]))
                                                                 :text  (label :t/remove)}]}]))]))

(defview contacts-list-view [group]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]]
  [contacts-list contacts (fn [row _ _]
                            (list-item
                              ^{:key row}
                              [contact-view {:contact        row
                                             :extended?      true
                                             :extend-options [{:value #(dispatch [:remove-contact-from-group
                                                                                  (:whisper-identity row)
                                                                                  (:group-id group)])
                                                               :text  (label :t/remove-from-group)}]}]))])

(defview edit-chat-group-contact-list []
  [chat-name [:chat :name]]
  [view st/group-container
   [status-bar]
   [contact-list-toolbar chat-name]
   [chat-contacts-list-view]])

(defview edit-group-contact-list []
  [group [:get-contact-group]
   type [:get :group-type]]
  [view st/group-container
   [status-bar]
   [contact-list-toolbar (:name group)]
   [contacts-list-view group]])
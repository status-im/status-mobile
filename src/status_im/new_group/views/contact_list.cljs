(ns status-im.new-group.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.confirm-button :refer [confirm-button]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.group :refer [separator]]
            [status-im.new-group.styles :as st]
            [status-im.contacts.styles :as cst]
            [status-im.i18n :refer [label]]
            [status-im.components.toolbar-new.actions :as act]))

(defview contact-list-toolbar [title]
  [show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              title
     :search-placeholder (label :t/search-contacts)}))

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
             [separator]))

(defn render-spacing []
  #(list-item [view cst/contact-list-spacing]))

(defn render-row [group]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :extended?      true
                     :extend-options (when group
                                       [{:value #(dispatch [:remove-contact-from-group
                                                            (:whisper-identity row)
                                                            (:group-id group)])
                                         :text (label :t/remove-from-group)}])
                     :on-click       nil}])))

(defview contacts-list-view [group]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]]
  [view {:flex 1}
   [list-view {:dataSource                (to-datasource contacts)
               :enableEmptySections       true
               :renderRow                 (render-row group)
               :bounces                   false
               :keyboardShouldPersistTaps true
               :renderSeparator           render-separator
               :renderFooter              (render-spacing)
               :renderHeader              (render-spacing)}]])

(defview edit-group-contact-list []
  [group [:get-contact-group]
   type [:get :group-type]]
  [view st/group-container
   [status-bar]
   [contact-list-toolbar (:name group)]
   [contacts-list-view group]])

(defn render-chat-row [admin?]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :extended?      admin?
                     :extend-options [{:value #(do
                                                 (dispatch [:set :selected-participants #{(:whisper-identity row)}])
                                                 (dispatch [:remove-participants]))
                                       :text (label :t/remove)}]
                     :on-click       #()}])))

(defview chat-contacts-list-view []
  [contacts [:contacts-filtered :current-chat-contacts]
   current-pk [:get :current-public-key]
   group-admin [:chat :group-admin]]
  (let [admin? (= current-pk group-admin)]
    [view {:flex 1}
     [list-view {:dataSource                (to-datasource contacts)
                 :enableEmptySections       true
                 :renderRow                 (render-chat-row admin?)
                 :bounces                   false
                 :keyboardShouldPersistTaps true
                 :renderSeparator           render-separator
                 :renderFooter              (render-spacing)
                 :renderHeader              (render-spacing)}]]))

(defview edit-chat-group-contact-list []
  [chat-name [:chat :name]]
  [view st/group-container
   [status-bar]
   [contact-list-toolbar chat-name]
   [chat-contacts-list-view]])
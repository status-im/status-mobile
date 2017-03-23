(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.common.common :refer [separator]]
            [status-im.components.react :refer [view text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.text-field.view :refer [text-field]]
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
                     :extended?      edit?
                     :extend-options (when group
                                       [{:value #(dispatch [:hide-contact row])
                                         :text (label :t/delete-contact)}
                                        {:value #(dispatch [:remove-contact-from-group
                                                            (:whisper-identity row)
                                                            (:group-id group)])
                                         :text (label :t/remove-from-group)}])}])))

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

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
             [separator st/contact-item-separator]))

(defview contacts-list-view [group edit?]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]]
  [list-view {:dataSource                (lw/to-datasource contacts)
              :enableEmptySections       true
              :renderRow                 (render-row group edit?)
              :bounces                   false
              :keyboardShouldPersistTaps true
              :renderHeader              #(list-item [view st/contact-list-spacing])
              :renderFooter              #(list-item [view st/contact-list-spacing])
              :renderSeparator           render-separator
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

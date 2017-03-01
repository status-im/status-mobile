(ns status-im.new-group.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.confirm-button :refer [confirm-button]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]
            [status-im.contacts.styles :as cst]
            [status-im.i18n :refer [label]]))

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

(defn contact-list-toolbar [contacts-count show-search? search-text]
  (toolbar-with-search
    {:show-search?       (= show-search? :contact-group-list)
     :search-text        search-text
     :search-key         :contact-group-list
     :custom-title       (title-with-count (label :t/new-group) contacts-count)
     :search-placeholder (label :t/search-contacts)}))

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
             [view cst/contact-item-separator-wrapper
              [view cst/contact-item-separator]]))

(defview contact-group-list []
  [contacts [:all-added-group-contacts-filtered]
   selected-contacts-count [:selected-contacts-count]
   show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  [view st/new-group-container
   [status-bar]
   [contact-list-toolbar selected-contacts-count show-search search-text]
   [view {:flex 1}
    [list-view
     {:dataSource                (to-datasource contacts)
      :renderRow                 (fn [row _ _]
                                  (list-item ^{:key row} [new-group-contact row]))
      :renderSeparator           render-separator
      :style                     cst/contacts-list
      :keyboardShouldPersistTaps true}]]
   (when (pos? selected-contacts-count)
     [confirm-button (label :t/next) #(dispatch [:navigation-replace :contact-group])])])

(ns status-im.new-group.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.confirm-button :refer [confirm-button]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]
            [status-im.i18n :refer [label]]))

(defn contact-list-toolbar [contacts-count show-search?]
  (toolbar-with-search
    {:show-search?       (= show-search? :contact-group-list)
     :search-key         :contact-group-list
     :title              (str (label :t/new-group) " (" contacts-count ")")
     :search-placeholder (label :t/search-for)}))

(defview contact-group-list []
  [contacts [:all-added-group-contacts-filtered]
   selected-contacts-count [:selected-contacts-count]
   show-search [:get-in [:toolbar-search :show]]]
  [view st/new-group-container
   [status-bar]
   [contact-list-toolbar selected-contacts-count show-search]
   [view {:flex 1}
    [list-view
     {:dataSource                (to-datasource contacts)
      :renderRow                 (fn [row _ _]
                                  (list-item ^{:key row} [new-group-contact row]))
      :style                     st/contacts-list
      :keyboardShouldPersistTaps true}]]
   (when (pos? selected-contacts-count)
     [confirm-button (label :t/next) #(dispatch [:navigation-replace :contact-group])])])

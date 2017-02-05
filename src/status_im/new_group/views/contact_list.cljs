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
    {:show-search?       show-search?
     :search-key         :new-group-contact-list
     :title              (str (label :t/new-group) " " contacts-count)
     :search-placeholder (label :t/search-for)}))

(defview new-group-contact-list []
  [contacts [:all-added-contacts]
   selected-contacts-count [:selected-contacts-count]]
  [view st/new-group-container
   [status-bar]
   [contact-list-toolbar selected-contacts-count false]
   [view {:flex 1}
    [list-view
     {:dataSource (to-datasource contacts)
      :renderRow  (fn [row _ _]
                    (list-item [new-group-contact row]))
      :style      st/contacts-list}]]
   (when (pos? selected-contacts-count)
     [confirm-button (label :t/next) #(dispatch [:navigate-to :new-contacts-group])])])

(ns status-im.contacts.search-results
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                list-view
                                                list-item]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]))

(defview contacts-search-results []
  [search-text [:get :contact-list-search-text]
   contacts [:contacts-with-letters]]
  [view st/search-container
   [status-bar]
   [toolbar {:nav-action (act/back #(dispatch [:navigate-back]))
             :title      search-text
             :style      (get-in platform-specific [:component-styles :toolbar])}]
   (if (empty? contacts)
     [view st/search-empty-view
      ;; todo change icon
      [icon :group_big st/empty-contacts-icon]
      [text {:style st/empty-contacts-text}
       "No contacts found"]]
     [list-view {:dataSource (to-datasource contacts)
                 :renderRow  (fn [row _ _]
                               (list-item [contact-view {:contact   row
                                                         :letter?   true
                                                         :extended? true}]))}])])

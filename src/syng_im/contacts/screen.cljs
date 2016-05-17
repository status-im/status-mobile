(ns syng-im.contacts.screen
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text
                                              image
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.contacts.views.contact :refer [contact-view]]
            [syng-im.components.styles :refer [toolbar-background2]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.main-tabs :refer [main-tabs]]
            [syng-im.contacts.styles :as st]
            [syng-im.utils.listview :as lw]))

(defn render-row [row _ _]
  (list-item [contact-view row]))

(defn contact-list-toolbar []
  [toolbar {:title            "Contacts"
            :background-color toolbar-background2
            :action           {:image   {:source {:uri :icon_search}
                                         :style  st/search-icon}
                               :handler (fn [])}}])

(defview contact-list []
  [contacts [:get-contacts]]
  [view st/contacts-list-container
   [contact-list-toolbar]
   ;; todo what if there is no contacts, should we show some information
   ;; about this?
   (when contacts
     [list-view {:dataSource          (lw/to-datasource contacts)
                 :enableEmptySections true
                 :renderRow           render-row
                 :style               st/contacts-list}])
   [main-tabs]])

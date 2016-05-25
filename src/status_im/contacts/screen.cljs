(ns status-im.contacts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.styles :refer [toolbar-background2]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defn render-row [row _ _]
  (list-item [contact-view row]))

(defn contact-list-toolbar []
  [toolbar {:title            (label :t/contacts)
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
                 :style               st/contacts-list}])])

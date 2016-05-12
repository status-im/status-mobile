(ns syng-im.contacts.screen
  (:require-macros
    [natal-shell.data-source :refer [data-source clone-with-rows]]
    [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text
                                              image
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.contacts.views.contact :refer [contact-view]]
            [syng-im.components.styles :refer [toolbar-background2]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.contacts.styles :as st]
            [syng-im.utils.listview :as lw]))

(defn render-row [row _ _]
  (list-item [contact-view row]))

(defn get-data-source [contacts]
  (clone-with-rows (data-source {:rowHasChanged not=}) contacts))

(defn contact-list-toolbar []
  [toolbar {:title            "Contacts"
            :background-color toolbar-background2
            :action           {:image   {:source {:uri :icon_search}
                                         :style  st/search-icon}
                               :handler (fn [])}}])

(defn contact-list []
  (let [contacts (subscribe [:get :contacts])]
    (fn []
      (let [contacts-ds (lw/to-datasource2 @contacts)]
        [view st/contacts-list-container
         [contact-list-toolbar]
         (when contacts-ds
           [list-view {:dataSource          contacts-ds
                       :enableEmptySections true
                       :renderRow           render-row
                       :style               st/contacts-list}])]))))

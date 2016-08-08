(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-with-letter-view]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.styles :refer [color-blue
                                                 hamburger-icon
                                                 icon-search
                                                 create-icon
                                                 toolbar-background1]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.components.styles :as cst]))

(defn render-row [row _ _]
  (list-item [contact-with-letter-view row]))


(defview contact-list-toolbar [platform-specific]
  [group [:get :contacts-group]]
  [view
   [status-bar {:platform-specific platform-specific}]
   [toolbar {:title            (label (if (= group :dapps)
                                        :t/contacs-group-dapps
                                        :t/contacs-group-people))
             :background-color toolbar-background1
             :action           {:image   {:source {:uri :icon_search}
                                          :style  icon-search}
                                :handler (fn [])}}]])

(defview contact-list [{platform-specific :platform-specific}]
  [contacts [:contacts-with-letters]]
   [drawer-view {:platform-specific platform-specific}
    [view st/contacts-list-container
     [contact-list-toolbar platform-specific]
     ;; todo what if there is no contacts, should we show some information
     ;; about this?
     (when contacts
       [list-view {:dataSource          (lw/to-datasource contacts)
                   :enableEmptySections true
                   :renderRow           render-row
                   :style               st/contacts-list}])]])

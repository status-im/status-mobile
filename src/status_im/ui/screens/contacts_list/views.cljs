(ns status-im.ui.screens.contacts-list.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.contact.contact :as contact-view]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar.view])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn contacts-list-item [{:keys [public-key name photo-path] :as contact}]
  [list.views/big-list-item
   {:text         name
    :image-source photo-path
    :action-fn      #(re-frame/dispatch [:chat.ui/show-profile public-key])}])

(defview contacts-list []
  (letsubs [blocked-contacts-count [:contacts/blocked-count]
            contacts      [:contacts/active]]
    [react/view {:flex 1
                 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar.view/simple-toolbar (i18n/label :t/contacts)]
     [react/scroll-view {:style {:background-color colors/white
                                 :padding-top 16}}
      [list.views/big-list-item
       {:text                (i18n/label :t/blocked-users)
        :icon                :main-icons/cancel
        :icon-color          colors/red
        :accessibility-label :blocked-users-list-button
        :accessory-value     blocked-contacts-count
        :action-fn           #(re-frame/dispatch [:navigate-to :blocked-users-list])}]
      [react/view {:height 16}]
      [list.views/flat-list
       {:data                      contacts
        :key-fn                    :address
        :render-fn                 contacts-list-item}]]]))

(defview blocked-users-list []
  (letsubs [blocked-contacts [:contacts/blocked]]
    [react/view {:flex 1
                 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar.view/simple-toolbar (i18n/label :t/blocked-users)]
     [react/scroll-view {:style {:background-color colors/white
                                 :padding-vertical 8}}
      [list.views/flat-list
       {:data                      blocked-contacts
        :key-fn                    :address
        :render-fn                 contacts-list-item
        :default-separator?        true
        :enableEmptySections       true
        :keyboardShouldPersistTaps :always}]]]))

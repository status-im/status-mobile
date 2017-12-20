(ns status-im.ui.screens.main-tabs.views
  (:require [status-im.ui.components.drawer.view :refer [drawer-view]]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.tabs.styles :as tabs.styles]
            [status-im.ui.components.tabs.views :as tabs]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.chats-list.views :refer [chats-list]]
            [status-im.ui.screens.contacts.views :refer [contact-groups-list]]
            [status-im.ui.screens.discover.views :refer [discover]]
            [status-im.ui.screens.wallet.main.views :refer [wallet]])
  (:require-macros [status-im.utils.views :as views]))

(defn- tab-content [{:keys [title icon-active icon-inactive]}]
  (fn [active?]
    [react/view {:style tabs.styles/tab-container}
     (let [icon (if active? icon-active icon-inactive)]
       [react/view
        [vector-icons/icon icon (tabs.styles/tab-icon active?)]])
     [react/view
      [react/text {:style (tabs.styles/tab-title active?)}
       title]]]))

(def tabs-list*
  [{:view-id :wallet
    :content {:title         (i18n/label :t/wallet)
              :icon-inactive :icons/wallet
              :icon-active   :icons/wallet-active}
    :screen  wallet}
   {:view-id :chat-list
    :content {:title         (i18n/label :t/chats)
              :icon-inactive :icons/chats
              :icon-active   :icons/chats-active}
    :screen  chats-list}
   {:view-id :discover
    :content {:title         (i18n/label :t/discover)
              :icon-inactive :icons/discover
              :icon-active   :icons/discover-active}
    :screen  discover}
   {:view-id :contact-list
    :content {:title         (i18n/label :t/contacts)
              :icon-inactive :icons/contacts
              :icon-active   :icons/contacts-active}
    :screen contact-groups-list}])

(def tabs-list (map #(update % :content tab-content) tabs-list*))

(views/defview main-tabs []
  (views/letsubs [keyboard-height [:get :keyboard-height]
                  current-tab     [:get :view-id]]
    [react/view styles/flex
     [status-bar {:type (if (= current-tab :wallet) :wallet :main)}]
     [drawer-view
      [tabs/swipable-tabs tabs-list current-tab (zero? keyboard-height)
       {:bottom-tabs? true
        :navigation-event :navigate-to-tab}]]]))

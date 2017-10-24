(ns status-im.ui.screens.contacts.contact-list-modal.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.components.action-button.action-button :refer [action-button
                                                                         action-separator]]
            [status-im.ui.components.action-button.styles :refer [actions-list]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.ui.components.drawer.view :refer [drawer-view]]
            [status-im.ui.screens.contacts.styles :as st]
            [status-im.i18n :as i18n]))

(defview contact-list-modal-toolbar []
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    (toolbar-with-search
      {:modal?             true
       :show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              (i18n/label :t/contacts)
       :search-placeholder (i18n/label :t/search-contacts)})))

(defn actions-view [action click-handler]
  [react/view actions-list
   [action-button
    {:label     (i18n/label :t/enter-address)
     :icon      :icons/address
     :icon-opts {:color :blue}
     :on-press  #(do
                   (dispatch [:chat-webview-bridge/send-to-bridge
                              {:event (name :webview-send-transaction)}])
                   (dispatch [:navigate-back]))}]
   [action-separator]
   (if (= :request action)
     [action-button {:label     (i18n/label :t/show-qr)
                     :icon      :icons/qr
                     :icon-opts {:color :blue}
                     :on-press  #(click-handler :qr-scan action)}]
     [action-button {:label     (i18n/label :t/scan-qr)
                     :icon      :icons/fullscreen
                     :icon-opts {:color :blue}
                     :on-press  #(click-handler :qr-scan action)}])])

(defn render-row [click-handler action params]
  (fn [row _ _]
    [contact-view {:contact  row
                   :on-press #(when click-handler
                                (click-handler row action params))}]))


(defview contact-list-modal []
  (letsubs [contacts [:contacts-filtered :all-added-people-contacts]
            click-handler [:get :contacts/click-handler]
            action [:get :contacts/click-action]
            params [:get :contacts/click-params]]
    [drawer-view
     [react/view {:flex 1}
      [status-bar {:type :modal-white}]
      [contact-list-modal-toolbar]
      [list/flat-list {:style                     st/contacts-list-modal
                       :data                      contacts
                       :render-fn                 (render-row click-handler action params)
                       :header                    (when-not (:hide-actions? params)
                                                    [react/view
                                                     [actions-view action click-handler]
                                                     [common/bottom-shadow]
                                                     [common/form-title (i18n/label :t/choose-from-contacts)
                                                      {:count-value (count contacts)}]
                                                     [common/list-header]])
                       :footer                    [react/view
                                                   [common/list-footer]
                                                   [common/bottom-shadow]]
                       :enableEmptySections       true
                       :bounces                   false
                       :keyboardShouldPersistTaps :always}]]]))

(ns status-im.ui.screens.contacts.contact-list-modal.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.renderers.renderers :as renderers]
            [status-im.ui.components.react :refer [view list-view list-item]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.components.action-button.action-button :refer [action-button
                                                                         action-separator]]
            [status-im.ui.components.action-button.styles :refer [actions-list]]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.ui.components.drawer.view :refer [drawer-view]]
            [status-im.ui.screens.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defview contact-list-modal-toolbar []
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    (toolbar-with-search
      {:modal?             true
       :show-search?       (= show-search :contact-list)
       :search-text        search-text
       :search-key         :contact-list
       :title              (label :t/contacts)
       :search-placeholder (label :t/search-contacts)})))

(defn actions-view [action click-handler]
  [view actions-list
   [action-button
    {:label     (label :t/enter-address)
     :icon      :icons/address
     :icon-opts {:color :blue}
     :on-press  #(do
                   (dispatch [:send-to-webview-bridge
                              {:event (name :webview-send-transaction)}])
                   (dispatch [:navigate-back]))}]
   [action-separator]
   (if (= :request action)
     [action-button {:label     (label :t/show-qr)
                     :icon      :icons/qr
                     :icon-opts {:color :blue}
                     :on-press  #(click-handler :qr-scan action)}]
     [action-button {:label     (label :t/scan-qr)
                     :icon      :icons/fullscreen
                     :icon-opts {:color :blue}
                     :on-press  #(click-handler :qr-scan action)}])])

(defn render-row [click-handler action params]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact  row
                     :on-press #(when click-handler
                                  (click-handler row action params))}])))

(defview contact-list-modal []
  (letsubs [contacts [:contacts-filtered :all-added-people-contacts]
            click-handler [:get :contacts/click-handler]
            action [:get :contacts/click-action]
            params [:get :contacts/click-params]]
    [drawer-view
     [view {:flex 1}
      [status-bar {:type :modal-white}]
      [contact-list-modal-toolbar]
      [list-view {:dataSource                (lw/to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 (render-row click-handler action params)
                  :bounces                   false
                  :keyboardShouldPersistTaps :always
                  :renderHeader              (when-not (:hide-actions? params)
                                               #(list-item
                                                  [view
                                                   [actions-view action click-handler]
                                                   [common/bottom-shadow]
                                                   [common/form-title (label :t/choose-from-contacts)
                                                    {:count-value (count contacts)}]
                                                   [common/list-header]]))
                  :renderFooter              #(list-item [view
                                                          [common/list-footer]
                                                          [common/bottom-shadow]])
                  :renderSeparator           renderers/list-separator-renderer
                  :style                     st/contacts-list-modal}]]]))

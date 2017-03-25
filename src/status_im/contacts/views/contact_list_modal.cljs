(ns status-im.contacts.views.contact-list-modal
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [status-im.components.common.common :refer [list-separator form-title bottom-shaddow]]
    [status-im.components.react :refer [view text
                                        image
                                        icon
                                        touchable-highlight
                                        list-view
                                        list-item]]
    [status-im.contacts.views.contact :refer [contact-view]]
    [status-im.components.action-button.action-button :refer [action-button
                                                              action-separator]]
    [status-im.components.action-button.styles :refer [actions-list]]
    [status-im.components.text-field.view :refer [text-field]]
    [status-im.components.status-bar :refer [status-bar]]
    [status-im.components.toolbar-new.view :refer [toolbar-with-search toolbar]]
    [status-im.components.toolbar-new.actions :as act]
    [status-im.components.toolbar-new.styles :refer [toolbar-background1]]
    [status-im.components.drawer.view :refer [drawer-view open-drawer]]
    [status-im.components.image-button.view :refer [scan-button]]
    [status-im.contacts.styles :as st]
    [status-im.utils.listview :as lw]
    [status-im.i18n :refer [label]]
    [status-im.utils.platform :refer [platform-specific]]))

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
             [list-separator]))

(defview contact-list-modal-toolbar []
  [show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              (label :t/contacts)
     :search-placeholder (label :t/search-contacts)}))

(defn actions-view [action click-handler]
  [view actions-list
   [action-button (label :t/enter-address)
                  :address_blue
                  #(do
                     (dispatch [:send-to-webview-bridge
                                {:event (name :webview-send-transaction)}])
                     (dispatch [:navigate-back]))]
   [action-separator]
   (if (= :request action)
     [action-button (label :t/show-qr)
                    :q_r_blue
                    #(click-handler :qr-scan action)]
     [action-button (label :t/scan-qr)
                    :fullscreen_blue
                    #(click-handler :qr-scan action)])])

(defn render-row [click-handler action params]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact  row
                     :on-click #(when click-handler
                                  (click-handler row action params))}])))

(defview contact-list-modal []
  [contacts [:contacts-filtered :all-added-people-contacts]
   click-handler [:get :contacts-click-handler]
   action [:get :contacts-click-action]
   params [:get :contacts-click-params]]
  [drawer-view
   [view {:flex 1}
    [status-bar]
    [contact-list-modal-toolbar]
    [list-view {:dataSource                (lw/to-datasource contacts)
                :enableEmptySections       true
                :renderRow                 (render-row click-handler action params)
                :bounces                   false
                :keyboardShouldPersistTaps true
                :renderHeader              #(list-item
                                              [view
                                               [actions-view action click-handler]
                                               [bottom-shaddow]
                                               [form-title (label :t/choose-from-contacts) (count contacts)]
                                               [view st/contact-list-spacing]])
                :renderFooter              #(list-item [view
                                                        [view st/contact-list-spacing]
                                                        [bottom-shaddow]])
                :renderSeparator           render-separator
                :style                     st/contacts-list-modal}]]])

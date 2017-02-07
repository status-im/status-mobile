(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.image-button.view :refer [scan-button]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn new-group-chat-view []
  [view
   [touchable-highlight
    {:on-press #(dispatch [:navigate-to :new-group])}
    [view st/contact-container
     [view st/option-inner-container
      [view st/option-inner
       [image {:source {:uri :icon_private_group_big}
               :style  st/group-icon}]]
      [view st/info-container
       [text {:style st/name-text}
        (label :t/new-group-chat)]]]]]
   [touchable-highlight
    {:on-press #(dispatch [:navigate-to :new-public-group])}
    [view st/contact-container
     [view st/option-inner-container
      [view st/option-inner
       [image {:source {:uri :icon_public_group_big}
               :style  st/group-icon}]]
      [view st/info-container
       [text {:style st/name-text}
        (label :t/new-public-group-chat)]]]]]])

(defn render-row [chat-modal click-handler action params]
  (fn [row _ _]
    (list-item
      [contact-view {:contact  row
                     :letter?  chat-modal
                     :on-click (when click-handler
                                 #(click-handler row action params))}])))

(defn contact-list-entry [{:keys [click-handler icon icon-style label]}]
  [touchable-highlight
   {:on-press click-handler}
   [view st/contact-container
    [view st/contact-inner-container
     [image {:source {:uri icon}
             :style  icon-style}]
     [view st/info-container
      [text {:style           st/name-text
             :number-of-lines 1}
       label]]]]])

(defview contact-list-toolbar []
  [group [:get :contacts-group]
   modal [:get :modal]]
  [view
   [status-bar]
   [toolbar {:title            (label (if-not group
                                        :t/contacts
                                        (if (= group :dapps)
                                          :t/contacts-group-dapps
                                          :t/contacts-group-new-chat)))
             :nav-action       (when modal
                                 (act/back #(dispatch [:navigate-back])))
             :background-color toolbar-background1
             :style            (get-in platform-specific [:component-styles :toolbar])
             :actions          [(act/search #())]}]])

(defview contact-list []
  [contacts [:contacts-with-letters]
   group [:get :contacts-group]
   modal [:get :modal]
   click-handler [:get :contacts-click-handler]
   action [:get :contacts-click-action]
   params [:get :contacts-click-params]]
  (let [show-new-group-chat? (and (= group :people)
                                  (get-in platform-specific [:chats :new-chat-in-toolbar?]))]
    [drawer-view
     [view st/contacts-list-container
      [contact-list-toolbar]
      ;; todo add stub
      (when modal
        [view
         [contact-list-entry {:click-handler #(do
                                                (dispatch [:send-to-webview-bridge
                                                           {:event (name :webview-send-transaction)}])
                                                (dispatch [:navigate-back]))
                              :icon          :icon_enter_address
                              :icon-style    st/enter-address-icon
                              :label         (label :t/enter-address)}]
         [contact-list-entry {:click-handler #(click-handler :qr-scan action)
                              :icon          :icon_scan_q_r
                              :icon-style    st/scan-qr-icon
                              :label         (label (if (= :request action)
                                                      :t/show-qr
                                                      :t/scan-qr))}]])
      (when contacts
        [list-view {:dataSource          (lw/to-datasource contacts)
                    :enableEmptySections true
                    :renderRow           (render-row modal click-handler action params)
                    :bounces             false
                    :renderHeader        #(list-item
                                            [view
                                             (if show-new-group-chat?
                                               [new-group-chat-view])
                                             [view st/spacing-top]])
                    :renderFooter        #(list-item [view st/spacing-bottom])
                    :style               st/contacts-list}])]]))

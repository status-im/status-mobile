(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
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

(defn render-row [chat-modal click-handler action params group edit?]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :letter?        chat-modal
                     :extended?      edit?
                     :extend-options (when group
                                       [{:value #(dispatch [:hide-contact row])
                                         :text (label :t/delete-contact)}
                                        {:value #(dispatch [:remove-contact-from-group row group])
                                         :text (label :t/remove-from-group)}])
                     :on-click       (when (and (not edit?) click-handler)
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

(defview contact-list-toolbar-edit [group]
  [toolbar {:nav-action     (act/back #(dispatch [:set-in [:contact-list-ui-props :edit?] false]))
            :actions        [{:image :blank}]
            :title          (if-not group
                              (label :t/contacts)
                              (or (:name group) (label :t/contacts-group-new-chat)))}])

(defview contact-list-toolbar [group]
  [modal       [:get :modal]
   show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              (if-not group
                                 (label :t/contacts)
                                 (or (:name group) (label :t/contacts-group-new-chat)))
     :search-placeholder (label :t/search-contacts)
     :actions            (if modal
                           (act/back #(dispatch [:navigate-back]))
                           [(act/opts [{:text (label :t/edit)
                                        :value #(dispatch [:set-in [:contact-list-ui-props :edit?] true])}])])}))

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
              [view st/contact-item-separator-wrapper
               [view st/contact-item-separator]]))

(defview contacts-list-view [group modal click-handler action edit?]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]
   params [:get :contacts-click-params]]
  (let [show-new-group-chat? (and (= group :people)
                                  (get-in platform-specific [:chats :new-chat-in-toolbar?]))]
    (when contacts
      [list-view {:dataSource                (lw/to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 (render-row modal click-handler action params group edit?)
                  :bounces                   false
                  :keyboardShouldPersistTaps true
                  :renderHeader              #(list-item
                                                [view
                                                 (when show-new-group-chat?
                                                   [new-group-chat-view])])
                  :renderFooter              #(list-item [view st/spacing-bottom])
                  :renderSeparator           render-separator
                  :style                     st/contacts-list}])))

(defview contact-list []
  [action [:get :contacts-click-action]
   modal [:get :modal]
   edit? [:get-in [:contact-list-ui-props :edit?]]
   click-handler [:get :contacts-click-handler]
   group [:get :contacts-group]]
  [drawer-view
   [view {:flex 1}
    [view
     [status-bar]
     (if edit?
       [contact-list-toolbar-edit group]
       [contact-list-toolbar group])]
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
    [contacts-list-view group modal click-handler action edit?]]])

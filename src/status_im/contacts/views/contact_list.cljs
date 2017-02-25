(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                linear-gradient
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar-with-search toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.image-button.view :refer [scan-button]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific ios?]]))

(defn list-bottom-shadow []
  [linear-gradient {:style  {:height 4}
                    :colors st/list-bottom-shadow}])

(defn list-top-shadow []
  [linear-gradient {:style  {:height 3}
                    :colors st/list-top-shadow}])

(defn options-list-item [{:keys [on-press icon-uri label-key]}]
  [touchable-highlight {:on-press on-press}
   [view st/contact-container
    [view st/option-inner-container
     [view st/icon-container
      [image {:source {:uri icon-uri}
              :style  st/group-icon}]]
     [view st/info-container
      [text {:style st/option-name-text}
       (label label-key)]]]]])

(defn option-list-separator []
  [view st/option-list-separator-wrapper
   [view st/list-separator]])

(defn new-group-chat-view []
  [view
   [view st/new-chat-options
    [options-list-item {:on-press #(dispatch [:navigate-to :new-group])
                        :icon-uri :icon_private_group_big
                        :label-key :t/new-group-chat}]
    (when ios? [option-list-separator])
    [options-list-item {:on-press #(dispatch [:navigate-to :new-public-group])
                        :icon-uri :icon_public_group_big
                        :label-key :t/new-public-group-chat}]
    (when ios? [option-list-separator])
    [options-list-item {:on-press #(dispatch [:navigate-to :new-contact])
                        :icon-uri :icon_add_blue
                        :label-key :t/add-new-contact}]]
   (when-not ios? [list-bottom-shadow])])

(defn render-row [chat-modal click-handler action params]
  (fn [row _ _]
    (list-item
      ^{:key row}
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

(defn contact-list-title [contact-count]
  [view
   [view st/contact-list-title-container
    [text {:style st/contact-list-title
           :font  :medium}
     (label :t/choose-from-contacts)
     (when ios? [text st/contact-list-title-count " " contact-count])]]
   (when-not ios? [list-top-shadow])])

(defview contact-list-toolbar []
  [group       [:get :contacts-group]
   modal       [:get :modal]
   show-search [:get-in [:toolbar-search :show]]]
  [view
   [status-bar]
   (toolbar-with-search
     {:show-search?       (= show-search :contact-list)
      :search-key         :contact-list
      :title              (if-not group
                                  (label :t/contacts)
                                  (or (:name group) (label :t/contacts-group-new-chat)))
      :search-placeholder (label :t/search-for)
      :actions            (when modal
                            (act/back #(dispatch [:navigate-back])))})])

(defn render-separator [_ row-id _]
  (when ios? (list-item ^{:key row-id}
                        [view st/contact-list-separator-wrapper
                         [view st/list-separator]])))

(defview contacts-list-view [group modal click-handler action]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]
   params [:get :contacts-click-params]]
  (let [show-new-group-chat? (and (= group :people)
                                  (get-in platform-specific [:chats :new-chat-in-toolbar?]))]
    (when contacts
      [list-view {:dataSource                (lw/to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 (render-row modal click-handler action params)
                  :bounces                   false
                  :keyboardShouldPersistTaps true
                  :renderHeader              #(list-item
                                                [view
                                                 (when show-new-group-chat?
                                                   [new-group-chat-view])
                                                 [contact-list-title (count contacts)]
                                                 [view st/spacing-top]])
                  :renderSeparator           render-separator
                  :renderFooter              #(list-item [view st/spacing-bottom])
                  :style                     st/contacts-list}])))

(defview contact-list []
  [action [:get :contacts-click-action]
   modal [:get :modal]
   click-handler [:get :contacts-click-handler]
   group [:get :contacts-group]]
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
    [contacts-list-view group modal click-handler action]]])

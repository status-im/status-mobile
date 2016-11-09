(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view
                                                      on-press
                                                      contact-view-with-letter]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.styles :refer [icon-search
                                                 icon-back]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn new-group-chat-view []
  [touchable-highlight
   {:on-press #(dispatch [:navigate-to :new-group])}
   [view st/contact-container
    [view st/option-inner-container
     [view st/option-inner
      [image {:source {:uri :icon_menu_group}
              :style  st/option-inner-image}]]
     [view st/info-container
      [text {:style st/name-text}
       (label :t/new-group-chat)]]]]])

(defn render-row [chat-modal click-handler action params]
  (fn [row _ _]
    (list-item
      (if chat-modal
        [contact-view-with-letter row click-handler action params]
        [contact-view row
         (or click-handler
             (let [whisper-identity (:whisper-identity row)]
               (on-press whisper-identity)))]))))

(defn qr-scan [click-handler action]
  [touchable-highlight
   {:onPress #(click-handler :qr-scan action)}
   [view st/contact-container
    [view st/contact-inner-container
     [image {:source {:uri :icon_menu_group}
             :style  st/scan-qr-icon}]
     [view st/info-container
      [text {:style           st/name-text
             :number-of-lines 1}
       (label :t/scan-qr)]]]]])

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
                                 {:handler #(dispatch [:navigate-back])
                                  :image   {:source {:uri :icon_back}
                                            :style  icon-back}})
             :background-color toolbar-background1
             :style            (get-in platform-specific [:component-styles :toolbar])
             :actions          [{:image   {:source {:uri :icon_search}
                                           :style  icon-search}
                                 :handler (fn [])}]}]])

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
        [qr-scan click-handler action])
      (when contacts
        [list-view {:dataSource          (lw/to-datasource contacts)
                    :enableEmptySections true
                    :renderRow           (render-row modal click-handler action params)
                    :renderHeader        #(list-item
                                            [view
                                             (if show-new-group-chat?
                                               [new-group-chat-view])
                                             [view st/spacing-top]])
                    :renderFooter        #(list-item [view st/spacing-bottom])
                    :style               st/contacts-list}])]]))

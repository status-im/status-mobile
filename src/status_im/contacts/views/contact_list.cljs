(ns status-im.contacts.views.contact-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view on-press]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.styles :refer [icon-search]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]))

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

(defn render-row [click-handler]
  (fn [row _ _]
    (list-item
      [contact-view row
       (or click-handler
           (let [whisper-identity (:whisper-identity row)]
             (on-press whisper-identity)))])))

(defview contact-list-toolbar []
  [group [:get :contacts-group]]
  [view
   [status-bar]
   [toolbar {:title            (label (if (= group :dapps)
                                        :t/contacts-group-dapps
                                        :t/contacts-group-new-chat))
             :background-color toolbar-background1
             :style            (get-in platform-specific [:component-styles :toolbar])
             :actions          [{:image   {:source {:uri :icon_search}
                                           :style  icon-search}
                                 :handler (fn [])}]}]])

(defview contact-list []
  [contacts [:contacts-with-letters]
   group [:get :contacts-group]
   click-handler [:get :contacts-click-handler]]
  (let [show-new-group-chat? (and (= group :people)
                                  (get-in platform-specific [:chats :new-chat-in-toolbar?]))]
    [drawer-view
     [view st/contacts-list-container
      [contact-list-toolbar]
      ;; todo add stub
      (when contacts
        [list-view {:dataSource          (lw/to-datasource contacts)
                    :enableEmptySections true
                    :renderRow           (render-row click-handler)
                    :renderHeader        #(list-item
                                           [view
                                            (if show-new-group-chat?
                                              [new-group-chat-view])
                                            [view st/spacing-top]])
                    :renderFooter        #(list-item [view st/spacing-bottom])
                    :style               st/contacts-list}])]]))

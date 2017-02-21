(ns status-im.new-chat.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view text
                                                linear-gradient
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.new-chat.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [ios?]]))

(defn list-bottom-shadow []
  [linear-gradient {:style  {:height 4}
                    :colors st/list-bottom-shadow}])

(defn list-top-shadow []
  [linear-gradient {:style  {:height 3}
                    :colors st/list-top-shadow}])

(defn list-separator []
  (when ios?
    [view st/list-separator-wrapper
     [view st/list-separator]]))

(defn options-list-item [{:keys [on-press icon-uri label-key]}]
  [touchable-highlight {:on-press on-press}
   [view st/option-container
    [view st/option-inner-container
     [view st/option-icon-container
      [image {:source {:uri icon-uri}
              :style  st/option-icon}]]
     [view st/option-info-container
      [text {:style st/option-name-text}
       (label label-key)]]]]])

(defn options-list []
  [view
   [view (st/options-list)
    [options-list-item {:on-press #(dispatch [:open-contact-toggle-list :chat-group])
                        :icon-uri :icon_private_group_big
                        :label-key :t/new-group-chat}]
    [list-separator]
    [options-list-item {:on-press #(dispatch [:navigate-to :new-public-group])
                        :icon-uri :icon_public_group_big
                        :label-key :t/new-public-group-chat}]
    [list-separator]
    [options-list-item {:on-press #(dispatch [:navigate-to :new-contact])
                        :icon-uri :icon_add_blue
                        :label-key :t/add-new-contact}]]
   (when-not ios? [list-bottom-shadow])])

(defn contact-list-row []
  (fn [row _ _]
    (list-item ^{:key row} [contact-view {:contact row}])))

(defn contact-list-title [contact-count]
  [view
   [view st/contact-list-title-container
    [text {:style st/contact-list-title
           :font  :medium}
     (label :t/choose-from-contacts)
     (when ios? [text {:style st/contact-list-title-count
                       :font  :medium}
                 "  " contact-count])]]
   (when-not ios? [list-top-shadow])])

(defn contact-list-separator [_ row-id _]
  (when ios? (list-item ^{:key row-id} [list-separator])))

(defview new-chat-toolbar []
  [show-search [:get-in [:toolbar-search :show]]]
  [view
   [status-bar]
   (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-key         :contact-list
     :title              (label :t/contacts-group-new-chat)
     :search-placeholder (label :t/search-for)})])

(defview new-chat []
  [contacts [:all-added-group-contacts-filtered]
   params [:get :contacts-click-params]]
  [drawer-view
   [view st/contacts-list-container
    [new-chat-toolbar]
    (when contacts
      [list-view {:dataSource                (lw/to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 (contact-list-row)
                  :bounces                   false
                  :keyboardShouldPersistTaps true
                  :renderHeader              #(list-item
                                               [view
                                                [options-list]
                                                [contact-list-title (count contacts)]
                                                (when-not ios? [view st/spacing-top])])
                  :renderSeparator           contact-list-separator
                  :renderFooter              #(list-item (when-not ios? [view
                                                                         [view st/spacing-bottom]
                                                                         [list-bottom-shadow]]))
                  :style                     st/contacts-list}])]])

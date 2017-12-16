(ns status-im.ui.screens.group.add-contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.renderers.renderers :as renderers]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.sticky-button :refer [sticky-button]]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.ui.screens.contacts.styles :as cstyles]
            [status-im.i18n :refer [label]]
            [status-im.ui.components.contact.contact :refer [toogle-contact-view]]))

(defn on-toggle [checked? whisper-identity]
  (let [action (if checked? :deselect-contact :select-contact)]
    (dispatch [action whisper-identity])))

(defn on-toggle-participant [checked? whisper-identity]
  (let [action (if checked? :deselect-participant :select-participant)]
    (dispatch [action whisper-identity])))

(defn group-toggle-contact [{:keys [whisper-identity] :as contact}]
  [toogle-contact-view contact :is-contact-selected? on-toggle])

(defn group-toggle-participant [{:keys [whisper-identity] :as contact}]
  [toogle-contact-view contact :is-participant-selected? on-toggle-participant])

(defn title-with-count [title count-value]
  [react/view styles/toolbar-title-with-count
   [react/text {:style styles/toolbar-title-with-count-text
                :font  :toolbar-title}
    title]
   (when (pos? count-value)
     [react/view styles/toolbar-title-with-count-container
      [react/text {:style styles/toolbar-title-with-count-text-count
                   :font  :toolbar-title}
       count-value]])])

(defview toggle-list-toolbar [title contacts-count]
  (letsubs [show-search [:get-in [:toolbar-search :show]]
            search-text [:get-in [:toolbar-search :text]]]
    (toolbar-with-search
      {:show-search?       (= show-search :contact-group-list)
       :search-text        search-text
       :search-key         :contact-group-list
       :custom-title       (title-with-count title contacts-count)
       :search-placeholder (label :t/search-contacts)})))

(defn toggle-list [contacts render-function]
  [react/view {:flex 1}
   [react/list-view
    {:dataSource                (to-datasource contacts)
     :renderRow                 (fn [row _ _]
                                  (react/list-item ^{:key row} [render-function row]))
     :renderSeparator           renderers/list-separator-renderer
     :renderFooter              renderers/list-footer-renderer
     :renderHeader              renderers/list-header-renderer
     :style                     cstyles/contacts-list
     :keyboardShouldPersistTaps :always}]])

(defview contact-toggle-list []
  (letsubs [contacts [:all-added-group-contacts-filtered]
            selected-contacts-count [:selected-contacts-count]
            group-type [:get-group-type]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar
      (label (if (= group-type :contact-group)
               :t/new-group
               :t/new-group-chat))
      selected-contacts-count]
     [toggle-list contacts group-toggle-contact]
     (when (pos? selected-contacts-count)
       [sticky-button (label :t/next) #(dispatch [:navigate-to :new-group])])]))

(defview add-contacts-toggle-list []
  (letsubs [contacts [:all-group-not-added-contacts-filtered]
            group [:get-contact-group]
            selected-contacts-count [:selected-contacts-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar (:name group) selected-contacts-count]
     [toggle-list contacts group-toggle-contact]
     (when (pos? selected-contacts-count)
       [sticky-button (label :t/save) #(do
                                          (dispatch [:add-selected-contacts-to-group])
                                          (dispatch [:navigate-back]))])]))

(defview add-participants-toggle-list []
  (letsubs [contacts [:contacts-filtered :all-new-contacts]
            chat-name [:chat :name]
            selected-contacts-count [:selected-participants-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar chat-name selected-contacts-count]
     [toggle-list contacts group-toggle-participant]
     (when (pos? selected-contacts-count)
       [sticky-button (label :t/save) #(do
                                          (dispatch [:add-new-group-chat-participants])
                                          (dispatch [:navigate-back]))])]))

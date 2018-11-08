(ns status-im.ui.screens.group.add-contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as utils.platform]
            [status-im.ui.components.button.view :as buttons]
            [status-im.ui.components.contact.contact :refer [toggle-contact-view]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.group.styles :as styles]))

(defn- on-toggle [checked? public-key]
  (let [action (if checked? :deselect-contact :select-contact)]
    (re-frame/dispatch [action public-key])))

(defn- on-toggle-participant [checked? public-key]
  (let [action (if checked? :deselect-participant :select-participant)]
    (re-frame/dispatch [action public-key])))

(defn- group-toggle-contact [contact]
  [toggle-contact-view contact :is-contact-selected? on-toggle])

(defn- group-toggle-participant [contact]
  [toggle-contact-view contact :is-participant-selected? on-toggle-participant])

(defn- handle-invite-friends-pressed []
  (if utils.platform/desktop?
    (re-frame/dispatch [:navigate-to :new-contact])
    (list-selection/open-share {:message (i18n/label :t/get-status-at)})))

(defn- toggle-list-toolbar [{:keys [handler count label]} title]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title title]
   (when (pos? count)
     [toolbar/text-action {:handler             handler
                           :accessibility-label :next-button}
      label])])

(defn toggle-list [contacts render-function]
  [react/scroll-view {:flex 1}
   (if utils.platform/desktop?
     (for [contact contacts]
       ^{:key (:public-key contact)}
       (render-function contact))
     [list/flat-list {:style                     styles/contacts-list
                      :data                      contacts
                      :key-fn                    :address
                      :render-fn                 render-function
                      :keyboardShouldPersistTaps :always}])])

(defn no-contacts []
  [react/view {:style {:flex 1
                       :justify-content :center
                       :align-items :center}}
   [react/text
    {:style styles/no-contact-text}
    (i18n/label :t/group-chat-no-contacts)]
   [buttons/secondary-button {:on-press handle-invite-friends-pressed} (i18n/label :t/invite-friends)]])

;; Start group chat
(defview contact-toggle-list []
  (letsubs [contacts                [:contacts/added]
            selected-contacts-count [:selected-contacts-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar {:handler #(re-frame/dispatch [:navigate-to :new-group])
                           :label   (i18n/label :t/next)
                           :count   (pos? selected-contacts-count)}
      (i18n/label :t/group-chat)]
     (if (seq contacts)
       [toggle-list contacts group-toggle-contact]
       [no-contacts])]))

;; Add participants to existing group chat
(defview add-participants-toggle-list []
  (letsubs [contacts                [:contacts/not-in-current-chat]
            {:keys [name]}          [:chats/current]
            selected-contacts-count [:selected-participants-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar {:count   selected-contacts-count
                           :handler #(do
                                       (re-frame/dispatch [:group-chats.ui/add-members-pressed])
                                       (re-frame/dispatch [:navigate-back]))
                           :label   (i18n/label :t/add)}
      name]
     (when (seq contacts)
       [toggle-list contacts group-toggle-participant])]))

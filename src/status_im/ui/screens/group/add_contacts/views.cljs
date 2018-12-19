(ns status-im.ui.screens.group.add-contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as utils.platform]
            [status-im.ui.components.button.view :as buttons]
            [status-im.constants :as constants]
            [status-im.ui.components.contact.contact :refer [toggle-contact-view]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.group.styles :as styles]))

(defn- on-toggle [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

(defn- on-toggle-participant [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-participant public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-participant public-key allow-new-users?])))

(defn- group-toggle-contact [allow-new-users? contact]
  [toggle-contact-view
   contact
   :is-contact-selected?
   (partial on-toggle allow-new-users?)
   (and (not (:is-contact-selected? contact))
        (not allow-new-users?))])

(defn- group-toggle-participant [allow-new-users? contact]
  [toggle-contact-view
   contact
   :is-participant-selected?
   (partial on-toggle-participant allow-new-users?)
   ;; Disable if not-checked and we don't allow new users
   (and (not (:is-participant-selected? contact))
        (not allow-new-users?))])

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

(defn number-of-participants-disclaimer [number-of-participants-available]
  [react/view {:style styles/number-of-participants-disclaimer}
   [react/text (if (> number-of-participants-available
                      0)
                 (i18n/label-pluralize number-of-participants-available :t/available-participants)
                 (i18n/label :t/no-more-participants-available))]])

;; Start group chat
(defview contact-toggle-list []
  (letsubs [contacts                [:contacts/all-added-people-contacts]
            selected-contacts-count [:selected-contacts-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [status-bar]
     [toggle-list-toolbar {:handler #(re-frame/dispatch [:navigate-to :new-group])
                           :label   (i18n/label :t/next)
                           :count   (pos? selected-contacts-count)}
      (i18n/label :t/group-chat)]
     (when (seq contacts)
       [number-of-participants-disclaimer (- (dec constants/max-group-chat-participants) selected-contacts-count)])
     (if (seq contacts)
       [toggle-list contacts (partial group-toggle-contact (< selected-contacts-count (dec constants/max-group-chat-participants)))]
       [no-contacts])]))

;; Add participants to existing group chat
(defview add-participants-toggle-list []
  (letsubs [contacts                        [:contacts/all-contacts-not-in-current-chat]
            {:keys [name] :as current-chat} [:chats/current-chat]
            selected-contacts-count         [:selected-participants-count]]
    (let [current-participants-count (count (:contacts current-chat))]
      [react/keyboard-avoiding-view {:style styles/group-container}
       [status-bar]
       [toggle-list-toolbar {:count   selected-contacts-count
                             :handler #(do
                                         (re-frame/dispatch [:group-chats.ui/add-members-pressed])
                                         (re-frame/dispatch [:navigate-back]))
                             :label   (i18n/label :t/add)}
        name]

       [number-of-participants-disclaimer (- constants/max-group-chat-participants current-participants-count)]
       (when (seq contacts)
         [toggle-list contacts (partial group-toggle-participant (< (+ current-participants-count
                                                                       selected-contacts-count) constants/max-group-chat-participants))])])))

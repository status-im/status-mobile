(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as components.styles]
            [status-im.constants :as constants]
            [status-im.utils.platform :as utils.platform]
            [status-im.ui.components.contact.contact :refer [toggle-contact-view]]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.group.styles :as styles]))

(views/defview group-name-view [new-group-name]
  [react/view add-new.styles/input-container
   [react/text-input
    {:auto-focus          true
     :on-change-text      #(re-frame/dispatch [:set :new-chat-name %])
     :default-value       new-group-name
     :placeholder         (i18n/label :t/set-a-topic)
     :style               add-new.styles/input
     :accessibility-label :chat-name-input}]])

(defn- render-contact [contact]
  [contact/contact-view {:contact             contact
                         :style               styles/contact
                         :accessibility-label :chat-member-item}])

(defn- toolbar [header sub-header]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [react/view {:style styles/toolbar-header-container}
    [react/view
     [react/text header]]
    [react/view
     [react/text {:style styles/toolbar-sub-header} sub-header]]]])

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
  [react/view {:style styles/no-contacts}
   [react/text
    {:style styles/no-contact-text}
    (i18n/label :t/group-chat-no-contacts)]
   (when-not platform/desktop?
     [button/button
      {:type     :secondary
       :on-press handle-invite-friends-pressed
       :label    :t/invite-friends}])])

(views/defview bottom-container [{:keys [on-press disabled label accessibility-label]}]
  [react/view {:style {:height         52
                       :elevation      8
                       :shadow-radius  4
                       :shadow-offset  {:width 0 :height -5}
                       :shadow-opacity 0.3
                       :shadow-color   "rgba(0, 9, 26, 0.12)"}}
   [react/view {:style components.styles/flex}]
   [react/view {:style styles/bottom-container}
    [components.common/bottom-button
     {:forward?            true
      :accessibility-label (or accessibility-label :next-button)
      :label               label
      :disabled?           disabled
      :on-press            on-press}]]])

;; Start group chat
(views/defview contact-toggle-list []
  (views/letsubs [contacts                [:contacts/active]
                  selected-contacts-count [:selected-contacts-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [toolbar
      (i18n/label :t/new-group-chat)
      (i18n/label :t/group-chat-members-count
                  {:selected selected-contacts-count
                   :max      (dec constants/max-group-chat-participants)})]
     (if (seq contacts)
       [toggle-list contacts (partial group-toggle-contact (< selected-contacts-count (dec constants/max-group-chat-participants)))]
       [no-contacts])
     [bottom-container {:on-press #(re-frame/dispatch [:navigate-to :new-group])
                        :disabled (zero? selected-contacts-count)
                        :label (i18n/label :t/next)}]]))

;; Set name of new group-chat
(views/defview new-group []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:new-chat-name]]
    (let [save-btn-enabled? (and (spec/valid? :global/not-empty-string group-name) (pos? (count contacts)))]
      [react/keyboard-avoiding-view (merge {:behavior :padding}
                                           styles/group-container)
       [toolbar
        (i18n/label :t/new-group-chat)
        (i18n/label :t/group-chat-members-count
                    {:selected (count contacts)
                     :max      (dec constants/max-group-chat-participants)})]
       [group-name-view]
       [react/scroll-view
        [list/list-with-label {:flex 1}
         (i18n/label :t/members-title)
         [list/flat-list {:data                         contacts
                          :key-fn                       :address
                          :render-fn                    render-contact
                          :bounces                      false
                          :keyboard-should-persist-taps :always
                          :enable-empty-sections        true}]]]
       [bottom-container {:on-press #(re-frame/dispatch [:group-chats.ui/create-pressed group-name])
                          :disabled (string/blank? group-name)
                          :label (i18n/label :t/create-group-chat)
                          :accessibility-label :create-group-chat-button}]])))

;; Add participants to existing group chat
(views/defview add-participants-toggle-list []
  (views/letsubs [contacts                        [:contacts/all-contacts-not-in-current-chat]
                  {:keys [name] :as current-chat} [:chats/current-chat]
                  selected-contacts-count         [:selected-participants-count]]
    (let [current-participants-count (count (:contacts current-chat))]
      [react/keyboard-avoiding-view {:style styles/group-container}
       [toolbar
        name
        (i18n/label :t/group-chat-members-count
                    {:selected (dec (+ current-participants-count selected-contacts-count))
                     :max      (dec constants/max-group-chat-participants)})]
       (when (seq contacts)
         [toggle-list contacts (partial group-toggle-participant (< (+ current-participants-count
                                                                       selected-contacts-count) constants/max-group-chat-participants))])
       [bottom-container {:on-press
                          #(re-frame/dispatch [:group-chats.ui/add-members-pressed])
                          :disabled (zero? selected-contacts-count)
                          :label (i18n/label :t/add)}]])))

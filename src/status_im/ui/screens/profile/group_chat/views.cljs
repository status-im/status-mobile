(ns status-im.ui.screens.profile.group-chat.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as chat.sheets]
            [status-im.ui.screens.profile.components.styles
             :as
             profile.components.styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn member-sheet [chat-id member us-admin?]
  [react/view
   [quo/list-item
    {:theme               :accent
     :icon                [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo member)]
     :title               (contact/format-name member)
     :subtitle            (i18n/label :t/view-profile)
     :accessibility-label :view-chat-details-button
     :chevron             true
     :on-press            #(chat.sheets/hide-sheet-and-dispatch
                            [:chat.ui/show-profile
                             (:public-key member)])}]
   (when (and us-admin?
              (not (:admin? member)))
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/make-admin)
       :accessibility-label :make-admin
       :icon                :main-icons/make-admin
       :on-press            #(chat.sheets/hide-sheet-and-dispatch [:group-chats.ui/make-admin-pressed chat-id (:public-key member)])}])
   (when-not (:admin? member)
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/remove-from-chat)
       :accessibility-label :remove-from-chat
       :icon                :main-icons/remove-contact
       :on-press            #(chat.sheets/hide-sheet-and-dispatch [:group-chats.ui/remove-member-pressed chat-id (:public-key member)])}])])

(defn render-member [chat-id {:keys [public-key] :as member} admin? current-user-identity]
  [quo/list-item
   (merge
    {:title               (contact/format-name member)
     :accessibility-label :member-item
     :icon                [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo member)]
     :on-press            (when (not= public-key current-user-identity)
                            #(re-frame/dispatch [:chat.ui/show-profile public-key]))}
    (when (:admin? member)
      {:accessory      :text
       :accessory-text (i18n/label :t/group-chat-admin)})
    (when (and admin?
               (not (:admin? member))
               (not= public-key current-user-identity))
      {:accessory [quo/button {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                         {:content (fn []
                                                                                     [member-sheet chat-id member admin?])}])
                               :type                :icon
                               :theme               :icon
                               :accessibility-label :menu-option}
                   :main-icons/more]}))])

(defview chat-group-members-view [chat-id admin? current-user-identity]
  (letsubs [members [:contacts/current-chat-contacts]]
    (when (seq members)
      [list/flat-list {:data      members
                       :key-fn    :address
                       :render-fn #(render-member chat-id % admin? current-user-identity)}])))

(defn members-list [{:keys [chat-id admin? current-pk allow-adding-members?]}]
  [react/view
   [quo/list-header (i18n/label :t/members-title)]
   (when allow-adding-members?
     [quo/list-item
      {:title    (i18n/label :t/add-members)
       :icon     :main-icons/add-contact
       :theme    :accent
       :on-press #(re-frame/dispatch [:navigate-to :add-participants-toggle-list])}])
   [chat-group-members-view chat-id admin? current-pk]])

(defview group-chat-profile []
  (letsubs [{:keys [admins chat-id joined? chat-name color contacts] :as current-chat} [:chats/current-chat]
            members      [:contacts/current-chat-contacts]
            current-pk   [:multiaccount/public-key]]
    (when current-chat
      (let [admin?                (get admins current-pk)
            allow-adding-members? (and admin? joined?
                                       (< (count members) constants/max-group-chat-participants))]
        [react/view profile.components.styles/profile
         [quo/animated-header
          {:use-insets        true
           :left-accessories  [{:icon                :main-icons/arrow-left
                                :accessibility-label :back-button
                                :on-press            #(re-frame/dispatch [:navigate-back])}]
           :right-accessories (when (and admin? joined?)
                                [{:icon                :icons/edit
                                  :accessibility-label :edit-button
                                  :on-press            #(re-frame/dispatch [:navigate-to :edit-group-chat-name])}])
           :extended-header   (profile-header/extended-header
                               {:title         chat-name
                                :color         color
                                :subtitle      (i18n/label :t/members-count {:count (count contacts)})
                                :subtitle-icon :icons/tiny-group})}
          [react/view profile.components.styles/profile-form
           (when joined?
             [quo/list-item
              {:theme               :negative
               :title               (i18n/label :t/leave-chat)
               :accessibility-label :leave-chat-button
               :icon                :main-icons/arrow-left
               :on-press            #(re-frame/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])
           [members-list {:chat-id               chat-id
                          :admin?                admin?
                          :current-pk            current-pk
                          :allow-adding-members? allow-adding-members?}]]]]))))

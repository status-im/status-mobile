(ns legacy.status-im.ui.screens.profile.group-chat.views
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.common.common :as components.common]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.profile-header.view :as profile-header]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.chat.photos :as photos]
    [legacy.status-im.ui.screens.chat.utils :as chat.utils]
    [legacy.status-im.ui.screens.profile.components.styles :as profile.components.styles]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn member-sheet
  [chat-id member us-admin?]
  [react/view
   [list.item/list-item
    {:theme               :accent
     :icon                [chat-icon/contact-icon-contacts-tab member]
     :title               (profile.utils/displayed-name member)
     :subtitle            (i18n/label :t/view-profile)
     :accessibility-label :view-chat-details-button
     :chevron             true
     :on-press            #(hide-sheet-and-dispatch
                            [:chat.ui/show-profile
                             (:public-key member)])}]
   (when (and us-admin? (not (:admin? member)))
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/make-admin)
       :accessibility-label :make-admin
       :icon                :main-icons/make-admin
       :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/make-admin-pressed
                                                       chat-id (:public-key member)])}])
   (when-not (:admin? member)
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/remove-from-chat)
       :accessibility-label :remove-from-chat
       :icon                :main-icons/remove-contact
       :on-press            #(hide-sheet-and-dispatch
                              [:group-chats.ui/remove-member-pressed chat-id
                               (:public-key member)])}])])

(defn render-member
  [{:keys [public-key] :as member} _ _ {:keys [chat-id admin? current-user-identity]}]
  (let [{:keys [secondary-name]} member]
    [list.item/list-item
     (merge
      {:title               (profile.utils/displayed-name member)
       :subtitle            secondary-name
       :accessibility-label :member-item
       :icon                [chat-icon/contact-icon-contacts-tab member]
       :on-press            (when (not= public-key current-user-identity)
                              #(re-frame/dispatch [:chat.ui/show-profile public-key]))}
      (when (:admin? member)
        {:accessory      :text
         :accessory-text (i18n/label :t/group-chat-admin)})
      (when (and admin?
                 (not (:admin? member))
                 (not= public-key current-user-identity))
        {:accessory [quo/button
                     {:on-press            #(re-frame/dispatch
                                             [:bottom-sheet/show-sheet-old
                                              {:content (fn []
                                                          [member-sheet chat-id member admin?])}])
                      :type                :icon
                      :theme               :icon
                      :accessibility-label :menu-option}
                     :main-icons/more]}))]))

(defview chat-group-members-view
  [chat-id admin? current-user-identity]
  (letsubs [members [:contacts/current-chat-contacts]]
    (when (seq members)
      [list/flat-list
       {:data        members
        :key-fn      :address
        :render-data {:chat-id               chat-id
                      :admin?                admin?
                      :current-user-identity current-user-identity}
        :render-fn   render-member}])))

(defn members-list
  [{:keys [chat-id admin? current-pk allow-adding-members?]}]
  [react/view
   [quo/list-header (i18n/label :t/members-title)]
   (when allow-adding-members?
     [list.item/list-item
      {:title    (i18n/label :t/add-members)
       :icon     :main-icons/add-contact
       :theme    :accent
       :on-press #(re-frame/dispatch [:open-modal :add-participants-toggle-list])}])
   [chat-group-members-view chat-id admin? current-pk]])

(defn hide-sheet-and-dispatch-old
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (debounce/dispatch-and-chill event 2000))

(defn invitation-sheet
  [{:keys [id]} contact]
  (let [members               @(re-frame/subscribe [:contacts/current-chat-contacts])
        allow-adding-members? (< (count members) constants/max-group-chat-participants)]
    [react/view
     [react/view {:margin-bottom 8 :margin-right 16}
      [react/view {:padding-left 72}
       (chat.utils/format-author-old contact)]
      [react/view {:flex-direction :row :align-items :flex-end}
       [react/view {:padding-left 16 :padding-top 4}
        [photos/photo (profile.utils/photo contact) {:size 36}]]]]
     [list.item/list-item
      {:theme               :accent
       :disabled            (not allow-adding-members?)
       :title               (i18n/label :t/accept)
       :subtitle            (when-not allow-adding-members? (i18n/label :t/members-limit-reached))
       :accessibility-label :accept-invitation-button
       :icon                :main-icons/checkmark-circle
       :on-press            #(hide-sheet-and-dispatch-old
                              [:group-chats.ui/add-members-from-invitation id (:public-key contact)])}]
     [list.item/list-item
      {:theme               :negative
       :title               (i18n/label :t/decline)
       :accessibility-label :decline-invitation-button
       :icon                :main-icons/cancel
       :on-press            #(hide-sheet-and-dispatch-old [:send-group-chat-membership-rejection
                                                           id])}]]))

(defn contacts-list-item
  [{:keys [from] :as invitation}]
  (let [contact (or @(re-frame/subscribe [:contacts/contact-by-identity from])
                    {:public-key from})]
    [list.item/list-item
     {:title    (profile.utils/displayed-name contact)
      :icon     [chat-icon/contact-icon-contacts-tab contact]
      :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                     {:content (fn []
                                                 [invitation-sheet invitation contact])}])}]))

(defview group-chat-invite
  []
  (letsubs [{:keys [chat-id]} [:chats/current-chat]]
    (let [invitations @(re-frame/subscribe [:group-chat/pending-invitations-by-chat-id chat-id])]
      [react/view {:flex 1}
       [topbar/topbar {:title (i18n/label :t/group-invite)}]
       [react/scroll-view {:flex 1}
        [react/view {:margin-top 26}
         (if (seq invitations)
           [list/flat-list
            {:data      invitations
             :key-fn    :id
             :render-fn contacts-list-item}]
           [react/text
            {:style {:color              colors/gray
                     :margin-top         28
                     :text-align         :center
                     :padding-horizontal 16}}
            (i18n/label :t/empty-pending-invitations-descr)])]]])))

(defview group-chat-profile
  []
  (letsubs [{:keys [admins chat-id member? chat-name color contacts] :as current-chat}
            [:chats/current-chat]
            members [:contacts/current-chat-contacts]
            current-pk [:multiaccount/public-key]
            pinned-messages [:chats/pinned chat-id]]
    (when current-chat
      (let [admin?                (get admins current-pk)
            allow-adding-members? (and admin?
                                       member?
                                       (< (count members) constants/max-group-chat-participants))]
        [react/view profile.components.styles/profile
         [quo/animated-header
          {:use-insets        true
           :left-accessories  [{:icon                :main-icons/arrow-left
                                :accessibility-label :back-button
                                :on-press            #(re-frame/dispatch [:navigate-back])}]
           :right-accessories (when (and admin? member?)
                                [{:icon                :icons/edit
                                  :accessibility-label :edit-button
                                  :on-press            #(re-frame/dispatch [:open-modal
                                                                            :edit-group-chat-name])}])
           :extended-header   (profile-header/extended-header
                               {:title         chat-name
                                :color         color
                                :subtitle      (i18n/label :t/members-count {:count (count contacts)})
                                :subtitle-icon :icons/tiny-group})}
          [react/view profile.components.styles/profile-form
           (when admin?
             [list.item/list-item
              {:chevron             true
               :title               (i18n/label :t/membership-requests)
               :accessibility-label :invite-chat-button
               :icon                :main-icons/share
               :accessory           (let [invitations
                                          (count @(re-frame/subscribe
                                                   [:group-chat/pending-invitations-by-chat-id
                                                    chat-id]))]
                                      (when (pos? invitations)
                                        [components.common/counter {:size 22} invitations]))
               :on-press            #(re-frame/dispatch [:navigate-to :group-chat-invite])}])
           [list.item/list-item
            {:title          (i18n/label :t/pinned-messages)
             :icon           :main-icons/pin
             :accessory      :text
             :accessory-text (count pinned-messages)
             :chevron        true
             :on-press       #(re-frame/dispatch [:pin-message/show-pins-bottom-sheet chat-id])}]
           (when member?
             [list.item/list-item
              {:theme               :negative
               :title               (i18n/label :t/leave-chat)
               :accessibility-label :leave-chat-button
               :icon                :main-icons/arrow-left
               :on-press            #(re-frame/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])
           [members-list
            {:chat-id               chat-id
             :admin?                admin?
             :current-pk            current-pk
             :allow-adding-members? allow-adding-members?}]]]]))))

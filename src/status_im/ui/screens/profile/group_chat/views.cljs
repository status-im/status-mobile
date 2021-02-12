(ns status-im.ui.screens.profile.group-chat.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as chat.sheets]
            [status-im.ui.screens.profile.components.styles
             :as
             profile.components.styles]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn member-sheet [chat-id member us-admin?]
  (let [[first-name _] (multiaccounts/contact-two-names member false)]
    [react/view
     [quo/list-item
      {:theme               :accent
       :icon                [chat-icon/contact-icon-contacts-tab
                             (multiaccounts/displayed-photo member)]
       :title               first-name
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
         :on-press            #(chat.sheets/hide-sheet-and-dispatch [:group-chats.ui/remove-member-pressed chat-id (:public-key member)])}])]))

(defn render-member [{:keys [public-key] :as member} _ _ {:keys [chat-id admin? current-user-identity]}]
  (let [[first-name second-name] (multiaccounts/contact-two-names member false)]
    [quo/list-item
     (merge
      {:title               first-name
       :subtitle            second-name
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
                     :main-icons/more]}))]))

(defview chat-group-members-view [chat-id admin? current-user-identity]
  (letsubs [members [:contacts/current-chat-contacts]]
    (when (seq members)
      [list/flat-list {:data        members
                       :key-fn      :address
                       :render-data {:chat-id               chat-id
                                     :admin?                admin?
                                     :current-user-identity current-user-identity}
                       :render-fn   render-member}])))

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

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (debounce/dispatch-and-chill event 2000))

(defn invitation-sheet [{:keys [introduction-message id]} contact]
  (let [members @(re-frame/subscribe [:contacts/current-chat-contacts])
        allow-adding-members? (< (count members) constants/max-group-chat-participants)]
    [react/view
     (let [message {:content       {:parsed-text
                                    [{:type     "paragraph"
                                      :children [{:literal introduction-message}]}]}
                    :content-type  constants/content-type-text}]
       [react/view {:margin-bottom 8 :margin-right 16}
        [react/view {:padding-left 72}
         (chat.utils/format-author (multiaccounts/displayed-name contact))]
        [react/view {:flex-direction :row :align-items :flex-end}
         [react/view {:padding-left 16 :padding-top 4}
          [photos/photo (multiaccounts/displayed-photo contact) {:size 36}]]
         [message/->message message {:on-long-press identity}]]])
     [quo/list-item
      {:theme               :accent
       :disabled            (not allow-adding-members?)
       :title               (i18n/label :t/accept)
       :subtitle            (when-not allow-adding-members?  (i18n/label :t/members-limit-reached))
       :accessibility-label :accept-invitation-button
       :icon                :main-icons/checkmark-circle
       :on-press            #(hide-sheet-and-dispatch
                              [:group-chats.ui/add-members-from-invitation id (:public-key contact)])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/decline)
       :accessibility-label :decline-invitation-button
       :icon                :main-icons/cancel
       :on-press            #(hide-sheet-and-dispatch [:send-group-chat-membership-rejection id])}]]))

(defn contacts-list-item [{:keys [from] :as invitation}]
  (let [contact (or @(re-frame/subscribe [:contacts/contact-by-identity from]) {:public-key from})]
    [quo/list-item
     {:title    (multiaccounts/displayed-name contact)
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo contact)]
      :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                     {:content (fn []
                                                 [invitation-sheet invitation contact])}])}]))

(defview group-chat-invite []
  (letsubs [{:keys [chat-id  chat-name]} [:chats/current-chat]
            current-pk   [:multiaccount/public-key]]
    (let [invite-link (universal-links/generate-link
                       :group-chat
                       :external
                       (str "args?a=" current-pk "&a1=" (js/encodeURI chat-name) "&a2=" chat-id))
          invitations  @(re-frame/subscribe [:group-chat/pending-invitations-by-chat-id chat-id])]
      [react/view {:flex 1}
       [topbar/topbar {:title       (i18n/label :t/group-invite)
                       :accessories [{:icon    :main-icons/share
                                      :handler #(list-selection/open-share {:message invite-link})}]}]
       [react/scroll-view {:flex 1}
        [react/view {:margin-top 26}
         [react/view {:padding-horizontal 16}
          [react/text {:style {:color colors/gray}} (i18n/label :t/group-invite-link)]
          [copyable-text/copyable-text-view
           {:copied-text invite-link}
           [react/view {:accessibility-label :invitation-link
                        :border-width 1 :border-color colors/gray-lighter
                        :justify-content :center :margin-top 10
                        :border-radius 8 :padding-horizontal 16 :padding-vertical 11}
            [react/text invite-link]]]
          [react/text {:style {:color colors/gray :margin-top 22}}
           (i18n/label :t/pending-invitations)]]
         (if (seq invitations)
           [list/flat-list
            {:data                      invitations
             :key-fn                    :id
             :render-fn                 contacts-list-item}]
           [react/text {:style {:color colors/gray :margin-top 28 :text-align :center
                                :padding-horizontal 16}}
            (i18n/label :t/empty-pending-invitations-descr)])]]])))

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
           (when admin?
             [quo/list-item
              {:chevron             true
               :title               (i18n/label :t/group-invite)
               :accessibility-label :invite-chat-button
               :icon                :main-icons/share
               :accessory           (let [invitations
                                          (count @(re-frame/subscribe
                                                   [:group-chat/pending-invitations-by-chat-id chat-id]))]
                                      (when (pos? invitations)
                                        [components.common/counter {:size 22} invitations]))
               :on-press            #(re-frame/dispatch [:navigate-to :group-chat-invite])}])
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

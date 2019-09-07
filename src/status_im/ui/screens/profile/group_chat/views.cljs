(ns status-im.ui.screens.profile.group-chat.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.utils.platform :as platform]
            [status-im.constants :as constants]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.screens.chat.sheets :as chat.sheets]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.topbar :as topbar]))

(defn group-chat-profile-toolbar [admin?]
  [topbar/topbar
   (when admin?
     {:accessories
      [{:icon                :icons/edit
        :accessibility-label :edit-button
        :handler             #(re-frame/dispatch [:navigate-to :edit-group-chat-name])}]})])

(defn member-sheet [chat-id member us-admin?]
  [react/view
   [list-item/list-item
    {:theme               :action
     :icon                (multiaccounts/displayed-photo member)
     :title               [chat.sheets/view-profile {:name   (contact/format-name member)
                                                     :helper :t/view-profile}]
     :accessibility-label :view-chat-details-button
     :accessories         [:chevron]
     :on-press            #(chat.sheets/hide-sheet-and-dispatch
                            [(if platform/desktop? :show-profile-desktop :chat.ui/show-profile)
                             (:public-key member)])}]
   (when (and us-admin?
              (not (:admin? member)))
     [list-item/list-item
      {:theme               :action
       :title               :t/make-admin
       :accessibility-label :make-admin
       ;; TODO(Ferossgp): Fix case for make admin icon
       :icon                :main-icons/make_admin
       :on-press            #(chat.sheets/hide-sheet-and-dispatch [:group-chats.ui/make-admin-pressed chat-id (:public-key member)])}])
   (when-not (:admin? member)
     [list-item/list-item
      {:theme               :action
       :title               :t/remove-from-chat
       :accessibility-label :remove-from-chat
       :icon                :main-icons/remove-contact
       :on-press            #(chat.sheets/hide-sheet-and-dispatch [:group-chats.ui/remove-member-pressed chat-id (:public-key member)])}])])

(defn render-member [chat-id {:keys [public-key] :as member} admin? current-user-identity]
  [list-item/list-item
   (merge
    {:title                (contact/format-name member)
     :accessibility-label :member-item
     :icon                [chat-icon/contact-icon-contacts-tab member]
     :on-press            (when (not= public-key current-user-identity)
                            #(re-frame/dispatch [(if platform/desktop? :show-profile-desktop :chat.ui/show-profile) public-key]))}
    (when (:admin? member)
      {:accessories [(i18n/label :t/group-chat-admin)]})
    (when (and admin?
               (not (:admin? member))
               (not= public-key current-user-identity))
      {:accessories [[react/touchable-highlight {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                                           {:content (fn []
                                                                                                       [member-sheet chat-id member admin?])}])
                                                 :accessibility-label :menu-option}
                      [vector-icons/icon :main-icons/more {:accessibility-label :options}]]]}))])

(defview chat-group-members-view [chat-id admin? current-user-identity]
  (letsubs [members [:contacts/current-chat-contacts]]
    (when (seq members)
      [list/flat-list {:data      members
                       :key-fn    :address
                       :render-fn #(render-member chat-id % admin? current-user-identity)}])))

(defn members-list [{:keys [chat-id admin? current-pk allow-adding-members?]}]
  [react/view
   [list-item/list-item {:title :t/members-title :type :section-header}]
   (when allow-adding-members?
     [list-item/list-item
      {:title    :t/add-members
       :icon     :main-icons/add-contact
       :theme    :action
       :on-press #(re-frame/dispatch [:navigate-to :add-participants-toggle-list])}])
   [chat-group-members-view chat-id admin? current-pk]])

(defview group-chat-profile []
  (letsubs [{:keys [admins chat-id joined?] :as current-chat} [:chats/current-chat]
            members      [:contacts/current-chat-contacts]
            changed-chat [:group-chat-profile/profile]
            current-pk   [:multiaccount/public-key]]
    (when current-chat
      (let [shown-chat            (merge current-chat changed-chat)
            admin?                (get admins current-pk)
            allow-adding-members? (and admin? joined?
                                       (< (count members) constants/max-group-chat-participants))]
        [react/view profile.components.styles/profile
         [group-chat-profile-toolbar (and admin? joined?)]
         [react/scroll-view
          [react/view profile.components.styles/profile-form
           [react/view {:style {:border-bottom-width 1
                                :padding-bottom      15
                                :margin-bottom       8
                                :border-bottom-color colors/gray-lighter}}
            [profile.components/group-header-display shown-chat]]
           (when joined?
             [list-item/list-item
              {:theme               :action
               :title               :t/leave-chat
               :accessibility-label :leave-chat-button
               :icon                :main-icons/arrow-left
               :on-press            #(re-frame/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])
           [members-list {:chat-id               chat-id
                          :admin?                admin?
                          :current-pk            current-pk
                          :allow-adding-members? allow-adding-members?}]]]]))))

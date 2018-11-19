(ns status-im.ui.screens.profile.group-chat.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.utils.platform :as platform]
            [status-im.ui.screens.profile.group-chat.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn group-chat-profile-toolbar [admin?]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   (when admin?
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:group-chat-profile/start-editing])}
      [react/view
       [react/text {:style               common.styles/label-action-text
                    :uppercase?          true
                    :accessibility-label :edit-button}
        (i18n/label :t/edit)]]])])

(defn group-chat-profile-edit-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/default-done {:handler   #(re-frame/dispatch [:group-chats.ui/save-pressed])
                          :icon      :icons/ok
                          :icon-opts {:color               colors/blue
                                      :accessibility-label :done-button}}]])

(defn actions [admin? chat-id]
  (concat
   (when admin?
     [{:label  (i18n/label :add-members)
       :icon   :icons/add
       :action #(re-frame/dispatch [:navigate-to :add-participants-toggle-list])}])
   [{:label               (i18n/label :t/clear-history)
     :icon                :icons/close
     :action              #(re-frame/dispatch [:chat.ui/clear-history-pressed])
     :accessibility-label :clear-history-button}
    {:label               (i18n/label :t/delete-chat)
     :icon                :icons/arrow-left
     :action              #(re-frame/dispatch [:group-chats.ui/remove-chat-pressed chat-id])
     :accessibility-label :delete-chat-button}]))

(defn member-actions [chat-id member]
  [{:action #(re-frame/dispatch [(if platform/desktop? :show-profile-desktop :chat.ui/show-profile) (:public-key member)])
    :label  (i18n/label :t/view-profile)}
   {:action #(re-frame/dispatch [:group-chats.ui/remove-member-pressed chat-id (:public-key member)])
    :label  (i18n/label :t/remove-from-chat)}])

(defn render-member
  [chat-id {:keys [name public-key current-account?] :as member}
   admin? current-user-identity]
  [react/view
   [contact/contact-view
    {:contact             member
     :extend-options      (member-actions chat-id member)
     :extend-title        name
     :extended?           (and admin? (not current-account?))
     :accessibility-label :member-item
     :inner-props         {:accessibility-label :member-name-text}
     :on-press            (when current-account?
                            #(re-frame/dispatch [(if platform/desktop?
                                                   :show-profile-desktop
                                                   :chat.ui/show-profile)
                                                 public-key]))}]])

(defn chat-group-members-view
  [chat-id members admin? current-public-key]
  (when (seq members)
    [react/view
     [list/flat-list {:data      members
                      :separator list/default-separator
                      :key-fn    :address
                      :render-fn #(render-member chat-id % admin? current-public-key)}]]))

(defn members-list
  [chat-id members admin? current-public-key]
  [react/view
   [profile.components/settings-title (i18n/label :t/members-title)]
   [chat-group-members-view chat-id members admin? current-public-key]])

(defview group-chat-profile []
  (letsubs [{:keys [admins chat-id] :as current-chat} [:chats/current]
            editing?     [:get :group-chat-profile/editing?]
            changed-chat [:get :group-chat-profile/profile]
            members      [:contacts/current-chat-contacts]
            current-pk   [:account/public-key]]
    (let [shown-chat (merge current-chat changed-chat)
          admin?     (admins current-pk)]
      [react/view profile.components.styles/profile
       [status-bar/status-bar]
       (if editing?
         [group-chat-profile-edit-toolbar]
         [group-chat-profile-toolbar admin?])
       [react/scroll-view
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header
          {:contact              shown-chat
           :editing?             editing?
           :allow-icon-change?   false
           :on-change-text-event :group-chats.ui/name-changed}]
         [list/action-list (actions admin? chat-id)
          {:container-style        styles/action-container
           :action-style           styles/action
           :action-label-style     styles/action-label
           :action-separator-style styles/action-separator
           :icon-opts              styles/action-icon-opts}]
         [members-list chat-id members admin? current-pk]]]])))

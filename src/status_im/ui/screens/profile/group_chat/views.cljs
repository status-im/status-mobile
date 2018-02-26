(ns status-im.ui.screens.profile.group-chat.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.profile.group-chat.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn group-chat-profile-toolbar []
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:group-chat-profile/start-editing])}
    [react/view
     [react/text {:style      common.styles/label-action-text
                  :uppercase? true}
      (i18n/label :t/edit)]]]])

(defn group-chat-profile-edit-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/default-done {:handler   #(re-frame/dispatch [:group-chat-profile/save-profile])
                          :icon      :icons/ok
                          :icon-opts {:color colors/blue}}]])

(defn actions [admin? chat-id]
  (concat
    ;; NOTE(goranjovic) - group chat participant removal has been temporarily disabled
    ;; due to this bug - https://github.com/status-im/status-react/issues/3463
    #_(when admin?
      [{:label  (i18n/label :add-members)
        :icon   :icons/add
        :action #(re-frame/dispatch [:navigate-to :add-participants-toggle-list])}])
    [{:label  (i18n/label :t/clear-history)
      :icon   :icons/close
      :action #(utils/show-confirmation (i18n/label :t/clear-history-title)
                                        (i18n/label :t/clear-group-history-confirmation)
                                        (i18n/label :t/clear-history-action)
                                        (fn [] (re-frame/dispatch [:clear-history])))}
     {:label  (i18n/label :t/delete-chat)
      :icon   :icons/delete
      :action #(utils/show-confirmation (i18n/label :t/delete-chat-title)
                                        (i18n/label :t/delete-group-chat-confirmation)
                                        (i18n/label :t/delete)
                                        (fn []              ;; TODO(goranjovic) - fix double dispatch after rebase agains group chat actions
                                           (re-frame/dispatch [:remove-chat chat-id])
                                           (re-frame/dispatch [:navigation-replace :home])))}
     {:label  (i18n/label :t/leave-group)
      :icon   :icons/arrow-left
      :action #(utils/show-confirmation (i18n/label :t/leave-group-title)
                                        (i18n/label :t/leave-group-confirmation)
                                        (i18n/label :t/leave-group-action)
                                        (fn [] (re-frame/dispatch [:leave-group-chat])))}]))

(defn contact-actions [contact]
  [{:action #(re-frame/dispatch [:show-profile (:whisper-identity contact)])
    :label  (i18n/label :t/view-profile)}
   ;; NOTE(goranjovic) - group chat participant removal has been temporarily disabled
   ;; due to this bug - https://github.com/status-im/status-react/issues/3463
   #_{:action #(re-frame/dispatch [:remove-group-chat-participants #{(:whisper-identity contact)}])
    :label  (i18n/label :t/remove-from-chat)}])

(defn render-contact [contact admin?]
  [react/view
   [contact/contact-view
    {:contact        contact
     :extend-options (contact-actions contact)
     :extend-title   (:name contact)
     :extended?      admin?}]])

(defview chat-group-contacts-view [admin?]
  (letsubs [contacts [:current-chat-contacts]]
    [react/view
     [list/flat-list {:data      contacts
                      :separator list/default-separator
                      :render-fn #(render-contact % admin?)}]]))

(defn members-list [admin?]
  [react/view
   [profile.components/settings-title (i18n/label :t/members-title)]
   [chat-group-contacts-view admin?]])

(defview group-chat-profile []
  (letsubs [current-chat [:get-current-chat]
            editing?     [:get :group-chat-profile/editing?]
            changed-chat [:get :group-chat-profile/profile]
            current-pk   [:get :current-public-key]
            group-admin  [:chat :group-admin]]
    (let [shown-chat (merge current-chat changed-chat)
          admin?     (= current-pk group-admin)]
      [react/view profile.components.styles/profile
       [status-bar/status-bar]
       (if editing?
         [group-chat-profile-edit-toolbar]
         [group-chat-profile-toolbar])
       [react/scroll-view
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header shown-chat editing? false nil :set-group-chat-name]
         [list/action-list (actions admin? (:chat-id current-chat))
          {:container-style        styles/action-container
           :action-style           styles/action
           :action-label-style     styles/action-label
           :action-separator-style styles/action-separator
           :icon-opts              styles/action-icon-opts}]
         [members-list admin?]]]])))

(ns status-im.ui.screens.profile.contact.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.profile.contact.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.list.views :as list]))

(defn profile-contact-toolbar []
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]])

(defn actions [{:keys [pending? whisper-identity dapp?]}]
  (concat (if pending?
            [{:label  (i18n/label :t/add-to-contacts)
              :icon   :icons/add-contact
              :action #(re-frame/dispatch [:add-contact whisper-identity])}]
            [{:label     (i18n/label :t/in-contacts)
              :icon      :icons/in-contacts
              :disabled? true}])
          [{:label  (i18n/label :t/send-message)
            :icon   :icons/chats
            :action #(re-frame/dispatch [:start-chat whisper-identity {:navigation-replace? true}])}]
          (when-not dapp?
            [{:label  (i18n/label :t/send-transaction)
              :icon   :icons/arrow-right
              :action #(re-frame/dispatch [:profile/send-transaction whisper-identity])}])))

(defn profile-info-item [{:keys [label value options accessibility-label]}]
  [react/view styles/profile-info-item
   [react/view (styles/profile-info-text-container options)
    [react/text {:style styles/profile-info-title}
     label]
    [react/view styles/profile-setting-spacing]
    [react/text {:style               styles/profile-setting-text
                 :accessibility-label accessibility-label
                 :selectable          true}
     value]]])

(defn profile-info-contact-code-item [whisper-identity]
  [profile-info-item
   {:label               (i18n/label :t/contact-code)
    :accessibility-label :profile-public-key
    :value               whisper-identity}])

(defn profile-info [{:keys [whisper-identity]}]
  [react/view
   [profile-info-contact-code-item whisper-identity]])

(defview profile []
  (letsubs [identity        [:current-contact-identity]
            maybe-contact   [:contact]]
    (let [contact (or maybe-contact (utils.contacts/whisper-id->new-contact identity))]
      [react/view profile.components.styles/profile
       [status-bar/status-bar]
       [profile-contact-toolbar]
       [react/scroll-view
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header contact false false nil nil]]
        [list/action-list (actions contact)
         {:container-style        styles/action-container
          :action-style           styles/action
          :action-label-style     styles/action-label
          :action-separator-style styles/action-separator
          :icon-opts              styles/action-icon-opts}]
        [react/view styles/contact-profile-info-container
         [profile-info contact]]]])))

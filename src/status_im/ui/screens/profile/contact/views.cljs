(ns status-im.ui.screens.profile.contact.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.contact.styles :as styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn profile-contact-toolbar []
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]])

(defn actions [{:keys [public-key added?]}]
  (concat (if added?
            [{:label               (i18n/label :t/in-contacts)
              :icon                :main-icons/in-contacts
              :disabled?           true
              :accessibility-label :in-contacts-button}]
            [{:label               (i18n/label :t/add-to-contacts)
              :icon                :main-icons/add-contact
              :action              #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
              :accessibility-label :add-to-contacts-button}])
          [{:label               (i18n/label :t/send-message)
            :icon                :main-icons/message
            :action              #(re-frame/dispatch [:contact.ui/send-message-pressed {:public-key public-key}])
            :accessibility-label :start-conversation-button}
           {:label               (i18n/label :t/send-transaction)
            :icon                :main-icons/send
            :action              #(re-frame/dispatch [:profile/send-transaction public-key])
            :accessibility-label :send-transaction-button}
           {:label               (i18n/label :t/share-profile-link)
            :icon                :main-icons/share
            :action              #(re-frame/dispatch [:profile/share-profile-link public-key])
            :accessibility-label :share-profile-link}]))

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

(defn profile-info-contact-code-item [public-key]
  [profile-info-item
   {:label               (i18n/label :t/contact-code)
    :accessibility-label :profile-public-key
    :value               public-key}])

(defn profile-info [{:keys [public-key]}]
  [react/view
   [profile-info-contact-code-item public-key]])

(defn block-contact-action [{:keys [blocked? public-key]}]
  [list/render-action
   {:label               (if blocked?
                           (i18n/label :t/unblock-contact)
                           (i18n/label :t/block-contact))
    :icon                :main-icons/cancel
    :action              (if blocked?
                           #(re-frame/dispatch [:contact.ui/unblock-contact-pressed public-key])
                           #(re-frame/dispatch [:contact.ui/block-contact-pressed public-key]))
    :accessibility-label (if blocked?
                           :unblock-contact
                           :block-contact)}
   {:action-style           styles/block-action
    :action-label-style     styles/block-action-label
    :icon-opts              styles/block-action-icon-opts}])

(defview profile []
  (letsubs [contact [:contacts/current-contact]]
    [react/view profile.components.styles/profile
     [status-bar/status-bar]
     [profile-contact-toolbar]
     [react/scroll-view
      [react/view profile.components.styles/profile-form
       [profile.components/profile-header
        {:contact              contact
         :editing?             false
         :allow-icon-change?   false}]]
      [list/action-list (actions contact)
       {:container-style        styles/action-container
        :action-style           styles/action
        :action-label-style     styles/action-label
        :action-subtext-style   styles/action-subtext
        :action-separator-style styles/action-separator
        :icon-opts              styles/action-icon-opts}]
      [react/view {:style {:height 16}}]
      [block-contact-action contact]
      [react/view styles/contact-profile-info-container
       [profile-info contact]]]]))

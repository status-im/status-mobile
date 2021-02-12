(ns status-im.ui.screens.contacts-list.views
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.invite.views :as invite]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn contacts-list-item [{:keys [public-key] :as contact}]
  (let [[first-name second-name] (multiaccounts/contact-two-names contact true)]
    [quo/list-item
     {:title    first-name
      :subtitle second-name
      :icon     [chat-icon.screen/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo contact)]
      :chevron  true
      :on-press #(re-frame/dispatch [:chat.ui/show-profile public-key])}]))

(defn add-new-contact []
  [quo/list-item
   {:icon                :main-icons/add
    :theme               :accent
    :title               (i18n/label :t/add-new-contact)
    :accessibility-label :add-new-contact-button
    :on-press            #(re-frame/dispatch [:navigate-to :new-contact])}])

(defview contacts-list []
  (letsubs [blocked-contacts-count [:contacts/blocked-count]
            contacts      [:contacts/active]]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/contacts)}]
     [react/scroll-view {:flex 1}
      [add-new-contact]
      (when (pos? blocked-contacts-count)
        [react/view {:margin-vertical 16}
         [quo/list-item
          {:title               (i18n/label :t/blocked-users)
           :icon                :main-icons/cancel
           :theme               :negative
           :accessibility-label :blocked-users-list-button
           :chevron             true
           :accessory           :text
           :accessory-text      blocked-contacts-count
           :on-press            #(re-frame/dispatch [:navigate-to :blocked-users-list])}]])
      (if (seq contacts)
        [list.views/flat-list
         {:data                      contacts
          :key-fn                    :address
          :render-fn                 contacts-list-item}]
        [react/view {:align-items     :center
                     :flex            1
                     :justify-content :center}
         [react/text {:style {:color colors/gray :margin-vertical 24}}
          (i18n/label :t/you-dont-have-contacts)]
         [invite/button]])]]))

(defview blocked-users-list []
  (letsubs [blocked-contacts [:contacts/blocked]]
    [react/view {:flex 1
                 :background-color colors/white}
     [topbar/topbar {:title (i18n/label :t/blocked-users)}]
     [react/scroll-view {:style {:background-color colors/white
                                 :padding-vertical 8}}
      [list.views/flat-list
       {:data                      blocked-contacts
        :key-fn                    :address
        :render-fn                 contacts-list-item
        :default-separator?        true
        :enableEmptySections       true
        :keyboardShouldPersistTaps :always}]]]))

(ns legacy.status-im.ui.screens.contacts-list.views
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon.screen]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.invite.views :as invite]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list.views]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defn contacts-list-item
  [{:keys [public-key] :as contact}]
  (let [{:keys [primary-name secondary-name customization-color]} contact]
    [list.item/list-item
     {:title    primary-name
      :subtitle secondary-name
      :icon     [chat-icon.screen/profile-photo-plus-dot-view
                 {:public-key          public-key
                  :full-name           primary-name
                  :customization-color (or customization-color :primary)
                  :photo-path          (profile.utils/photo contact)}]
      :chevron  true
      :on-press #(re-frame/dispatch [:chat.ui/show-profile public-key])}]))

(defn add-new-contact
  []
  [list.item/list-item
   {:icon                :main-icons/add
    :theme               :accent
    :title               (i18n/label :t/add-new-contact)
    :accessibility-label :add-new-contact-button
    :on-press            #(re-frame/dispatch [:open-modal :new-contact])}])

(defview contacts-list
  []
  (letsubs [blocked-contacts-count [:contacts/blocked-count]
            sorted-contacts        [:contacts/sorted-contacts]]
    [react/scroll-view {:flex 1}
     [add-new-contact]
     (when (pos? blocked-contacts-count)
       [react/view {:margin-vertical 16}
        [list.item/list-item
         {:title               (i18n/label :t/blocked-users)
          :icon                :main-icons/cancel
          :theme               :negative
          :accessibility-label :blocked-users-list-button
          :chevron             true
          :accessory           :text
          :accessory-text      blocked-contacts-count
          :on-press            #(re-frame/dispatch [:navigate-to :blocked-users-list])}]])
     (if (seq sorted-contacts)
       [list.views/flat-list
        {:data      sorted-contacts
         :key-fn    :address
         :render-fn contacts-list-item}]
       [react/view
        {:align-items     :center
         :flex            1
         :justify-content :center}
        [react/text {:style {:color colors/gray :margin-vertical 24}}
         (i18n/label :t/you-dont-have-contacts)]
        [invite/button]])]))

(defview blocked-users-list
  []
  (letsubs [blocked-contacts [:contacts/blocked]]
    [list.views/flat-list
     {:data                         blocked-contacts
      :key-fn                       :address
      :render-fn                    contacts-list-item
      :default-separator?           true
      :enableEmptySections          true
      :keyboard-should-persist-taps :always}]))

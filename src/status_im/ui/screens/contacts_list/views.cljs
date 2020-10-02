(ns status-im.ui.screens.contacts-list.views
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.icons.vector-icons :as icons])
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

(defview contacts-list []
  (letsubs [blocked-contacts-count [:contacts/blocked-count]
            contacts [:contacts/active]]
    [:<>
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
        {:data      contacts
         :key-fn    :address
         :render-fn contacts-list-item
         :footer    [react/view {:height 68}]}]
       [react/view {:padding-horizontal 32 :margin-top 32}
        [react/view {:border-width               1
                     :border-color               colors/gray-lighter
                     :border-top-right-radius    16
                     :border-bottom-left-radius  16
                     :border-top-left-radius     16
                     :border-bottom-right-radius 4
                     :padding-horizontal         12
                     :padding-vertical           6}
         [react/text {:style {:color colors/gray :line-height 22}}
          (i18n/label :t/contacts-descr)]]
        [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
         [react/view {:flex-direction :row
                      :align-items    :center
                      :align-self     :center
                      :padding        12}
          [react/text {:style {:color colors/blue :margin-right 8}}
           (i18n/label :t/add-contact)]
          [icons/icon :main-icons/add-contact {:color colors/blue}]]]])]))

(defview blocked-users-list []
  (letsubs [blocked-contacts [:contacts/blocked]]
    [react/view {:flex             1
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

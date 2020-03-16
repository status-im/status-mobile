(ns status-im.ui.screens.add-new.new-chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.new-chat.styles :as styles]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]))

(defn- render-row [row _ _]
  [list-item/list-item
   {:title       (multiaccounts/displayed-name row)
    :icon        [chat-icon/contact-icon-contacts-tab row]
    :accessories [:chevron]
    :on-press    #(re-frame/dispatch [:chat.ui/start-chat
                                      (:public-key row)
                                      {:navigation-reset? true}])}])

(defn- icon-wrapper [color icon]
  [react/view
   {:style {:margin-right 16
            :margin-top 11
            :width 32
            :height 32
            :border-radius 25
            :align-items :center
            :justify-content :center
            :background-color color}}
   icon])

(defn- input-icon
  [state]
  (case state
    :searching
    [icon-wrapper colors/gray
     [react/activity-indicator {:color colors/white-persist}]]

    :valid
    [react/touchable-highlight
     {:on-press #(debounce/dispatch-and-chill [:contact.ui/contact-code-submitted] 3000)}
     [icon-wrapper colors/blue
      [vector-icons/icon :main-icons/arrow-right {:color colors/white-persist}]]]

    [icon-wrapper colors/gray
     [vector-icons/icon :main-icons/arrow-right {:color colors/white-persist}]]))

(defn get-validation-label [value]
  (case value
    :invalid
    (i18n/label :t/user-not-found)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(views/defview new-chat []
  (views/letsubs [contacts      [:contacts/active]
                  {:keys [state ens-name public-key error]} [:contacts/new-identity]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:title       :t/new-chat
       :modal?      true
       :accessories [{:icon                :qr
                      :accessibility-label :scan-contact-code-button
                      :handler #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                    {:title   (i18n/label :t/new-contact)
                                                     :handler :contact/qr-code-scanned}])}]}]
     [react/view add-new.styles/new-chat-container
      [react/view (add-new.styles/new-chat-input-container)
       [react/text-input
        {:on-change-text
         #(do
            (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
            (debounce/debounce-and-dispatch [:new-chat/set-new-identity %] 600))
         :on-submit-editing
         #(when (= state :valid)
            (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted] 3000))
         :placeholder         (i18n/label :t/enter-contact-code)
         :style               add-new.styles/input
         ;; This input is fine to preserve inputs
         ;; so its contents will not be erased
         ;; in onWillBlur navigation event handler
         :preserve-input?     true
         :accessibility-label :enter-contact-code-input
         :return-key-type     :go}]]
      [react/view {:width 16}]
      [input-icon state]]
     [react/view {:min-height 30 :justify-content :flex-end}
      [react/text {:style styles/message}
       (cond (= state :error)
             (get-validation-label error)
             (= state :valid)
             (str (when ens-name (str ens-name " • "))
                  (utils/get-shortened-address public-key))
             :else "")]]
     [list/flat-list {:data                      contacts
                      :key-fn                    :address
                      :render-fn                 render-row
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps :always}]]))

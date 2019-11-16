(ns status-im.ui.screens.add-new.new-chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar.view]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.new-chat.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]))

(defn- render-row [row _ _]
  [list-item/list-item {:title       (multiaccounts/displayed-name row)
                        :icon        [chat-icon/contact-icon-contacts-tab row]
                        :accessories [:chevron]
                        :on-press    #(re-frame/dispatch [:chat.ui/start-chat (:public-key row) {:navigation-reset? true}])}])

(views/defview new-chat []
  (views/letsubs [contacts      [:contacts/active]
                  new-identity  [:contacts/new-identity]
                  error-message [:new-identity-error]]
    [react/view {:style {:flex 1}}
     [toolbar.view/simple-toolbar (i18n/label :t/new-chat) true]
     [react/view add-new.styles/new-chat-container
      [react/view add-new.styles/new-chat-input-container
       [react/text-input {:on-change-text      #(re-frame/dispatch [:new-chat/set-new-identity %])
                          :on-submit-editing   #(when (and new-identity (not error-message))
                                                  (re-frame/dispatch [:contact.ui/contact-code-submitted]))
                          :placeholder         (i18n/label :t/enter-contact-code)
                          :style               add-new.styles/input
                          ;; This input is fine to preserve inputs
                          ;; so its contents will not be erased 
                          ;; in onWillBlur navigation event handler
                          :preserve-input?     true
                          :accessibility-label :enter-contact-code-input
                          :return-key-type     :go}]]
      (when-not platform/desktop?
        [react/touchable-highlight {:on-press            #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                                              {:title (i18n/label :t/new-contact)
                                                                               :handler :contact/qr-code-scanned}])
                                    :style               add-new.styles/button-container
                                    :accessibility-label :scan-contact-code-button}
         [react/view
          [vector-icons/icon :main-icons/camera {:color colors/blue}]]])]
     (when error-message
       [react/text {:style styles/error-message}
        error-message])
     (when (seq contacts)
       [list-item/list-item {:title :t/contacts :type :section-header}])
     [list/flat-list {:data                      contacts
                      :key-fn                    :address
                      :render-fn                 render-row
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps :always}]]))

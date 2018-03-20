(ns status-im.ui.screens.add-new.new-chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.contact.contact :as contact-view]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar.view]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.new-chat.styles :as styles]
            [status-im.ui.screens.add-new.open-dapp.styles :as open-dapp.styles]))

(defn- render-row [row _ _]
  [contact-view/contact-view {:contact       row
                              :on-press      #(re-frame/dispatch [:start-chat (:whisper-identity %) {:navigation-replace? true}])
                              :show-forward? true}])

(views/defview new-chat []
  (views/letsubs [contacts      [:all-added-people-contacts]
                  error-message [:new-contact-error-message]]
    [react/keyboard-avoiding-view open-dapp.styles/main-container
     [status-bar/status-bar]
     [toolbar.view/simple-toolbar (i18n/label :t/new-chat)]
     [react/view add-new.styles/input-container
      [react/text-input {:on-change-text      #(re-frame/dispatch [:set :contacts/new-identity %])
                         :on-submit-editing   #(when-not error-message
                                                 (re-frame/dispatch [:add-contact-handler]))
                         :placeholder         (i18n/label :t/enter-contact-code)
                         :style               add-new.styles/input
                         :accessibility-label :enter-contact-code-input}]
      [react/touchable-highlight {:on-press            #(re-frame/dispatch [:scan-qr-code
                                                                            {:toolbar-title (i18n/label :t/new-contact)}
                                                                            :set-contact-identity-from-qr])
                                  :style               {:margin-right 14}
                                  :accessibility-label :scan-contact-code-button}
       [react/view
        [vector-icons/icon :icons/qr {:color colors/blue}]]]]
     [react/text {:style styles/error-message}
      error-message]
     [react/text {:style open-dapp.styles/list-title}
      (i18n/label :t/contacts)]
     [list/flat-list {:data                      contacts
                      :key-fn                    :address
                      :render-fn                 render-row
                      :default-separator?        true
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps :always}]]))

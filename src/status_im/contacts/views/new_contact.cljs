(ns status-im.contacts.views.new-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.components.styles :refer [toolbar-background2]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.components.styles :refer [color-blue
                                                 color-purple
                                                 hamburger-icon
                                                 search-icon
                                                 create-icon
                                                 import-qr-icon
                                                 toolbar-background2
                                                 form-text-input]]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]
            [status-im.utils.logging :as log]))

(defn import-qr-button []
  [touchable-highlight {:on-press #(dispatch [:scan-qr-code :new-contact])}
   [view st/import-qr-button
    [view st/import-qr-button-content
     [icon {:name :qr-scanner
            :style import-qr-icon}]
     [text {:style st/import-qr-text} (label :t/import-qr)]]]])

(defview contact-name-input [name]
   []
   [text-input
    {:underlineColorAndroid color-purple
     :style                 form-text-input
     :autoFocus             true
     :placeholder           (label :t/contact-name)
     :onChangeText          #(dispatch [:set-in [:new-contact :name] %])}
    name])

(defview contact-address-input [address]
   []
   [text-input
    {:underlineColorAndroid color-purple
     :style                 form-text-input
     :autoFocus             true
     :placeholder           (label :t/contact-address)
     :onChangeText          #(dispatch [:set-in [:new-contact :address] %])}
    address])

(defview new-contact []
  [{:keys [name address whisper-identity phone-number] :as new-contact} [:get :new-contact]
   qr-contact [:get-in [:qr-codes :new-contact]]]
  (let [_ (log/debug qr-contact)
        _ (when qr-contact (dispatch [:set-new-contact-from-qr :new-contact]))]
  [drawer-view
   [view st/contact-form-container
    [toolbar {:title            (label :t/new-contact)
              :background-color toolbar-background2
              :action           {:image   {:source {:uri :icon_add_gray}
                                           :style  search-icon}
                                 :handler (fn [] (dispatch [:add-new-contact new-contact]))}}]
    [import-qr-button]
    [contact-name-input name]
    [contact-address-input address]
    [text (str "Whisper identity: " whisper-identity)]
    ]]))
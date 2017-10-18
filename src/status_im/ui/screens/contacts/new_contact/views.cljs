(ns status-im.ui.screens.contacts.new-contact.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [cljs.spec.alpha :as s]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.text-field.view :as text-field]
            [status-im.ui.components.status-bar :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.styles :as components.styles :refer [icon-ok button-input-container button-input color-blue]]
            [status-im.ui.components.image-button.view :as scan-button]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.contacts.db :as v]
            [status-im.ui.screens.contacts.styles :as st]
            [status-im.utils.hex :as hex]
            [status-im.utils.platform :as platform]))

(defn- validation-error-message
  [whisper-identity {:keys [address public-key]} error]
  (cond
    (#{(hex/normalize-hex address) (hex/normalize-hex public-key)}
      (hex/normalize-hex whisper-identity))
    (i18n/label :t/can-not-add-yourself)

    (not (s/valid? :global/public-key whisper-identity))
    (i18n/label :t/enter-valid-public-key)

    (not (v/contact-can-be-added? whisper-identity))
    (i18n/label :t/contact-already-added)

    :else error))

(defn- toolbar-actions [new-contact-identity account error]
  (let [error-message (validation-error-message new-contact-identity account error)]
    [{:icon                :icons/ok
      :icon-opts           {:color (if (string/blank? error-message)
                                     components.styles/color-blue4
                                     components.styles/color-gray11)}
      :handler             #(when (string/blank? error-message)
                              (re-frame/dispatch [:add-contact-handler new-contact-identity]))
      :accessibility-label :confirm-button}]))

(defview contact-whisper-id-input [whisper-identity error]
  (letsubs [current-account [:get-current-account]]
    (let [error (when-not (string/blank? whisper-identity)
                  (validation-error-message whisper-identity current-account error))]
      [react/view button-input-container
       [text-field/text-field
        {:error               error
         :error-color         color-blue
         :input-style         st/qr-input
         :value               whisper-identity
         :wrapper-style       button-input
         :label               (i18n/label :t/public-key)
         :on-change-text      #(do
                                 (re-frame/dispatch [:set :contacts/new-identity %])
                                 (re-frame/dispatch [:set :contacts/new-public-key-error nil]))
         :accessibility-label :public-key-input}]
       [scan-button/scan-button {:show-label? (zero? (count whisper-identity))
                                 :handler     #(re-frame/dispatch [:scan-qr-code
                                                                   {:toolbar-title (i18n/label :t/new-contact)}
                                                                   :set-contact-identity-from-qr])}]])))

(defview new-contact []
  (letsubs [new-contact-identity [:get :contacts/new-identity]
            error [:get :contacts/new-public-key-error]
            account [:get-current-account]]
    [react/view st/contact-form-container
     [status-bar/status-bar]
     [toolbar/toolbar {:style (get-in platform/platform-specific [:component-styles :toolbar])}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/add-new-contact)]
      [toolbar/actions (toolbar-actions new-contact-identity account error)]]
     [react/view st/form-container
      [contact-whisper-id-input new-contact-identity error]]
     [react/view st/address-explication-container
      [react/text {:style st/address-explication
                   :font  :default}
       (i18n/label :t/address-explication)]]]))

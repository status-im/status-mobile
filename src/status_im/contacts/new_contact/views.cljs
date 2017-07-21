(ns status-im.contacts.new-contact.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-title-container
                                                         toolbar-title-text
                                                         toolbar-background1]]
            [status-im.components.styles :refer [icon-ok button-input-container button-input color-blue]]
            [status-im.components.image-button.view :refer [scan-button]]
            [status-im.i18n :refer [label]]
            [cljs.spec.alpha :as s]
            [status-im.contacts.styles :as st]
            [status-im.utils.hex :refer [normalize-hex]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.contacts.db :as v]))

(def toolbar-title
  [view toolbar-title-container
   [text {:style toolbar-title-text}
    (label :t/add-new-contact)]])

(defn- validation-error-message
  [whisper-identity {:keys [address public-key]} error]
  (cond
    (#{(normalize-hex address) (normalize-hex public-key)}
      (normalize-hex whisper-identity))
    (label :t/can-not-add-yourself)

    (not (s/valid? ::v/public-key whisper-identity))
    (label :t/enter-valid-public-key)

    (not (v/contact-can-be-added? whisper-identity))
    (label :t/contact-already-added)

    :else error))

(defn toolbar-actions [new-contact-identity account error]
  (let [error-message (validation-error-message new-contact-identity account error)]
    [{:image   {:source {:uri (if (str/blank? error-message)
                                :icon_ok_blue
                                :icon_ok_disabled)}
                :style  icon-ok}
      :handler #(when (str/blank? error-message)
                  (dispatch [:add-contact-handler new-contact-identity]))}]))

(defview contact-whisper-id-input [whisper-identity error]
  (letsubs [current-account [:get-current-account]]
    (let [error (when-not (str/blank? whisper-identity)
                  (validation-error-message whisper-identity current-account error))]
      [view button-input-container
       [text-field
        {:error          error
         :error-color    color-blue
         :input-style    st/qr-input
         :value          whisper-identity
         :wrapper-style  button-input
         :label          (label :t/public-key)
         :on-change-text #(do
                            (dispatch [:set :contacts/new-identity %])
                            (dispatch [:set :contacts/new-public-key-error nil]))}]
       [scan-button {:show-label? (zero? (count whisper-identity))
                     :handler     #(dispatch [:scan-qr-code
                                              {:toolbar-title (label :t/new-contact)}
                                              :set-contact-identity-from-qr])}]])))

(defview new-contact []
  (letsubs [new-contact-identity [:get :contacts/new-identity]
            error [:get :contacts/new-public-key-error]
            account [:get-current-account]]
    [view st/contact-form-container
     [status-bar]
     [toolbar {:background-color toolbar-background1
               :style            (get-in platform-specific [:component-styles :toolbar])
               :nav-action       (act/back #(dispatch [:navigate-back]))
               :title            (label :t/add-new-contact)
               :actions          (toolbar-actions new-contact-identity account error)}]
     [view st/form-container
      [contact-whisper-id-input new-contact-identity error]]
     [view st/address-explication-container
      [text {:style st/address-explication
             :font  :default}
       (label :t/address-explication)]]]))


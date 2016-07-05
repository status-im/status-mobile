(ns status-im.contacts.views.new-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as str]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.styles :refer [color-purple
                                                 color-white
                                                 icon-search
                                                 icon-back
                                                 icon-qr
                                                 toolbar-background1
                                                 toolbar-title-container
                                                 toolbar-title-text
                                                 button-input-container
                                                 button-input
                                                 form-text-input]]
            [status-im.qr-scanner.views.scan-button :refer [scan-button]]
            [status-im.i18n :refer [label]]
            [cljs.spec :as s]
            [status-im.contacts.validations :as v]
            [status-im.contacts.styles :as st]))


(def toolbar-title
  [view toolbar-title-container
   [text {:style toolbar-title-text}
    (label :t/add-new-contact)]])

(defview contact-name-input [name]
  []
  [text-field
   {:error (if (str/blank? name) "" nil)
    :errorColor "#7099e6"
    :value name
    :label           (label :t/name)
    :onChangeText          #(dispatch [:set-in [:new-contact :name] %])}])

(defview contact-whisper-id-input [whisper-identity]
  []
  (let [error (if (str/blank? whisper-identity) "" nil)
        error (if (s/valid? ::v/whisper-identity whisper-identity)
                error
                "Please enter a valid address or scan a QR code")]
    [view button-input-container
   [text-field
    {:error error
     :errorColor "#7099e6"
     :value whisper-identity
     :wrapperStyle                 (merge button-input)
     :label           (label :t/address)
     :onChangeText          #(dispatch [:set-in [:new-contact :whisper-identity] %])}]
   [scan-button {:showLabel (zero? (count whisper-identity))
                 :handler #(dispatch [:scan-qr-code {:toolbar-title (label :t/new-contact)} :set-new-contact-from-qr])}]]))

(defview new-contact []
  [{:keys [name whisper-identity phone-number] :as new-contact} [:get :new-contact]]
  (let [valid-contact? (s/valid? ::v/contact new-contact)]
    [view st/contact-form-container
     [toolbar {:background-color :white
               :nav-action       {:image   {:source {:uri :icon_back}
                                            :style  icon-back}
                                  :handler #(dispatch [:navigate-back])}
               :custom-content   toolbar-title
               :action           {:image   {:source {:uri (if valid-contact?
                                                            :icon_ok_blue
                                                            :icon_ok_disabled)}
                                            :style  icon-search}
                                  :handler #(when valid-contact? (dispatch [:add-new-contact (merge {:photo-path (identicon whisper-identity)} new-contact)]))}}]
     [view st/form-container
      [contact-name-input name]
      [contact-whisper-id-input whisper-identity]]
     [view st/address-explication-container
      [text {:style st/address-explication} (label :t/address-explication)]]]))

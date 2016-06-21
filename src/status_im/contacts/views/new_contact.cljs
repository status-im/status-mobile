(ns status-im.contacts.views.new-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
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
            [status-im.contacts.styles :as st]))



(def toolbar-title
  [view toolbar-title-container
   [text {:style toolbar-title-text}
    (label :t/add-new-contact)]])

(defview contact-name-input [name]
  []
  [text-input
   {:underlineColorAndroid "#0000001f"
    :placeholderTextColor  "#838c93de"
    :style                 form-text-input
    :autoFocus             true
    :placeholder           (label :t/name)
    :onChangeText          #(dispatch [:set-in [:new-contact :name] %])}
   name])

(defview contact-whisper-id-input [whisper-identity]
  [view button-input-container
   [text-input
    {:underlineColorAndroid "#0000001f"
     :placeholderTextColor  "#838c93de"
     :style                 (merge form-text-input button-input)
     :autoFocus             true
     :placeholder           (label :t/address)
     :onChangeText          #(dispatch [:set-in [:new-contact :whisper-identity] %])}
    whisper-identity]
   [scan-button #(dispatch [:scan-qr-code {:toolbar-title (label :t/new-contact)} :set-new-contact-from-qr])]])

(defview new-contact []
  [{:keys [name whisper-identity phone-number] :as new-contact} [:get :new-contact]]
  [drawer-view
   [view st/contact-form-container
    [toolbar {:background-color :white
              :nav-action     {:image   {:source {:uri :icon_back}
                                         :style  icon-back}
                               :handler  #(dispatch [:navigate-back])}
              :custom-content   toolbar-title
              :action           {:image   {:source {:uri :icon_ok_blue}
                                           :style  icon-search}
                                 :handler #(dispatch [:add-new-contact new-contact])}}]
    [view st/form-container
     [contact-name-input name]
     [contact-whisper-id-input whisper-identity]]
    [view st/address-explication-container
     [text {:style st/address-explication} (label :t/address-explication)]]]])

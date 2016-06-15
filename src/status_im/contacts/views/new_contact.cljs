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
                                                 white-form-text-input]]
            [status-im.qr-scanner.views.import-button :refer [import-button]]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]))



(def toolbar-title
  [view toolbar-title-container
   [text {:style (merge toolbar-title-text {:color color-white})}
    (label :t/new-contact)]])

(defview contact-name-input [name]
  []
  [text-input
   {:underlineColorAndroid color-white
    :placeholderTextColor  color-white
    :style                 white-form-text-input
    :autoFocus             true
    :placeholder           (label :t/contact-name)
    :onChangeText          #(dispatch [:set-in [:new-contact :name] %])}
   name])

(defview contact-whisper-id-input [whisper-identity]
  [view button-input-container
   [text-input
    {:underlineColorAndroid color-white
     :placeholderTextColor  color-white
     :style                 (merge white-form-text-input button-input)
     :autoFocus             true
     :placeholder           (label :t/whisper-identity)
     :onChangeText          #(dispatch [:set-in [:new-contact :whisper-identity] %])}
    whisper-identity]
   [import-button #(dispatch [:scan-qr-code {:toolbar-title (label :t/new-contact)} :set-new-contact-from-qr])]])

(defview new-contact []
  [{:keys [name whisper-identity phone-number] :as new-contact} [:get :new-contact]]
  [drawer-view
   [view st/contact-form-container
    [linear-gradient {:colors ["rgba(182, 116, 241, 1)" "rgba(107, 147, 231, 1)" "rgba(43, 171, 238, 1)"]
                      :start [0, 0]
                      :end [0.5, 1]
                      :locations [0, 0.8 ,1]
                      :style  st/gradient-background}]

    [toolbar {:background-color :transparent
              :nav-action     {:image   {:source {:uri :icon_back_white}
                                         :style  icon-back}
                               :handler  #(dispatch [:navigate-back])}
              :custom-content   toolbar-title
              :action           {:image   {:source {:uri :icon_add}
                                           :style  icon-search}
                                 :handler #(dispatch [:add-new-contact new-contact])}}]
    [view st/form-container
     [contact-whisper-id-input whisper-identity]
     [contact-name-input name]
     ]]])

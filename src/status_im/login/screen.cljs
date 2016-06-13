(ns status-im.login.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                linear-gradient
                                                touchable-highlight]]
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
                                                 white-form-text-input]]
            [status-im.qr-scanner.views.import-button :refer [import-button]]
            [status-im.i18n :refer [label]]
            [status-im.login.styles :as st]))

(def toolbar-title
  [view toolbar-title-container
   [text {:style (merge toolbar-title-text {:color color-white})}
    (label :t/login)]])

(defview address-input [address]
  [view button-input-container
   [text-input
    {:underlineColorAndroid color-white
     :placeholderTextColor  color-white
     :style                 (merge white-form-text-input button-input)
     :autoFocus             true
     :placeholder           (label :t/address)
     :onChangeText          #(dispatch [:set-in [:login :address] %])}
    address]
   [import-button #(dispatch [:scan-qr-code {:toolbar-title (label :t/login)} :set-address-from-qr])]])

(defview password-input []
  []
  [text-input
   {:underlineColorAndroid color-white
    :placeholderTextColor  color-white
    :style                 white-form-text-input
    :autoFocus             true
    :placeholder           (label :t/password)
    :onChangeText          #(dispatch [:set-in [:login :password] %])}
   ""])

(defview login []
  [{:keys [address]} [:get :login]]
   [view st/screen-container
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
              :action           {:image   {:style  icon-search}
                                 :handler #()}}]
    [view st/form-container
     [address-input (or address "")]
     [password-input]
     ]
    [view st/bottom-actions-container
     [view st/recover-text-container
      [touchable-highlight
       {:on-press #()}
       [text {:style st/recover-text} (label :t/recover-from-passphrase)]]]
     [view st/connect-button-container
      [touchable-highlight
       {:on-press #(dispatch [:login-account])}
       [view st/connect-button
        [text {:style st/connect-button-text} (label :t/connect)]]]]]])

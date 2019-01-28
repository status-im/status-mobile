(ns status-im.ui.screens.hardwallet.authentication-method.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.hardwallet.authentication-method.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.resources :as resources]))

(defn authentication-method-row [{:keys [title on-press icon]}]
  [react/touchable-highlight {:on-press on-press}
   [react/view styles/authentication-method-row
    [react/view styles/authentication-method-row-icon-container
     [vector-icons/icon icon {:color colors/blue}]]
    [react/view styles/authentication-method-row-wrapper
     [react/text {:style           styles/choose-authentication-method-row-text
                  :number-of-lines 1}
      title]]
    [vector-icons/icon :main-icons/next {:color colors/gray}]]])

(defn hardwallet-authentication-method []
  [react/view styles/container
   [status-bar/status-bar]
   [react/view components.styles/flex
    [toolbar/toolbar {}
     toolbar/default-nav-back
     nil]
    [common/separator]
    [react/view styles/choose-authentication-method
     [react/view styles/lock-image-container
      [react/image {:source (:keycard-lock resources/ui)
                    :style  styles/lock-image}]]
     [react/text {:style           styles/choose-authentication-method-text
                  :number-of-lines 3}
      (i18n/label :t/choose-authentication-method)]]
    [react/view styles/authentication-methods
     [authentication-method-row {:title    (i18n/label :t/keycard)
                                 :icon     :main-icons/keycard
                                 :on-press #(re-frame/dispatch [:hardwallet.ui/status-hardwallet-option-pressed])}]
     [authentication-method-row {:title    (i18n/label :t/password)
                                 :icon     :main-icons/password
                                 :on-press #(re-frame/dispatch [:hardwallet.ui/password-option-pressed])}]]]])

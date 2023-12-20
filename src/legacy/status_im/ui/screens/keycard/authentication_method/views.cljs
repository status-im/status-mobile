(ns legacy.status-im.ui.screens.keycard.authentication-method.views
  (:require
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.separator :as separator]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.keycard.authentication-method.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn authentication-method-row
  [{:keys [title on-press icon]}]
  [react/touchable-highlight {:on-press on-press}
   [react/view styles/authentication-method-row
    [react/view styles/authentication-method-row-icon-container
     [icons/icon icon {:color colors/blue}]]
    [react/view styles/authentication-method-row-wrapper
     [react/text
      {:style           styles/choose-authentication-method-row-text
       :number-of-lines 1}
      title]]
    [icons/icon :main-icons/next {:color colors/gray}]]])

(defn keycard-authentication-method
  []
  [react/view styles/container
   [react/view {:flex 1}
    [topbar/topbar]
    [separator/separator]
    [react/view styles/choose-authentication-method
     [react/view styles/lock-image-container
      [react/image
       {:source (resources/get-image :keycard-lock)
        :style  styles/lock-image}]]
     [react/text
      {:style           styles/choose-authentication-method-text
       :number-of-lines 3}
      (i18n/label :t/choose-authentication-method)]]
    [react/view styles/authentication-methods
     [authentication-method-row
      {:title    (i18n/label :t/keycard)
       :icon     :main-icons/keycard
       :on-press #(re-frame/dispatch [:onboarding.ui/keycard-option-pressed])}]
     [authentication-method-row
      {:title    (i18n/label :t/password)
       :icon     :main-icons/password
       :on-press #(re-frame/dispatch [:keycard.ui/password-option-pressed])}]]]])

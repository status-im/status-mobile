(ns status-im.ui.screens.wallet.onboarding.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.onboarding.styles :as styles]
            [status-im.react-native.resources :as resources]))

(defn onboarding []
  [react/view styles/root
   [react/view {:style styles/onboarding-image-container}
    [react/image {:source (:wallet-welcome resources/ui)
                  :style  styles/onboarding-image}]]
   [react/text {:style styles/onboarding-title}
    (i18n/label :t/wallet-onboarding-title)]
   [react/text {:style styles/onboarding-text}
    (i18n/label :t/wallet-onboarding-description)]

   [components.common/button
    {:button-style styles/set-up-button
     :label-style  styles/set-up-button-label
     :on-press     #(re-frame/dispatch [:navigate-to :wallet-onboarding-setup])
     :label        (i18n/label :t/wallet-onboarding-set-up)}]])
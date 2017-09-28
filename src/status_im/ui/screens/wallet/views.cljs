(ns status-im.ui.screens.wallet.views
  (:require [status-im.components.react :as react]
            [status-im.ui.screens.wallet.styles :as styles]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(defn error-message-view [error-container-style error-message-style]
  [react/view {:style error-container-style}
   [react/view {:style styles/error-container}
    [vector-icons/icon :icons/exclamation_mark {:color           :white
                                                :container-style styles/error-exclamation}]
    [react/text {:style error-message-style} (i18n/label :t/wallet-error)]]])

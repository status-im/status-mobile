(ns status-im.ui.screens.wallet.views
  (:require [status-im.components.react :as react]
            [status-im.ui.screens.wallet.styles :as styles]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(defn message-view [error-container-style error-message-style icon-container-style label]
  [react/view {:style error-container-style}
   [react/view {:style styles/error-container}
    [vector-icons/icon :icons/exclamation_mark {:color           :white
                                                :container-style icon-container-style}]
    [react/text {:style error-message-style} label]]])

(defn error-message-view [error-container-style error-message-style]
  [message-view error-container-style error-message-style styles/error-exclamation (i18n/label :t/wallet-error)])

(defn wallet-syncing [error-container-style error-message-style]
  [message-view error-container-style error-message-style styles/warning-exclamation (i18n/label :t/sync-in-progress)])

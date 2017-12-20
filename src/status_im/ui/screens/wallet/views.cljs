(ns status-im.ui.screens.wallet.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(defn- message-view [icon-container-style label]
  [react/view {:style styles/error-container}
   [react/view {:style styles/error-message-container}
    [vector-icons/icon :icons/exclamation_mark {:color           :white
                                                :container-style icon-container-style}]
    [react/text {:style styles/error-message}
     label]]])

(def error-message-view [message-view styles/error-exclamation (i18n/label :t/wallet-error)])

(def wallet-syncing [message-view styles/warning-exclamation (i18n/label :t/sync-in-progress)])

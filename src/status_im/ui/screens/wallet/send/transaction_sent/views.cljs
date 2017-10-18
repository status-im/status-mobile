(ns status-im.ui.screens.wallet.send.transaction-sent.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.components.react :as react]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.send.transaction-sent.styles :as styles]
            [status-im.components.styles :as components.styles]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

(defview transaction-sent []
  (letsubs [close-transaction-screen-event [:wallet.sent/close-transaction-screen-event]]
    [react/view wallet.styles/wallet-modal-container
     [status-bar/status-bar {:type :transparent}]
     [react/view styles/transaction-sent-container
      [react/view styles/ok-icon-container
       [vi/icon :icons/ok {:color components.styles/color-blue4}]]
      [react/text {:style      styles/transaction-sent
                   :font       (if platform/android? :medium :default)}
       (i18n/label :t/transaction-sent)]
      [react/view styles/gap]
      [react/text {:style styles/transaction-sent-description} (i18n/label :t/transaction-description)]]
     [react/view components.styles/flex]
     [react/touchable-highlight {:on-press #()}; TODO (andrey) #(re-frame/dispatch [:navigate-to-clean :wallet-transaction-details])}
      [react/view styles/transaction-details-container
       [react/text {:style styles/transaction-details
                    :font       (if platform/android? :medium :default)
                    :uppercase? (get-in platform/platform-specific [:uppercase?])}
        (i18n/label :t/view-transaction-details)]]]
     [components/separator]
     [react/touchable-highlight {:on-press #(re-frame/dispatch close-transaction-screen-event)}
      [react/view styles/got-it-container
       [react/text {:style styles/got-it
                    :font       (if platform/android? :medium :default)
                    :uppercase? (get-in platform/platform-specific [:uppercase?])}
        (i18n/label :t/got-it)]]]]))

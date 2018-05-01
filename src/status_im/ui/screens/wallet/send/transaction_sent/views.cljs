(ns status-im.ui.screens.wallet.send.transaction-sent.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.send.transaction-sent.styles :as styles]
            [status-im.ui.components.styles :as components.styles]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

(defview transaction-sent [& [modal?]]
  [react/view wallet.styles/wallet-modal-container
   [status-bar/status-bar {:type (if modal? :modal-wallet :transparent)}]
   [react/view styles/transaction-sent-container
    [react/view styles/ok-icon-container
     [vi/icon :icons/ok {:color components.styles/color-blue4}]]
    [react/text {:style               styles/transaction-sent
                 :font                (if platform/android? :medium :default)
                 :accessibility-label :transaction-sent-text}
     (i18n/label :t/transaction-sent)]
    [react/view styles/gap]
    [react/text {:style styles/transaction-sent-description} (i18n/label :t/transaction-description)]]
   [react/view components.styles/flex]
   [components/separator]
   [react/touchable-highlight {:on-press            #(re-frame/dispatch [:close-transaction-sent-screen])
                               :accessibility-label :got-it-button}
    [react/view styles/got-it-container
     [react/text {:style      styles/got-it
                  :font       (if platform/android? :medium :default)
                  :uppercase? true}
      (i18n/label :t/got-it)]]]])

(defview transaction-sent-modal []
  [transaction-sent true])

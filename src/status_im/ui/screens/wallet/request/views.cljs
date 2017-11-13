(ns status-im.ui.screens.wallet.request.views
  (:require-macros [status-im.utils.views :as views])
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.react :as react]
    [status-im.ui.components.qr-code :as components.qr-code]
    [status-im.ui.components.toolbar.actions :as actions]
    [status-im.ui.components.toolbar.view :as toolbar]
    [status-im.ui.components.status-bar :as status-bar]
    [status-im.ui.screens.wallet.styles :as wallet.styles]
    [status-im.ui.components.icons.vector-icons :as vi]
    [status-im.ui.screens.wallet.components.views :as components]
    [status-im.ui.screens.wallet.request.styles :as styles]
    [status-im.ui.components.styles :as components.styles]
    [status-im.i18n :as i18n]
    [status-im.utils.platform :as platform]
    [status-im.utils.eip.eip67 :as eip67]
    [status-im.ui.components.common.common :as common]))

(defn toolbar-view []
  [toolbar/toolbar {:style wallet.styles/toolbar :hide-border? true}
   [toolbar/nav-button (actions/back-white actions/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/request-transaction)]])

(defn send-request []
  (re-frame/dispatch [:navigate-to-modal
                      :contact-list-modal
                      {:handler #(re-frame/dispatch [:wallet-send-request %1])
                       :action  :request
                       :params  {:hide-actions? true}}]))

(views/defview qr-code [amount]
  (views/letsubs [account [:get-current-account]]
    [components.qr-code/qr-code
     {:value   (eip67/generate-uri (:address account) (when amount {:value amount}))
      :size    256}]))

(views/defview request-transaction []
  ;;Because input field is in the end of view we will scroll to the end on input focus event
  (views/letsubs [amount           [:get-in [:wallet/request-transaction :amount]]
                  amount-error     [:get-in [:wallet/request-transaction :amount-error]]
                  request-enabled? [:wallet.request/request-enabled?]
                  scroll           (atom nil)]
    [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
     [status-bar/status-bar {:type :wallet}]
     [toolbar-view]
     [common/network-info {:text-color :white}]
     [react/scroll-view {:ref #(reset! scroll %)}
      [react/view components.styles/flex
        [react/view styles/network-container
         [react/view styles/qr-container
          [qr-code amount]]]
        [react/view wallet.styles/choose-wallet-container
         [components/choose-wallet]]
        [react/view wallet.styles/amount-container
         [components/amount-input
          {:error         amount-error
           :input-options {:on-focus (fn [] (when @scroll (js/setTimeout #(.scrollToEnd @scroll) 100)))
                           :on-change-text
                           #(do (re-frame/dispatch [:set-in [:wallet/request-transaction :amount] %])
                                (re-frame/dispatch [:wallet-validate-request-amount]))}}]
         [react/view wallet.styles/choose-currency-container
          [components/choose-currency wallet.styles/choose-currency]]]]]
     [components/separator]
     [react/view wallet.styles/buttons-container
      [react/touchable-highlight {:style wallet.styles/button :disabled true}
       [react/view (wallet.styles/button-container false)
        [vi/icon :icons/share {:color :white :container-style styles/share-icon-container}]
        [components/button-text (i18n/label :t/share)]]]
      [react/view components.styles/flex]
      [react/touchable-highlight {:style wallet.styles/button :disabled (not request-enabled?) :on-press  send-request}
       [react/view (wallet.styles/button-container request-enabled?)
        [components/button-text (i18n/label :t/send-request)]
        [vi/icon :icons/forward {:color :white :container-style wallet.styles/forward-icon-container}]]]]]))
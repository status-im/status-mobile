(ns status-im.ui.screens.wallet.request.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.request.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.ethereum.tokens :as tokens]))

(defn toolbar-view []
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (actions/back-white actions/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/request-transaction)]])

(defn send-request []
  (re-frame/dispatch [:navigate-to-modal
                      :contact-list-modal
                      {:handler #(re-frame/dispatch [:wallet-send-request %1])
                       :action  :request
                       :params  {:hide-actions? true}}]))

(defn- generate-value [address {:keys [symbol] :as m}]
  (if (tokens/ethereum? symbol)
    (eip681/generate-uri address (dissoc m :symbol))
    (eip681/generate-erc20-uri address m)))

(views/defview qr-code [amount symbol]
  (views/letsubs [account [:get-current-account]
                  chain-id [:get-network-id]]
    [qr-code-viewer/qr-code
     (let [address (ethereum/normalized-address (:address account))
           params  {:chain-id chain-id :value amount :symbol (or symbol :ETH)}]
       {:value   (generate-value address params)
        :size    256})]))

(views/defview request-transaction []
  ;;Because input field is in the end of view we will scroll to the end on input focus event
  (views/letsubs [amount           [:get-in [:wallet :request-transaction :amount]]
                  amount-error     [:get-in [:wallet :request-transaction :amount-error]]
                  symbol           [:wallet.request/symbol]
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
          [react/with-activity-indicator
           {:style wallet.styles/qr-code-preview}
           [qr-code amount symbol]]]]
       [components/amount-selector
        {:error         amount-error
         :input-options {:on-focus (fn [] (when @scroll (js/setTimeout #(.scrollToEnd @scroll) 100)))
                         :on-change-text #(re-frame/dispatch [:wallet.request/set-and-validate-amount %])}}]
       [components/asset-selector {:type   :request
                                   :symbol symbol}]]]
     [components/separator]
     [react/view wallet.styles/buttons-container
      [react/touchable-highlight {:style wallet.styles/button :disabled true}
       [react/view (wallet.styles/button-container false)
        [vector-icons/icon :icons/share {:color :white :container-style styles/share-icon-container}]
        [components/button-text (i18n/label :t/share)]]]
      [react/view components.styles/flex]
      [react/touchable-highlight {:style wallet.styles/button :disabled (not request-enabled?) :on-press send-request}
       [react/view (wallet.styles/button-container request-enabled?)
        [components/button-text (i18n/label :t/send-request)]
        [vector-icons/icon :icons/forward {:color :white :container-style wallet.styles/forward-icon-container}]]]]]))

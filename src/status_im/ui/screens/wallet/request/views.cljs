(ns status-im.ui.screens.wallet.request.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.request.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.wallet.utils :as wallet.utils])
  (:require-macros [status-im.utils.views :as views]))

;;TODO DEPRECATED
(views/defview send-transaction-request []
  ;; TODO(jeluard) both send and request flows should be merged
  (views/letsubs [chain          [:ethereum/chain-keyword]
                  {:keys [amount amount-error amount-text symbol to
                          to-name public-key]} [:wallet.request/transaction]
                  network-status [:network-status]
                  all-tokens     [:wallet/all-tokens]
                  scroll         (atom nil)]
    (let [{:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)]
      [wallet.components/simple-screen {:avoid-keyboard? true}
       [wallet.components/topbar (i18n/label :t/new-request)]
       [react/view components.styles/flex
        [wallet.components/network-info]
        [react/scroll-view {:ref #(reset! scroll %) :keyboardShouldPersistTaps :always}
         [react/view styles/request-details-wrapper
          [wallet.components/recipient-selector
           {:contact-only? true
            :address       to
            :name          to-name
            :request?      true}]
          [wallet.components/asset-selector
           {:disabled? false
            :type      :request
            :symbol    symbol}]
          [wallet.components/amount-selector
           {:error         amount-error
            :disabled?     (= :offline network-status)
            :amount        amount
            :amount-text   amount-text
            :input-options {:on-focus       (fn [] (when @scroll (utils/set-timeout #(.scrollToEnd @scroll) 100)))
                            :on-change-text #(re-frame/dispatch [:wallet.request/set-and-validate-amount % symbol decimals])}}
           token]]]
        [toolbar/toolbar
         {:right {:type                :next
                  :disabled?           (or amount-error (not (and to amount)))
                  :on-press            #(re-frame/dispatch [:wallet-send-request public-key amount
                                                            (wallet.utils/display-symbol token) decimals])
                  :accessibility-label :sent-request-button
                  :label               :t/send-request}}]]])))

(views/defview share-address []
  (views/letsubs [{:keys [address]} [:popover/popover]
                  chain-id    [:chain-id]
                  width       (reagent/atom nil)]
    [react/view {:on-layout #(reset! width (-> % .-nativeEvent .-layout .-width))}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      (when @width
        [qr-code-viewer/qr-code-view
         (- @width 32)
         (eip681/generate-uri address {:chain-id chain-id})])
      [copyable-text/copyable-text-view
       {:label           :t/ens-wallet-address
        :container-style {:margin-top 12 :margin-bottom 4}
        :copied-text     (eip55/address->checksum address)}
       [react/text {:number-of-lines     1
                    :ellipsize-mode      :middle
                    :accessibility-label :address-text
                    :style               {:line-height 22 :font-size 15
                                          :font-family "monospace"}}
        (eip55/address->checksum address)]]]
     [react/view {:margin-top 12 :margin-bottom 8}
      [button/button
       {:on-press            #(re-frame/dispatch [:wallet.accounts/share address])
        :label               :t/share-address
        :accessibility-label :share-address-button}]]
     ;;TODO temporary hide for v1
     #_[button/button
        {:on-press
         #(do
            (re-frame/dispatch [:hide-popover])
            (re-frame/dispatch [:navigate-to :wallet-send-transaction-request address]))
         :accessibility-label :sent-transaction-request-button
         :label               :t/send-transaction-request
         :type                :secondary}]]))

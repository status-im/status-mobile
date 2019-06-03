(ns status-im.ui.screens.wallet.request.views
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.request.styles :as styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :as views]))

;; Request screen

(views/defview send-transaction-request []
  ;; TODO(jeluard) both send and request flows should be merged
  (views/letsubs [chain                           [:ethereum/chain-keyword]
                  {:keys [to to-name public-key]} [:wallet.send/transaction]
                  {:keys [amount amount-error amount-text symbol to
                          to-name public-key]}    [:wallet.request/transaction]
                  network-status                  [:network-status]
                  all-tokens                      [:wallet/all-tokens]
                  scroll                          (atom nil)]
    (let [{:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)]
      [wallet.components/simple-screen {:avoid-keyboard? true}
       [wallet.components/toolbar (i18n/label :t/new-request)]
       [react/view components.styles/flex
        [common/network-info {:text-color :white}]
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
        [bottom-buttons/bottom-buttons styles/bottom-buttons
         nil   ;; Force a phantom button to ensure consistency with other transaction screens which define 2 buttons
         [button/button {:disabled?           (or amount-error (not (and to amount)))
                         :on-press            #(re-frame/dispatch [:wallet-send-request public-key amount
                                                                   (wallet.utils/display-symbol token) decimals])
                         :text-style          {:padding-horizontal 0}
                         :accessibility-label :sent-request-button}
          (i18n/label :t/send-request)
          [vector-icons/icon :main-icons/next {:color :white}]]]]])))

;; Main screen

(defn send-transaction-request-button [value]
  [button/primary-button {:on-press            #(re-frame/dispatch [:navigate-to :wallet-send-transaction-request])
                          :style               styles/send-request
                          :accessibility-label :sent-transaction-request-button}
   (i18n/label :t/send-transaction-request)])

(views/defview request-transaction []
  (views/letsubs [address-hex [:account/hex-address]
                  chain-id    [:get-network-id]]
    [wallet.components/simple-screen
     [wallet.components/toolbar {:transparent? true}
      wallet.components/default-action
      (i18n/label :t/receive)
      [toolbar/actions [{:icon      :main-icons/share
                         :icon-opts {:color               :white
                                     :accessibility-label :share-button}
                         :handler   #(list-selection/open-share {:message (eip55/address->checksum address-hex)})}]]]
     [react/view {:flex 1}
      [common/network-info {:text-color :white}]
      [qr-code-viewer/qr-code-viewer
       {:hint-style    styles/hint
        :footer-style  styles/footer
        :footer-button send-transaction-request-button
        :value         (eip681/generate-uri address-hex {:chain-id chain-id})
        :hint          (i18n/label :t/request-qr-legend)
        :legend        (eip55/address->checksum address-hex)}]]]))

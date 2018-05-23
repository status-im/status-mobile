(ns status-im.ui.screens.wallet.request.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.request.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components :as comp]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.utils :as utils]))

;; Request screen

(views/defview send-transaction-request []
  ;; TODO(jeluard) both send and request flows should be merged
  (views/letsubs [{:keys [to to-name whisper-identity]} [:wallet.send/transaction]
                  {:keys [amount amount-error amount-text]}         [:wallet.request/transaction]
                  scroll (atom nil)]
    [comp/simple-screen {:avoid-keyboard? true}
     [comp/toolbar (i18n/label :t/new-request)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:ref #(reset! scroll %) :keyboardShouldPersistTaps :always}
       [react/view styles/request-details-wrapper
        [components/recipient-selector {:contact-only? true
                                        :address       to
                                        :name          to-name
                                        :request?      true}]
        [components/asset-selector {:disabled? true
                                    :symbol    :ETH}]
        [components/amount-selector {:error         amount-error
                                     :amount amount
                                     :amount-text amount-text
                                     :input-options {:max-length     21
                                                     :on-focus       (fn [] (when @scroll (utils/set-timeout #(.scrollToEnd @scroll) 100)))
                                                     :on-change-text #(re-frame/dispatch [:wallet.request/set-and-validate-amount %])}}]]]
      [bottom-buttons/bottom-buttons styles/bottom-buttons
       nil ;; Force a phantom button to ensure consistency with other transaction screens which define 2 buttons
       [button/button {:disabled?           (not (and to amount))
                       :on-press            #(re-frame/dispatch [:wallet-send-request whisper-identity amount])
                       :text-style          {:padding-horizontal 0}
                       :accessibility-label :sent-request-button}
        (i18n/label :t/send-request)
        [vector-icons/icon :icons/forward {:color :white}]]]]]))

;; Main screen

(defn- qr-code [address chain-id]
  [qr-code-viewer/qr-code-viewer {:hint-style styles/hint :footer-style styles/footer}
   (eip681/generate-uri address {:chain-id chain-id})
   (i18n/label :t/request-qr-legend)
   address])

(views/defview request-transaction []
  (views/letsubs [address-hex       [:get-current-account-hex]
                  chain-id          [:get-network-id]]
    [comp/simple-screen
     [comp/toolbar {}
      comp/default-action
      (i18n/label :t/receive)
      [toolbar/actions [{:icon      :icons/share
                         :icon-opts {:color               :white
                                     :accessibility-label :share-button}
                         :handler   #(list-selection/open-share {:message address-hex})}]]]
     [react/view {:flex 1}
      [common/network-info {:text-color :white}]
      [react/scroll-view styles/request-wrapper
       [qr-code address-hex chain-id]
       [button/primary-button {:on-press            #(re-frame/dispatch [:navigate-to :wallet-send-transaction-request])
                               :style               styles/send-request
                               :accessibility-label :sent-transaction-request-button}
        (i18n/label :t/send-transaction-request)]]]]))

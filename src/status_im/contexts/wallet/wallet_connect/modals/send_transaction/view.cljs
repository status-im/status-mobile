(ns status-im.contexts.wallet.wallet-connect.modals.send-transaction.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.wallet.wallet-connect.modals.common.data-block.view :as data-block]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.view :as footer]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.view :as header]
            [status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view :as page-nav]
            [status-im.contexts.wallet.wallet-connect.modals.common.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [bottom                (safe-area/get-bottom)
        {:keys [customization-color]
         :as   account}       (rf/sub [:wallet-connect/current-request-account-details])
        dapp                  (rf/sub [:wallet-connect/current-request-dapp])
        network               (rf/sub [:wallet-connect/current-request-network])
        {:keys [max-fees-fiat-formatted
                error-state]} (rf/sub [:wallet-connect/current-request-transaction-information])]
    [rn/view {:style (style/container bottom)}
     [quo/gradient-cover {:customization-color customization-color}]
     [page-nav/view
      {:accessibility-label :wallet-connect-sign-message-close}]
     [rn/view {:flex 1}
      [rn/view {:style style/data-content-container}
       [header/view
        {:label   (i18n/label :t/wallet-connect-send-transaction-header)
         :dapp    dapp
         :account account}]
       [data-block/view]]
      (when error-state
        [quo/alert-banner
         {:action? false
          :text    (i18n/label (condp = error-state
                                 :not-enough-assets-to-pay-gas-fees
                                 :t/not-enough-assets-to-pay-gas-fees

                                 :not-enough-assets
                                 :t/not-enough-assets))}])
      [footer/view
       {:warning-label     (i18n/label :t/wallet-connect-send-transaction-warning)
        :slide-button-text (i18n/label :t/slide-to-send)
        :disabled?         error-state}
       [quo/data-item
        {:status          :default
         :card?           false
         :container-style style/data-item
         :title           (i18n/label :t/network)
         :subtitle-type   :network
         :network-image   (:source network)
         :subtitle        (:full-name network)}]
       [quo/data-item
        {:size            :small
         :status          :default
         :card?           false
         :container-style style/data-item
         :title           (i18n/label :t/max-fees)
         :subtitle        (or max-fees-fiat-formatted (i18n/label :t/no-fees))}]]]]))


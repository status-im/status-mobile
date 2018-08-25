(ns status-im.ui.screens.wallet.transaction-fee.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]))

(defn return-to-transaction [modal?]
  (if modal?
    ;;TODO(andrey) artificial navigation stack for modals (should be reworked)
    (re-frame/dispatch [:navigate-to-modal :wallet-send-transaction-modal])
    (act/default-handler)))

(defn- toolbar [modal? title]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/close-white #(return-to-transaction modal?))]
   [toolbar/content-title {:color :white} title]])

(defview transaction-fee []
  (letsubs [send-transaction            [:wallet.send/transaction]
            network                     [:get-current-account-network]
            {gas-edit       :gas
             max-fee        :max-fee
             gas-price-edit :gas-price} [:wallet/edit]]
    (let [modal?         (:id send-transaction)
          {:keys [amount symbol]} send-transaction
          gas            (:value gas-edit)
          gas-price      (:value gas-price-edit)
          {:keys [decimals]} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)]
      [components/simple-screen {:status-bar-type :modal-wallet}
       [toolbar modal? (i18n/label :t/wallet-transaction-fee)]
       [react/view components.styles/flex
        [react/view {:flex-direction :row}

         [react/view styles/gas-container-wrapper
          [components/cartouche {}
           (i18n/label :t/gas-limit)
           [react/view styles/gas-input-wrapper
            [react/text-input (merge styles/transaction-fee-input
                                     {:on-change-text      #(re-frame/dispatch [:wallet.send/edit-value :gas %])
                                      :default-value       gas
                                      :accessibility-label :gas-limit-input})]]]
          (when (:invalid? gas-edit)
            [tooltip/tooltip (i18n/label :t/invalid-number) styles/gas-input-error-tooltip])]

         [react/view styles/gas-container-wrapper
          [components/cartouche {}
           (i18n/label :t/gas-price)
           [react/view styles/gas-input-wrapper
            [react/text-input (merge styles/transaction-fee-input
                                     {:on-change-text      #(re-frame/dispatch [:wallet.send/edit-value :gas-price %])
                                      :default-value       gas-price
                                      :accessibility-label :gas-price-input})]
            [components/cartouche-secondary-text
             (i18n/label :t/gwei)]]]
          (when (:invalid? gas-price-edit)
            [tooltip/tooltip
             (i18n/label (if (= :invalid-number (:invalid? gas-price-edit))
                           :t/invalid-number
                           :t/wallet-send-min-wei))
             styles/gas-input-error-tooltip])]]

        [react/view styles/transaction-fee-info
         [react/view styles/transaction-fee-info-icon
          [react/text {:style styles/transaction-fee-info-icon-text} "?"]]
         [react/view styles/transaction-fee-info-text-wrapper
          [react/i18n-text {:style styles/advanced-fees-text
                            :key   :wallet-transaction-fee-details}]]]
        [components/separator]
        [react/view styles/transaction-fee-block-wrapper
         [components/cartouche {:disabled? true}
          (i18n/label :t/amount)
          [react/view {:accessibility-label :amount-input}
           [components/cartouche-text-content
            (str (money/to-fixed (money/internal->formatted amount symbol decimals)))
            (name symbol)]]]
         [components/cartouche {:disabled? true}
          (i18n/label :t/wallet-transaction-total-fee)
          [react/view {:accessibility-label :total-fee-input}
           [components/cartouche-text-content
            (str max-fee " " (i18n/label :t/eth))]]]]

        [bottom-buttons/bottom-buttons styles/fee-buttons
         [button/button {:on-press            #(re-frame/dispatch [:wallet.send/reset-gas-default])
                         :accessibility-label :reset-to-default-button}
          (i18n/label :t/reset-default)]
         [button/button {:on-press            #(do (re-frame/dispatch [:wallet.send/set-gas-details
                                                                       (:value-number gas-edit)
                                                                       (:value-number gas-price-edit)])
                                                   (return-to-transaction modal?))
                         :accessibility-label :done-button
                         :disabled?           (or (:invalid? gas-edit)
                                                  (:invalid? gas-price-edit))}
          (i18n/label :t/done)]]]])))
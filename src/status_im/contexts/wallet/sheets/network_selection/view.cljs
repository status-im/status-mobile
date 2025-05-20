(ns status-im.contexts.wallet.sheets.network-selection.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.sheets.network-selection.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- network-item
  [{:keys [network on-select-network source]}]
  (let [{:keys [full-name chain-id]} network
        {balance-in-crypto :crypto
         balance-in-fiat   :fiat}    (if (= source :swap)
                                       (rf/sub [:wallet/swap-asset-to-pay-network-balance
                                                chain-id])
                                       (rf/sub [:wallet/send-token-network-balance
                                                chain-id]))]
    [quo/network-list
     {:label           full-name
      :network-image   (:source network)
      :token-value     balance-in-crypto
      :fiat-value      balance-in-fiat
      :on-press        #(on-select-network network)
      :container-style style/network-list-container}]))

(defn view
  [{:keys [token-symbol on-select-network source title]
    :or   {source :swap
           title  (i18n/label :t/select-network)}}]
  (let [{token-networks :networks} (case source
                                     :swap   (rf/sub [:wallet/swap-asset-to-pay])
                                     :bridge (rf/sub [:wallet/bridge-token])
                                     (rf/sub [:wallet/wallet-send-token]))
        render-fn                  (rn/use-callback (fn [network]
                                                      [network-item
                                                       {:network           network
                                                        :on-select-network on-select-network
                                                        :source            source}]))]
    [:<>
     [rn/view {:style style/header-container}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       title]
      [quo/context-tag
       {:type            :token
        :size            24
        :token           token-symbol
        :container-style style/context-tag}]]
     [rn/flat-list
      {:data           (vec token-networks)
       :render-fn      render-fn
       :scroll-enabled false}]]))

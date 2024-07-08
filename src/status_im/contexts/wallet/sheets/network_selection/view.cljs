(ns status-im.contexts.wallet.sheets.network-selection.view
  (:require [quo.core :as quo]
            [quo.foundations.resources :as quo.resources]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.sheets.network-selection.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- network-item
  [{:keys [network on-select-network]}]
  (let [{:keys [network-name
                chain-id]}        network
        {balance-in-crypto :crypto
         balance-in-fiat   :fiat} (rf/sub [:wallet/swap-asset-to-pay-network-balance chain-id])
        mainnet?                  (= network-name constants/mainnet-network-name)]
    [quo/network-list
     {:label           (name network-name)
      :network-image   (quo.resources/get-network network-name)
      :token-value     balance-in-crypto
      :fiat-value      balance-in-fiat
      :on-press        #(on-select-network network)
      :container-style (style/network-list-container mainnet?)}]))

(defn view
  [{:keys [on-select-network]}]
  (let [token-symbol                         (rf/sub [:wallet/swap-asset-to-pay-token-symbol])
        {mainnet-network  :mainnet-network
         layer-2-networks :layer-2-networks} (rf/sub [:wallet/swap-asset-to-pay-networks])
        render-fn                            (rn/use-callback (fn [network]
                                                                [network-item
                                                                 {:network network
                                                                  :on-select-network
                                                                  on-select-network}]))]
    [:<>
     [rn/view {:style style/header-container}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       (i18n/label :t/select-network)]
      [quo/context-tag
       {:type            :token
        :size            24
        :token           token-symbol
        :container-style style/context-tag}]]
     (when mainnet-network
       [network-item
        {:network           mainnet-network
         :on-select-network on-select-network}])
     [quo/divider-label {:container-style style/divider-label}
      (i18n/label :t/layer-2)]
     [rn/flat-list
      {:data           (vec layer-2-networks)
       :render-fn      render-fn
       :scroll-enabled false}]]))

(ns status-im.contexts.wallet.trading.swap.components.network-selector
  (:require [quo.core :as quo]
            [quo.foundations.resources :as quo.resources]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.sheets.network-selection.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- network-item
  [{:keys [network on-select-network mainnet? token-symbol address]}]
  (let [{:keys [network-name full-name chain-id]} network
        {:keys [crypto fiat]}                     (rf/sub [:wallet/token-balance address token-symbol
                                                           chain-id])]
    [quo/network-list
     {:label           full-name
      :network-image   (quo.resources/get-network network-name)
      :token-value     crypto
      :fiat-value      fiat
      :on-press        #(on-select-network network)
      :container-style (style/network-list-container mainnet?)}]))

(defn view
  [{:keys [token-symbol on-select-network address]}]
  (let [supported-networks (rf/sub [:wallet/network-details])
        networks-by-layer  (group-by :layer supported-networks)
        mainnet-network    (-> networks-by-layer (get constants/layer-1-network) first)
        layer-2-networks   (get networks-by-layer constants/layer-2-network)
        render-fn          (rn/use-callback (fn [network]
                                              [network-item
                                               {:network           network
                                                :address           address
                                                :token-symbol      token-symbol
                                                :on-select-network on-select-network}])
                                            [on-select-network address])]
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
         :address           address
         :token-symbol      token-symbol
         :mainnet?          true
         :on-select-network on-select-network}])
     [quo/divider-label {:container-style style/divider-label}
      (i18n/label :t/layer-2)]
     [rn/flat-list
      {:data           layer-2-networks
       :render-fn      render-fn
       :scroll-enabled false}]]))

(ns status-im.contexts.wallet.sheets.network-filter.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [selected-networks (rf/sub [:wallet/selected-networks])
        selector-state    (rf/sub [:wallet/network-filter-selector-state])
        color             (rf/sub [:profile/customization-color])
        network-details   (rf/sub [:wallet/network-details])
        viewing-account?  (rf/sub [:wallet/viewing-account?])
        balance-per-chain (if viewing-account?
                            (rf/sub [:wallet/current-viewing-account-fiat-balance-per-chain])
                            (rf/sub [:wallet/aggregated-fiat-balance-per-chain]))
        mainnet           (first network-details)
        layer-2-networks  (rest network-details)]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/select-networks)}]
     [quo/category
      {:list-type :settings
       :data      [(utils/make-network-item
                    {:state        selector-state
                     :network-name (:network-name mainnet)
                     :color        color
                     :networks     selected-networks
                     :label-props  (get balance-per-chain (:chain-id mainnet))
                     :on-change    #(rf/dispatch
                                     [:wallet/update-selected-networks
                                      (:network-name
                                       mainnet)])})]}]
     [quo/category
      {:list-type :settings
       :label     (i18n/label :t/layer-2)
       :data      (mapv (fn [network]
                          (utils/make-network-item
                           {:state        selector-state
                            :network-name (:network-name network)
                            :color        color
                            :networks     selected-networks
                            :label-props  (get balance-per-chain (:chain-id network))
                            :on-change    #(rf/dispatch
                                            [:wallet/update-selected-networks
                                             (:network-name
                                              network)])}))
                        layer-2-networks)}]]))

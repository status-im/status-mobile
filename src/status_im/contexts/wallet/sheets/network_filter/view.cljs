(ns status-im.contexts.wallet.sheets.network-filter.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [selected-networks (rf/sub [:wallet/selected-networks])
        color             (rf/sub [:profile/customization-color])
        network-details   (rf/sub [:wallet/network-details])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/select-networks)}]
     [quo/information-box
      {:type  :informative
       :icon  :i/info
       :blur? false
       :style {:margin-horizontal 20
               :margin-bottom     4}}
      (i18n/label :t/base-chain-available-info)]
     [quo/category
      {:list-type :settings
       :data      (mapv (fn [network]
                          (utils/make-network-item
                           {:network-name (:network-name network)
                            :chain-id     (:chain-id network)
                            :full-name    (:full-name network)
                            :color        color
                            :networks     selected-networks
                            :on-change    #(rf/dispatch
                                            [:wallet/update-selected-networks
                                             (:network-name
                                              network)])}))
                        network-details)}]]))

(ns status-im.contexts.wallet.sheets.network-filter.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.networks.core :as networks]
    [status-im.contexts.wallet.sheets.network-filter.network-field :as network-field]
    [status-im.contexts.wallet.sheets.network-filter.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-network-press
  [chain-id]
  (rf/dispatch [:wallet/filter-network-balances {:by-id #{chain-id}}])
  (rf/dispatch [:hide-bottom-sheet]))

(defn render-network
  [chain-id]
  (let [{:keys [chain-id full-name source]} (rf/sub [:wallet/network-by-id chain-id])
        network-balance                     (rf/sub [:wallet/balance-for-network-filter chain-id])
        n-collectibles                      (rf/sub [:wallet/collectibles-count-for-network-filter
                                                     chain-id])]
    [network-field/view
     {:title          full-name
      :image-source   source
      :balance        network-balance
      :n-collectibles n-collectibles
      :on-press       #(on-network-press chain-id)
      :new?           (networks/new-network? chain-id)}]))

(defn view
  []
  (let [active-chain-ids (rf/sub [:wallet/active-chain-ids])]
    [rn/view {:style {:padding-horizontal 20}}
     [rn/view {:style style/header-container}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       (i18n/label :t/active-networks)]]
     [quo/item-list
      {:data            active-chain-ids
       :render-fn       render-network
       :container-style {:margin-top 12}}]
     [quo/button
      {:type            :outline
       :container-style {:margin-vertical 12}
       :size            50
       :on-press        #(rf/dispatch [:open-modal :screen/settings.network-settings])}
      [quo/text {:weight :medium} (i18n/label :t/manage-networks)]]]))
